/*
  Copyright 2023 Democracy Developers
  This is a Java re-implementation of raire-rs https://github.com/DemocracyDevelopers/raire-rs
  It attempts to copy the design, API, and naming as much as possible subject to being idiomatic and efficient Java.

  This file is part of raire-java.
  raire-java is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  raire-java is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
  You should have received a copy of the GNU Affero General Public License along with ConcreteSTV.  If not, see <https://www.gnu.org/licenses/>.

 */

// Test the examples given in https://arxiv.org/pdf/1903.08804.pdf

package au.org.democracydevelopers.raire;

import au.org.democracydevelopers.raire.assertions.NotEliminatedBefore;
import au.org.democracydevelopers.raire.audittype.BallotComparisonMACRO;
import au.org.democracydevelopers.raire.audittype.BallotPollingBRAVO;
import au.org.democracydevelopers.raire.irv.Vote;
import au.org.democracydevelopers.raire.irv.Votes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPaperExamples {


    /// Get the votes in table 1.
    Votes getVotesInTable1() {
        final int c1 = 0;
        final int c2 = 1;
        final int c3 = 2;
        final int c4 = 3;
        final Vote[] votes = new Vote[]{
                new Vote(4000, new int[]{c2, c3}),
                new Vote(20000, new int[]{c1}),
                new Vote(9000, new int[]{c3, c4}),
                new Vote(6000, new int[]{c2, c3, c4}),
                new Vote(15000, new int[]{c4, c1, c2}),
                new Vote(6000, new int[]{c1, c3}),
        };
        return new Votes(votes, 4);
    }

    /// Get the votes for example 9.
    Votes getVotesInExample9() {
        final int c1 = 0;
        final int c2 = 1;
        final int c3 = 2;
        final Vote[] votes = new Vote[]{
                new Vote(10000, new int[]{c1,c2, c3}),
                new Vote( 6000, new int[]{c2, c1, c3}),
                new Vote( 5999, new int[]{c3,c1, c2}),
        };
        return new Votes(votes, 3);
    }


    /** test the getVotesInTable1 function and the methods on the Votes object. */
    @Test
    void testVotesStructure() {
        Votes votes = getVotesInTable1();
        assertEquals(60000,votes.totalVotes());
        assertEquals(26000,votes.firstPreferenceOnlyTally(0));
        assertEquals(10000,votes.firstPreferenceOnlyTally(1));
        assertEquals( 9000,votes.firstPreferenceOnlyTally(2));
        assertEquals(15000,votes.firstPreferenceOnlyTally(3));
        assertArrayEquals(new int[]{26000, 10000, 24000},votes.restrictedTallies(new int[]{0, 1, 3}));
        assertArrayEquals(new int[]{26000,30000},votes.restrictedTallies(new int[]{0, 3}));
    }

    /** Test ASNs for example 10 in the paper */
    @Test
    void test_example10() {
        final Votes votes = getVotesInExample9();
        final BallotPollingBRAVO BRAVO_EG5 = new BallotPollingBRAVO(0.05,21999);
        assertEquals(BRAVO_EG5.totalAuditableBallots,votes.totalVotes());
        NotEliminatedBefore assertion1 = new NotEliminatedBefore(0,1);
        NotEliminatedBefore assertion2 = new NotEliminatedBefore(0,2);
        double asn1 = assertion1.difficulty(votes, BRAVO_EG5).difficulty;
        double asn2 = assertion2.difficulty(votes, BRAVO_EG5).difficulty;
        System.out.println("Example 10 : ASN1="+asn1+" ASN2="+asn2);
        assertEquals(135.3,asn1,0.1);
        assertEquals(135.2,asn2,0.1);
    }

    /** Test ASNs for example 10 in the paper */
    @Test
    void test_example11() {
        final Votes votes = getVotesInExample9();
        final BallotComparisonMACRO MACRO_EG5 = new BallotComparisonMACRO(0.05,1.1,21999);
        assertEquals(MACRO_EG5.totalAuditableBallots,votes.totalVotes());
        NotEliminatedBefore assertion1 = new NotEliminatedBefore(0,1);
        NotEliminatedBefore assertion2 = new NotEliminatedBefore(0,2);
        double asn1 = assertion1.difficulty(votes, MACRO_EG5).difficulty;
        double asn2 = assertion2.difficulty(votes, MACRO_EG5).difficulty;
        System.out.println("Example 11 : ASN1="+asn1+" ASN2="+asn2);
        assertEquals(36.2,asn1,0.1);
        assertEquals(36.2,asn2,0.1);
    }
}
