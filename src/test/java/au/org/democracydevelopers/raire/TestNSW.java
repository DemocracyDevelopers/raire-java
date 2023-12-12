/*
  Copyright 2023 Democracy Developers
  This is a Java re-implementation of raire-rs https://github.com/DemocracyDevelopers/raire-rs
  It attempts to copy the design, API, and naming as much as possible subject to being idiomatic and efficient Java.

  This file is part of raire-java.
  raire-java is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  raire-java is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
  You should have received a copy of the GNU Affero General Public License along with ConcreteSTV.  If not, see <https://www.gnu.org/licenses/>.

 */

// Test the same NSW data tested in raire-rs, against the difficulties computed in raire-rs.
// This is not a great test - it is testing one program written by one developer against an almost identical program written by the same developer in a different language, but it is data from a real election.
// Other tests compute against hand computed data by a different individual, which is generally a better test, but is not real election data.

package au.org.democracydevelopers.raire;

import au.org.democracydevelopers.raire.algorithm.RaireResult;
import au.org.democracydevelopers.raire.assertions.NotEliminatedBefore;
import au.org.democracydevelopers.raire.assertions.NotEliminatedNext;
import au.org.democracydevelopers.raire.audittype.BallotComparisonMACRO;
import au.org.democracydevelopers.raire.audittype.BallotPollingBRAVO;
import au.org.democracydevelopers.raire.irv.IRVResult;
import au.org.democracydevelopers.raire.irv.Vote;
import au.org.democracydevelopers.raire.irv.Votes;
import au.org.democracydevelopers.raire.pruning.TrimAlgorithm;
import au.org.democracydevelopers.raire.time.TimeOut;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestNSW {
    private final ObjectMapper mapper = new ObjectMapper();

    void testDirectory(String directory) throws Exception {
        //noinspection ConstantConditions
        for (File file : new File(directory).listFiles()) {
            String filename = file.getName();
            if (filename.endsWith(".json") && !filename.endsWith("_out.json")) {
                System.out.println("Processing "+filename);
                RaireProblem problem = mapper.readValue(file,RaireProblem.class);
                RaireSolution computedSolution = problem.solve();
                File solutionFile = new File(directory,filename.replace(".json","_out.json"));
                RaireSolution expectedSolution = mapper.readValue(solutionFile,RaireSolution.class);
                assert expectedSolution.solution.Ok != null;
                double expected_difficulty = expectedSolution.solution.Ok.difficulty;
                assert computedSolution.solution.Ok != null;
                double computed_difficulty = computedSolution.solution.Ok.difficulty;
                System.out.println("Expected difficulty for "+filename+" : "+expected_difficulty+", computed difficulty : "+computed_difficulty);
                assertEquals(expected_difficulty,computed_difficulty,0.001);
            }
        }
    }

    /** Test NSW data against raire-rs. */
    @Test
    void testNSW2021() throws Exception {
        testDirectory("Australian Examples/NSW Local Government/2021/");
    }

}
