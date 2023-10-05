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

import java.util.HashMap;

public class Votes {
    public final Vote[] votes;
    private final int[] firstPreferenceVotes;

    public Votes(Vote[] votes, int numCandidates) {
        this.votes=votes;
        this.firstPreferenceVotes=new int[numCandidates];
        for (Vote v : votes) {
            if (v.prefs.length>0) this.firstPreferenceVotes[v.prefs[0]]+=v.n;
        }
    }

    public int firstPreferenceOnlyTally(int candidate) { return firstPreferenceVotes[candidate]; }

    /// Get the tallies for continuing candidates, returning a vector of the same length and order as the continuing structure
    public int[] restrictedTallies(int[] continuing) {
        int[] res = new int[continuing.length];
        HashMap<Integer,Integer> continuingMap = new HashMap<>();
        for (int i=0;i<continuing.length;i++) continuingMap.put(continuing[i],i);
        for (Vote v :votes) {
            Integer c = v.topSubPreference(continuingMap);
            if (c!=null) res[c]+=v.n;
        }
        return res;
    }

    public int totalVotes() {
        int res = 0;
        for (Vote v : votes) {
            res+=v.n;
        }
        return res;
    }

    public int numCandidates() { return firstPreferenceVotes.length; }

/* TODO
    /// only possible error is RaireError::TimeoutCheckingWinner
    pub fn run_election(&self,timeout:&mut TimeOut) -> Result<IRVResult,RaireError> {
        let mut work = IRVElectionWork{ winner_given_continuing_candidates: Default::default(), elimination_order: vec![] };
        let all_candidates : Vec<CandidateIndex> = (0..self.num_candidates()).into_iter().map(|c|CandidateIndex(c)).collect();
        let possible_winners = work.find_all_possible_winners(all_candidates,&self,timeout)?;
        Ok(IRVResult{ possible_winners, elimination_order: work.elimination_order })
    }
*/
}
