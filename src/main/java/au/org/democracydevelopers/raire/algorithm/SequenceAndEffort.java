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
import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raire.assertions.NotEliminatedBeforeCache;
import au.org.democracydevelopers.raire.audittype.AuditType;
import au.org.democracydevelopers.raire.irv.Votes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.PriorityQueue;

class SequenceAndEffort implements Comparable<SequenceAndEffort> {
    /** an elimination order suffix that needs to be ruled out. */
    final int[] pi;
    final AssertionAndDifficulty best_assertion_for_ancestor;
    /** the best ancestor for pi will be a subset of pi, in particular the last best_ancestor_length elements of pi. */
    final int best_ancestor_length;
    /** if not null, then a dive has already been done on the specified candidate. */
    final Integer dive_done;

    SequenceAndEffort(int[] pi, AssertionAndDifficulty best_assertion_for_ancestor, int best_ancestor_length, Integer dive_done) {
        this.pi = pi;
        this.best_assertion_for_ancestor = best_assertion_for_ancestor;
        this.best_ancestor_length = best_ancestor_length;
        this.dive_done = dive_done;
    }

    /** We want this in a priority queue which has the smallest at the head. Since
     * we want the highest difficulty at the head, we want the inverse of the normal
     * ordering on difficulty. We want a negative integer result if this has a higher
     * difficulty than other.
     */
    @Override
    public int compareTo(SequenceAndEffort other) {
        return Double.compare(other.best_assertion_for_ancestor.difficulty, best_assertion_for_ancestor.difficulty);
    }

    public double difficulty() { return best_assertion_for_ancestor.difficulty; }


    /** get the best ancestor of pi, which is a subset of pi. */
    public int[] best_ancestor() {
        return Arrays.copyOfRange(pi,pi.length-best_ancestor_length,pi.length);
    }

    public SequenceAndEffort extend_by_candidate(int c, Votes votes, AuditType audit, NotEliminatedBeforeCache neb_cache) {
        int [] pi_prime=new int[pi.length+1]; // π ′ ← [c] ++π
        pi_prime[0]=c;
        System.arraycopy(pi,0,pi_prime,1,pi.length);
        AssertionAndDifficulty a = RaireResult.find_best_audit(pi_prime, votes, audit,neb_cache); // a in the original paper
        int best_ancestor_length = a.difficulty <difficulty()?pi_prime.length:this.best_ancestor_length;
        AssertionAndDifficulty best_assertion_for_ancestor=a.difficulty <difficulty()?a:this.best_assertion_for_ancestor;
        return new SequenceAndEffort(pi_prime,best_assertion_for_ancestor,best_ancestor_length,null);
    }

    /** Called when the only use for this is to take the assertion and add it to the list of assertions.
      This checks that it is not already there and removes elements from the frontier that obviously match it. */
    public void just_take_assertion(ArrayList<AssertionAndDifficulty> assertions, PriorityQueue<SequenceAndEffort> frontier) {
        for (AssertionAndDifficulty a:assertions) {
            if (a.assertion.equals(best_assertion_for_ancestor.assertion)) {
                return; // don't add assertion as it was already there.
            }
        }
        int [] best_ancestor_pi = best_ancestor();
        // 15 F ← F \ {π ′ ∈ F | ba[π] is a suffix of π ′ }
        // TODO frontier.retain(|s|!s.pi.ends_with(best_ancestor_pi));
        // 14 A ← A ∪ {asr[ba[π]]}
        assertions.add(best_assertion_for_ancestor);
    }

    /** Called when a sequence has gone as far as it can - i.e. all candidates are in the exclusion order list. Returns the new lower bound, or throws an exception. */
    public double contains_all_candidates(ArrayList<AssertionAndDifficulty> assertions, PriorityQueue<SequenceAndEffort> frontier,double lower_bound) throws RaireException.CouldNotRuleOut {
        if (Double.isInfinite(difficulty())) { // 23 if (ASN (asr[ba[π ′ ]]) = ∞):
            //println!("Couldn't deal with {:?}",new_sequence.pi);
            throw new RaireException.CouldNotRuleOut(pi); // 24 terminate algorithm, full recount necessary
        } else {
            if (lower_bound<difficulty()) {
                lower_bound=difficulty(); // 27 LB ← max(LB, ASN (asr[ba[π′]]))
                // log::trace!("Found bound {} on elimination sequence {:?}",*bound,self.pi)
            }
            just_take_assertion(assertions,frontier); // Steps 26 and 28 are same as 14 and 15.
            return lower_bound;
        }
    }
}
