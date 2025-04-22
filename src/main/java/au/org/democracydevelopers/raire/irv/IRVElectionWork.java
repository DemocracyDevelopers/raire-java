/*
  Copyright 2023 Democracy Developers
  This is a Java re-implementation of raire-rs https://github.com/DemocracyDevelopers/raire-rs
  It attempts to copy the design, API, and naming as much as possible subject to being idiomatic and efficient Java.

  This file is part of raire-java.
  raire-java is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  raire-java is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
  You should have received a copy of the GNU Affero General Public License along with ConcreteSTV.  If not, see <https://www.gnu.org/licenses/>.

 */

package au.org.democracydevelopers.raire.irv;

import au.org.democracydevelopers.raire.RaireError;
import au.org.democracydevelopers.raire.RaireException;
import au.org.democracydevelopers.raire.time.TimeOut;

import java.util.*;

/** Utility to find all possible winners (counting ties) of an IRV election. This class provides the
 * functionality to tabulate an IRV contest. If there are ties at any stage of the tabulation, this
 * functionality will determine all possible winners that may arise through different resolutions of those ties. */
class IRVElectionWork {
    /** One possible order in which candidates may be eliminated. */
    private final ArrayList<Integer> elimination_order=new ArrayList<>();

    /** Key is a list of continuing candidates, Value is a list of possible candidates who could win from that point. */
    private final HashMap<BitSet,int[]> winner_given_continuing_candidates=new HashMap<>();

    /** Find all possible winners, trying all options with ties. Performs IRV tabulation within the context of a
     * given time limit, a given set of Votes (votes), and a set of candidates assumed to be continuing.
     *
     * # Algorithm Complexity
     *
     *  The worst case for all possible elimination orders is n!. An example of this
     *  would be n candidates, each with one vote, just preferencing themself, in which
     *  case all n! elimination orders are plausible. This would make an algorithm
     *  that uses this n! in time complexity (worst case), which is horrible. A simple
     *  dynamic programming optimization based on the continuing candidates reduces
     *  this to 2^n, which is a little less horrible, but still occasionally problematic.
     *
     *  Of course this example is not practical as such an example could not be solved by
     *  RAIRE anyway. However, a more likely example of a few candidates with a large number
     *  of votes, and a large number of candidates each with only one vote is more plausible
     *  and causes the same problem, but is easily solvable with RAIRE using NEB assertions.
     *
     *  There are a variety of solutions to this. Stochastic evaluation - run the election
     *  a million times, with each tie resolved randomly - would do a pretty good job, but
     *  is unreasonably slow in the normal case, is hard to test, and is generally imperfect.
     *  However, it is guaranteed not too bad. I hate this idea, but it may be the best option
     *  if this turns out to be a problem in practice.
     *
     *  What this algorithm does do is to use a special case optimization. Let the candidates
     *  be ranked by tally V_i at some point, so V_i≥V_{i-1}. Compute a cumulative sum
     *  S_i = ∑_{j⩽i} V_j. Then if V_i>S_{i-1} we can say that no matter how the preferences
     *  of the votes going to candidates up to and including i-1 go, no candidate i or above
     *  will be excluded before all candidates up to and including i-1 have been excluded. Thus
     *  one can exclude all candidates up to i-1 at this point without worrying about their
     *  order. This *bulk elimination* doesn't solve all cases, but does solve the most likely
     *  problematic case of a few candidates with lots of votes and a large number of candidates
     *  with a tiny number of votes.
     *
     *  Also note that if bulk elimination is used, the example elimination order may not be exact.
     *  This is simply remedied (at gratuitous but not exponential computational cost)
     *  by just excluding one candidate from the bulk elimination - one of the ones with
     *  lowest tally. For computational reasons, bulk elimination is only tried in the case of ties.
     *
     **/
    public int[] findAllPossibleWinners(int[] continuing, Votes votes, TimeOut timeout) throws RaireException {
        if (timeout.quickCheckTimeout()) throw new RaireException(new RaireError.TimeoutCheckingWinner());
        if (continuing.length==1) {
            if (elimination_order.size()+continuing.length==votes.numCandidates()) {
                // There may be multiple elimination orders. The check above checks that we are in the path of the first depth first traversal of the tree of elimination orders.
                elimination_order.add(continuing[0]);
            }
            return continuing;
        } else {
            final BitSet continuing_as_bitset = new BitSet();
            for (int c:continuing) continuing_as_bitset.set(c);
            final int[] already_computed = winner_given_continuing_candidates.get(continuing_as_bitset);
            if (already_computed!=null) return already_computed;
            final int[] tallies = votes.restrictedTallies(continuing);
            final int min_tally = Arrays.stream(tallies).min().orElseThrow();
            final HashSet<Integer> winnerSet = new HashSet<>();
            boolean already_tried_one_option=false;
            boolean already_tried_bulk_elimination=false;
            for (int i=0;i<continuing.length;i++) if (min_tally==tallies[i]) { // this is a plausible candidate to exclude. There may be a tie in which case there are multiple options. Try them all.
                if (already_tried_one_option && !already_tried_bulk_elimination) {
                    // check to see if bulk elimination is an option. If so, don't bother trying any more candidates.
                    if (find_bulk_elimination(continuing,tallies)!=null) { break; }
                    already_tried_bulk_elimination=true;
                }
                if (elimination_order.size()+continuing.length==votes.numCandidates()) {
                    // There may be multiple elimination orders. The check above checks that we are in the path of the first depth first traversal of the tree of elimination orders.
                    elimination_order.add(continuing[i]);
                }
                int[] new_continuing = new int[continuing.length-1];
                System.arraycopy(continuing,0,new_continuing,0,i);
                System.arraycopy(continuing,i+1,new_continuing,i,new_continuing.length-i);
                for (int c:findAllPossibleWinners(new_continuing,votes,timeout)) {
                    winnerSet.add(c);
                }
                already_tried_one_option=true;
            }
            final int[] winners = winnerSet.stream().mapToInt(Integer::intValue).toArray();
            winner_given_continuing_candidates.put(continuing_as_bitset,winners);
            return winners;
        }
    }


    /** Compute a set of at least 2 candidates to eliminate, if possible, using the
        bulk elimination algorithm described in the docs for find_all_possible_winners.

        If it finds such a set, they are returned, sorted in order from smallest tally to largest tally, otherwise null.

        Note: In practice, a boolean return value would work, in which case we only need to sort tallies, which would be faster.
     */
    private static int[] find_bulk_elimination(int[] continuing,int[] tallies)  {
        class CandidateAndCount {
            int candidate;
            int count;
            CandidateAndCount(int candidate, int count) { this.candidate = candidate; this.count = count;  }
        }
        CandidateAndCount[] merged = new CandidateAndCount[tallies.length];
        for (int i=0;i<merged.length;i++) merged[i] = new CandidateAndCount(continuing[i],tallies[i]);
        Arrays.sort(merged,new Comparator<CandidateAndCount>() {
            @Override
            public int compare(CandidateAndCount a, CandidateAndCount b) {
                return a.count - b.count ;
            }
        });
        int cumulative_sum = 0;
        for (int i=0;i<merged.length;i++)  {
            if (i>1 && merged[i].count>cumulative_sum) { // can do bulk exclusion of candidates 0 inclusive to i exclusive
                int bulk_elimination[] = new int[i];
                for (int j=0;j<i;j++) bulk_elimination[j]=merged[j].candidate;
                return bulk_elimination;
            }
            cumulative_sum+=merged[i].count;
        }
        return null;
    }

    /** Find one of (possibly) multiple elimination orders */
    public int[] possibleEliminationOrder() {
        return elimination_order.stream().mapToInt(Integer::intValue).toArray();
    }

}

