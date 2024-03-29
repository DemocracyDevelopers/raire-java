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

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.IntStream;

/** This class stores the set of consolidated votes cast in the contest we are generating assertions for. A
 * consolidated votes defines a ranking and the number of times that ranking appears on a vote cast in the contest. */
public class Votes {
    /** Consolidated set of votes cast in a contest. */
    public final Vote[] votes;

    /** Array, indexed by candidate number, indicating the first preference tally of each candidate in the contest. */
    private final int[] firstPreferenceVotes;

    public Votes(Vote[] votes, int numCandidates) throws RaireException {
        this.votes=votes;
        this.firstPreferenceVotes=new int[numCandidates];
        for (Vote v : votes) {
            if (v.prefs.length>0) {
                final int candidate = v.prefs[0];
                if (candidate>=numCandidates || candidate<0) throw new RaireException(new RaireError.InvalidCandidateNumber());
                this.firstPreferenceVotes[candidate]+=v.n;
            }
        }
    }

    /** Return the first preference tally for the given candidate. */
    public int firstPreferenceOnlyTally(int candidate) { return firstPreferenceVotes[candidate]; }

    /** Get the tallies for each continuing candidate in the given array (continuing), returning an array of the same
     * length and order as the continuing structure. */
    public int[] restrictedTallies(int[] continuing) {
        int[] res = new int[continuing.length];
        //HashMap<Integer,Integer> continuingMap = new HashMap<>();
        Integer[] continuingMap = new Integer[Arrays.stream(continuing).max().orElse(0)+1];
        for (int i=0;i<continuing.length;i++) continuingMap[continuing[i]]=i; // continuingMap.put(continuing[i],i);
        for (Vote v :votes) {
            Integer c = v.topSubPreferenceArray(continuingMap);
            if (c!=null) res[c]+=v.n;
        }
        return res;
    }

    /** Computes and returns the total number of votes cast in the contest. */
    public int totalVotes() {
        int res = 0;
        for (Vote v : votes) {
            res+=v.n;
        }
        return res;
    }

    /** Returns the total number of candidates in the contest. */
    public int numCandidates() { return firstPreferenceVotes.length; }

    /** Tabulates the outcome of the IRV election, returning the outcome as an IRVResult. The only
     * error that may arise during tabulation is a RaireError::TimeoutCheckingWinner exception. */
    public IRVResult runElection(TimeOut timeout) throws RaireException {
        IRVElectionWork work = new IRVElectionWork();
        int[] all_candidates = IntStream.range(0,numCandidates()).toArray();
        int[] possible_winners = work.findAllPossibleWinners(all_candidates,this,timeout);
        return new IRVResult(possible_winners,work.possibleEliminationOrder());
    }
}
