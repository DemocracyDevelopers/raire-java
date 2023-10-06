/*
  Copyright 2023 Democracy Developers
  This is a Java re-implementation of raire-rs https://github.com/DemocracyDevelopers/raire-rs
  It attempts to copy the design, API, and naming as much as possible subject to being idiomatic and efficient Java.

  This file is part of raire-java.
  raire-java is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  raire-java is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
  You should have received a copy of the GNU Affero General Public License along with ConcreteSTV.  If not, see <https://www.gnu.org/licenses/>.

 */

package au.org.democracydevelopers.raire.algorithm;

import au.org.democracydevelopers.raire.RaireException;
import au.org.democracydevelopers.raire.assertions.*;
import au.org.democracydevelopers.raire.audittype.AuditType;
import au.org.democracydevelopers.raire.irv.IRVResult;
import au.org.democracydevelopers.raire.irv.Votes;
import au.org.democracydevelopers.raire.time.TimeOut;
import au.org.democracydevelopers.raire.time.TimeTaken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

/** The main result of the RAIRE algorithm. */
public class RaireResult {
    public AssertionAndDifficulty[] assertions;
    public double difficulty;
    public int margin; // The smallest margin in votes in one of the assertions. Provided primarily for informational purposes.
    public int winner;
    public int num_candidates;
    public TimeTaken time_to_determine_winners;
    public TimeTaken time_to_find_assertions;
    public TimeTaken time_to_trim_assertions;
    public boolean warning_trim_timed_out;

    static AssertionAndDifficulty find_best_audit(int[] pi, Votes votes, AuditType audit, NotEliminatedBeforeCache neb_cache) {
        final int c = pi[0];
        AssertionAndDifficulty res = new AssertionAndDifficulty(new NotEliminatedBefore(c,c),Double.POSITIVE_INFINITY,0); // dummy infinitely bad assertion
        // consider WO contests
        int[] remaining_pi = Arrays.copyOfRange(pi,1,pi.length);
        AssertionAndDifficulty bestNEB = NotEliminatedBefore.findBestAssertionUsingCache(c,remaining_pi,votes,neb_cache);
        if (bestNEB!=null && bestNEB.difficulty< res.difficulty) res=bestNEB;
        // consider IRV(c,c′,{c′′ | c′′ ∈ π}): Assertion that c beats some c′ != c ∈ π
        AssertionAndDifficulty bestNEN = NotEliminatedNext.findBestDifficulty(votes, audit, pi, c);
        if (bestNEN!=null && bestNEN.difficulty< res.difficulty) res=bestNEN;
        return res;
    }

    /** This is the main raire algorithm... equivalent of the raire() function in rust-rs */
    public RaireResult(Votes votes, Integer claimed_winner, AuditType audit, TimeOut timeout) throws RaireException { // TODO add trim algorithm
        IRVResult irv_result = votes.runElection(timeout);
        this.time_to_determine_winners=timeout.timeTaken();
        if (irv_result.possibleWinners.length!=1) throw new RaireException.TiedWinners(irv_result.possibleWinners);
        this.winner = irv_result.possibleWinners[0];
        if (claimed_winner!=null && claimed_winner!=winner) throw new RaireException.WrongWinner(irv_result.possibleWinners);
        NotEliminatedBeforeCache neb_cache = new NotEliminatedBeforeCache(votes,audit);
        ArrayList<AssertionAndDifficulty> assertions = new ArrayList<>(); // A in the original paper
        double lower_bound = 0.0; // LB in the original paper. A lower bound on the difficulty of the problem.
        PriorityQueue<SequenceAndEffort> frontier = new PriorityQueue<>(); // F in the original paper
        double last_difficulty = Double.POSITIVE_INFINITY;
        this.num_candidates=votes.numCandidates();
        // Populate F with single-candidate sequences
        for (int c=0;c<votes.numCandidates();c++) if (c!=winner) { // 4 for each(c ∈ C \ {c w }):
            int[] pi = {c};
            //  asr[π] ← a ⊲ Record best assertion for π
            AssertionAndDifficulty best_assertion_for_pi = find_best_audit(pi,votes,audit,neb_cache);  // a in the original paper
            //  ba[π] ← π ⊲ Record best ancestor sequence for π
            int best_ancestor_length = pi.length;
            frontier.add(new SequenceAndEffort(pi,best_assertion_for_pi,best_ancestor_length,null)); // difficulty comes from asr[π].

        }
        // Repeatedly expand the sequence with largest ASN in F
        for (SequenceAndEffort sequence_being_considered=frontier.poll();sequence_being_considered!=null;sequence_being_considered=frontier.poll()) {
            if (timeout.quickCheckTimeout()) throw new RaireException.TimeoutFindingAssertions(Math.max(sequence_being_considered.difficulty(),lower_bound));
            if (sequence_being_considered.difficulty()!=last_difficulty) {
                last_difficulty=sequence_being_considered.difficulty();
                // log::trace!("Difficulty reduced to {}{}",last_difficulty,if last_difficulty<= lower_bound {" OK"} else {""});
            }
            if (sequence_being_considered.difficulty()<= lower_bound) { // may as well just include.
                sequence_being_considered.just_take_assertion(assertions,frontier);
            } else {
                /* TODO
                if USE_DIVING && !sequence_being_considered.dive_done.is_some() {
                    let mut last : Option<SequenceAndEffort> = None;
                    assert_eq!(irv_result.elimination_order.len(),votes.num_candidates() as usize);
                    for &c in irv_result.elimination_order.iter().rev() {
                        if !sequence_being_considered.pi.contains(&c) {
                            let new_sequence = match last.take() { // don't repeat work! Mark that this path has already been dealt with.
                                Some(mut l) => {
                                    l.dive_done=Some(c);
                                    let new_sequence = l.extend_by_candidate(c,votes,audit,&neb_cache);
                                    frontier.push(l);
                                    new_sequence
                                }
                                None => {
                                    sequence_being_considered.dive_done=Some(c);
                                    sequence_being_considered.extend_by_candidate(c,votes,audit,&neb_cache)
                                },
                            };
                            if new_sequence.difficulty()<= lower_bound {
                                new_sequence.just_take_assertion(&mut assertions,&mut frontier);
                                break;
                            } else {
                                last = Some(new_sequence);
                            }
                        }
                    }
                    if let Some(last) = last {
                        assert_eq!(last.pi.len(),votes.num_candidates() as usize);
                        last.contains_all_candidates(&mut assertions,&mut frontier,&mut lower_bound)?;
                    }
                }*/
                for (int c=0;c<num_candidates;c++) {// for each(c ∈ C \ π):
                    int finalC=c;
                    if (!(Arrays.stream(sequence_being_considered.pi).anyMatch(pc->pc==finalC)||sequence_being_considered.dive_done.equals(c))) {
                        SequenceAndEffort new_sequence = sequence_being_considered.extend_by_candidate(c,votes,audit,neb_cache);
                        if (new_sequence.pi.length==num_candidates) { // 22 if (|π′| = |C|):
                            lower_bound=new_sequence.contains_all_candidates(assertions,frontier,lower_bound);
                        } else {
                            frontier.add(new_sequence); // 31 F ← F ∪ {π ′ }
                        }
                    }
                }
            }
        }
        this.difficulty=lower_bound;
        this.time_to_find_assertions = timeout.timeTaken().minus(time_to_determine_winners);
        this.warning_trim_timed_out = false;
        /* TODO
        let warning_trim_timed_out = match crate::tree_showing_what_assertions_pruned_leaves::order_assertions_and_remove_unnecessary(&mut assertions,winner,votes.num_candidates(),trim_algorithm,timeout) {
            Ok(_) => false,
                    Err(RaireError::TimeoutTrimmingAssertions) => true,
                    Err(e) => {return Err(e);}
        };*/
        this.assertions = assertions.toArray(new AssertionAndDifficulty[assertions.size()]);
        this.time_to_trim_assertions = timeout.timeTaken().minus(time_to_find_assertions).minus(time_to_determine_winners);
        this.margin = assertions.stream().mapToInt(a->a.margin).min().orElse(0);
        // simple fast consistency check - make sure that the ostensible elimination order is consistent with all the assertions. If so, then the winner is not ruled out, and all is good.
        for (AssertionAndDifficulty a : this.assertions) {
            if (a.assertion.okEliminationOrderSuffix(irv_result.eliminationOrder)!= EffectOfAssertionOnEliminationOrderSuffix.Ok) throw new RaireException.InternalErrorRuledOutWinner();
        }
    }
}
