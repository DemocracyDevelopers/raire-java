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
     * given time limit, a given set of Votes (votes), and a set of candidates assumed to be continuing.  */
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
            for (int i=0;i<continuing.length;i++) if (min_tally==tallies[i]) { // this is a plausible candidate to exclude. There may be a tie in which case there are multiple options. Try them all.
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
            }
            final int[] winners = winnerSet.stream().mapToInt(Integer::intValue).toArray();
            winner_given_continuing_candidates.put(continuing_as_bitset,winners);
            return winners;
        }
    }

    /** Find one of (possibly) multiple elimination orders */
    public int[] possibleEliminationOrder() {
        return elimination_order.stream().mapToInt(Integer::intValue).toArray();
    }

}

