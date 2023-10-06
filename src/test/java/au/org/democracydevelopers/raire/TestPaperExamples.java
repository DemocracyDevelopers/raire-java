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
import au.org.democracydevelopers.raire.assertions.NotEliminatedNext;
import au.org.democracydevelopers.raire.audittype.BallotComparisonMACRO;
import au.org.democracydevelopers.raire.audittype.BallotPollingBRAVO;
import au.org.democracydevelopers.raire.irv.IRVResult;
import au.org.democracydevelopers.raire.irv.Vote;
import au.org.democracydevelopers.raire.irv.Votes;
import au.org.democracydevelopers.raire.time.TimeOut;
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

    /// Get the votes for example 12.
    Votes getVotesInExample12() {
        final int c1 = 0;
        final int c2 = 1;
        final int c3 = 2;
        final int c4 = 3;
        final Vote[] votes = new Vote[]{
                new Vote(5000, new int[]{c1,c2,c3}),
                new Vote(5000, new int[]{c1,c3,c2}),
                new Vote(5000, new int[]{c2,c3,c1}),
                new Vote(1500, new int[]{c2,c1,c3}),
                new Vote(5000, new int[]{c3,c2,c1}),
                new Vote( 500, new int[]{c3,c1,c2}),
                new Vote(5000, new int[]{c4,c1}),
        };
        return new Votes(votes, 4);
    }
    /** test the getVotesInTable1 function and the methods on the Votes object. */
    @Test
    void testVotesStructure() throws RaireException.TimeoutCheckingWinner {
        Votes votes = getVotesInTable1();
        assertEquals(60000,votes.totalVotes());
        assertEquals(26000,votes.firstPreferenceOnlyTally(0));
        assertEquals(10000,votes.firstPreferenceOnlyTally(1));
        assertEquals( 9000,votes.firstPreferenceOnlyTally(2));
        assertEquals(15000,votes.firstPreferenceOnlyTally(3));
        assertArrayEquals(new int[]{26000, 10000, 24000},votes.restrictedTallies(new int[]{0, 1, 3}));
        assertArrayEquals(new int[]{26000,30000},votes.restrictedTallies(new int[]{0, 3}));
        IRVResult result = votes.runElection(TimeOut.never());
        assertArrayEquals(new int[]{3},result.possibleWinners);
        assertArrayEquals(new int[]{2,1,0,3},result.eliminationOrder);
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

    /** Test ASNs for example 11 in the paper */
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

    /** Test ASNs for example 12 in the paper */
    @Test
    void test_example12_asns() {
        final Votes votes = getVotesInExample12();
        final BallotPollingBRAVO BRAVO_EG12 = new BallotPollingBRAVO(0.05, 27000);
        final BallotComparisonMACRO MACRO_EG12 = new BallotComparisonMACRO(0.05, 1.1, 27000);
        assertEquals(MACRO_EG12.totalAuditableBallots, votes.totalVotes());
        assertEquals(BRAVO_EG12.totalAuditableBallots, votes.totalVotes());
        { // test bravo
            NotEliminatedNext assertion1 = new NotEliminatedNext(0,1,new int[]{0,1});
            NotEliminatedNext assertion2 = new NotEliminatedNext(0,2,new int[]{0,2});
            NotEliminatedBefore assertion3 = new NotEliminatedBefore(0,3);
            NotEliminatedNext assertion4 = new NotEliminatedNext(0,2,new int[]{0,1,2});
            double asn1 = assertion1.difficulty(votes, BRAVO_EG12);
            double asn2 = assertion2.difficulty(votes, BRAVO_EG12);
            double asn3 = assertion3.difficulty(votes, BRAVO_EG12).difficulty;
            double asn4 = assertion4.difficulty(votes, BRAVO_EG12);
            System.out.println("Example 12 : ASN1="+asn1+" ASN2="+asn2+"  ASN3="+asn3+"  ASN4="+asn4);
            double asn1p = 100.0*asn1/votes.totalVotes();
            double asn2p = 100.0*asn2/votes.totalVotes();
            double asn3p = 100.0*asn3/votes.totalVotes();
            double asn4p = 100.0*asn4/votes.totalVotes();
            System.out.println("Example 12 percentages : ASN1="+asn1p+"% ASN2="+asn2p+"%  ASN3="+asn3p+"%  ASN4="+asn4p+"%");
            assertEquals(1.0,asn1p,0.1);
            assertEquals(0.5,asn2p,0.1);
            assertEquals(0.4,asn3p,0.1);
            assertEquals(0.1,asn4p,0.1);
        }
        {// ballot comparison
            NotEliminatedNext assertion1 = new NotEliminatedNext(0,1,new int[]{0,1});
            NotEliminatedNext assertion2 = new NotEliminatedNext(0,2,new int[]{0,1,2});
            NotEliminatedNext assertion3 = new NotEliminatedNext(0,2,new int[]{0,2});
            NotEliminatedBefore assertion4 = new NotEliminatedBefore(0,3);
            NotEliminatedNext assertion5a = new NotEliminatedNext(1,3,new int[]{1,3});
            NotEliminatedNext assertion5b = new NotEliminatedNext(2,3,new int[]{2,3});
            double asn1 = assertion1.difficulty(votes, MACRO_EG12);
            double asn2 = assertion2.difficulty(votes, MACRO_EG12);
            double asn3 = assertion3.difficulty(votes, MACRO_EG12);
            double asn4 = assertion4.difficulty(votes, MACRO_EG12).difficulty;
            double asn5a = assertion5a.difficulty(votes, MACRO_EG12);
            double asn5b = assertion5b.difficulty(votes, MACRO_EG12);
            System.out.println("Example 12 : ASN1="+asn1+" ASN2="+asn2+"  ASN3="+asn3+"  ASN4="+asn4+" ASN5="+asn5a+" and "+asn5b);
            double asn1p = 100.0*asn1/votes.totalVotes();
            double asn2p = 100.0*asn2/votes.totalVotes();
            double asn3p = 100.0*asn3/votes.totalVotes();
            double asn4p = 100.0*asn4/votes.totalVotes();
            double asn5ap = 100.0*asn5a/votes.totalVotes();
            double asn5bp = 100.0*asn5b/votes.totalVotes();
            System.out.println("Example 12 percentages : ASN1="+asn1p+"% ASN2="+asn2p+"%  ASN3="+asn3p+"%  ASN4="+asn4p+"%  ASN5="+asn5ap+"% and "+asn5bp+"%");
            assertEquals(0.17,asn1p,0.01);
            assertEquals(0.07,asn2p,0.01);
            assertEquals(0.11,asn3p,0.01);
            assertEquals(0.13,asn4p,0.01);
            assertEquals(0.04,asn5ap,0.01);
            assertEquals(0.04,asn5bp,0.01);

        }
    }
}
