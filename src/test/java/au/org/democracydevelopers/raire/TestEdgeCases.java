/*
  Copyright 2024 Democracy Developers
  This is a Java re-implementation of raire-rs https://github.com/DemocracyDevelopers/raire-rs
  It attempts to copy the design, API, and naming as much as possible subject to being idiomatic and efficient Java.

  This file is part of raire-java.
  raire-java is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  raire-java is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
  You should have received a copy of the GNU Affero General Public License along with ConcreteSTV.  If not, see <https://www.gnu.org/licenses/>.

 */

package au.org.democracydevelopers.raire;

import au.org.democracydevelopers.raire.algorithm.RaireResult;
import au.org.democracydevelopers.raire.assertions.NotEliminatedBefore;
import au.org.democracydevelopers.raire.audittype.AuditType;
import au.org.democracydevelopers.raire.audittype.BallotComparisonOneOnDilutedMargin;
import au.org.democracydevelopers.raire.irv.IRVResult;
import au.org.democracydevelopers.raire.irv.Vote;
import au.org.democracydevelopers.raire.irv.Votes;
import au.org.democracydevelopers.raire.pruning.TrimAlgorithm;
import au.org.democracydevelopers.raire.time.TimeOut;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class TestEdgeCases {

    @Test
    void test_zero_candidates() {
        RaireProblem problem = new RaireProblem(new HashMap<>(),new Vote[0],0,null, new BallotComparisonOneOnDilutedMargin(13500),null,null,null);
        RaireError error = problem.solve().solution.Err;
        assertNotNull(error);
        assertEquals(RaireError.InvalidNumberOfCandidates.class,error.getClass());
    }

    @Test
    void test_one_candidate() {
        RaireProblem problem = new RaireProblem(new HashMap<>(),new Vote[0],1,null, new BallotComparisonOneOnDilutedMargin(13500),null,null,null);
        RaireResult result = problem.solve().solution.Ok;
        assertNotNull(result);
        assertEquals(0,result.winner);
    }

}
