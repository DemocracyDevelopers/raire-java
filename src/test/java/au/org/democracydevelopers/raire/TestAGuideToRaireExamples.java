/*
  Copyright 2023 Democracy Developers
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
import au.org.democracydevelopers.raire.audittype.BallotComparisonOneOnDilutedMargin;
import au.org.democracydevelopers.raire.irv.IRVResult;
import au.org.democracydevelopers.raire.irv.Vote;
import au.org.democracydevelopers.raire.irv.Votes;
import au.org.democracydevelopers.raire.pruning.TrimAlgorithm;
import au.org.democracydevelopers.raire.time.TimeOut;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestAGuideToRaireExamples {
    final static int A = 0; // Alice
    final static int B = 1; // Bob
    final static int C = 2; // Chuan
    final static int D = 3; // Diego

    /** Get the votes in Example 10 (at the time of writing), used in examples in chapter 6, "Using RAIRE to generate assertions". */
    public Votes getVotes() throws RaireException {
        Vote [] votes = new Vote[] {
                new Vote(5000,new int[]{C,B,A}),
                new Vote(1000,new int[]{B,C,D}),
                new Vote(1500,new int[]{D,A}),
                new Vote(4000,new int[]{A,D}),
                new Vote(2000,new int[]{D}),
        };
        return new Votes(votes,4);
    }

    final static BallotComparisonOneOnDilutedMargin AUDIT = new BallotComparisonOneOnDilutedMargin(13500);

    // Test the get_votes() function and the methods on the Votes object.
    @Test
    void testVotesStructure() throws RaireException {
        Votes votes = getVotes();
        assertEquals(AUDIT.total_auditable_ballots,votes.totalVotes());
        assertEquals(4000,votes.firstPreferenceOnlyTally(A));
        assertEquals(1000,votes.firstPreferenceOnlyTally(B));
        assertEquals(5000,votes.firstPreferenceOnlyTally(C));
        assertEquals(3500,votes.firstPreferenceOnlyTally(D));
        assertArrayEquals(new int[]{4000,6000,3500},votes.restrictedTallies(new int[]{A,C,D}));
        assertArrayEquals(new int[]{5500,6000},votes.restrictedTallies(new int[]{A,C}));
        IRVResult result=votes.runElection(TimeOut.never());
        assertArrayEquals(new int[]{C},result.possibleWinners);
        assertArrayEquals(new int[]{B,D,A,C},result.eliminationOrder);
    }

    double testNEB(int winner,int loser) throws RaireException {
        NotEliminatedBefore assertion = new NotEliminatedBefore(winner,loser);
        return assertion.difficulty(getVotes(),AUDIT).difficulty;
    }

    /** Check NEB assertions in table 6.1 showing that A, B and C cannot be the last candidate standing. */
    @Test
    void test_neb_assertions() throws RaireException {
        assertTrue(Double.isInfinite(testNEB(B,A)));
        assertTrue(Double.isInfinite(testNEB(C,A)));
        assertTrue(Double.isInfinite(testNEB(D,A)));
        assertTrue(Double.isInfinite(testNEB(A,B)));
        assertEquals(3.375,testNEB(C,B),0.001);
        assertTrue(Double.isInfinite(testNEB(D,B)));
        assertTrue(Double.isInfinite(testNEB(A,D)));
        assertTrue(Double.isInfinite(testNEB(B,D)));
        assertTrue(Double.isInfinite(testNEB(C,D)));
    }

    /// Test RAIRE
    @Test
    void test_raire() throws RaireException {
        Votes votes = getVotes();
        RaireResult minAssertions = new RaireResult(votes,C,AUDIT, TrimAlgorithm.MinimizeAssertions,TimeOut.never());
        assertEquals(27.0,minAssertions.difficulty,1e-6);
        assertEquals(5,minAssertions.assertions.length);
        RaireResult minTree = new RaireResult(votes,C,AUDIT, TrimAlgorithm.MinimizeTree,TimeOut.never());
        assertEquals(27.0,minTree.difficulty,1e-6);
        assertEquals(6,minTree.assertions.length);
    }
}
