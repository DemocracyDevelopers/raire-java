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

import au.org.democracydevelopers.raire.irv.Vote;
import au.org.democracydevelopers.raire.util.VoteConsolidator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestVoteConsolidator {

    @Test
    void testWithoutNames() {
        VoteConsolidator consolidator=new VoteConsolidator();
        assertEquals(0,consolidator.getVotes().length);
        consolidator.addVote(new int[]{0, 1});
        assertEquals(1,consolidator.getVotes().length);
        consolidator.addVote(new int[]{0, 1});
        Vote[] shouldBe1VoteMultiplicity2 = consolidator.getVotes();
        assertEquals(1,shouldBe1VoteMultiplicity2.length);
        assertEquals(2,shouldBe1VoteMultiplicity2[0].n);
        assertArrayEquals(new int[]{0, 1},shouldBe1VoteMultiplicity2[0].prefs);
        consolidator.addVote(new int[]{2});
        Vote[] shouldBe2VotesMultiplicities1and2 = consolidator.getVotes();
        assertEquals(2,shouldBe2VotesMultiplicities1and2.length);
        // order is not guaranteed. Work out which is which.
        Vote vote01 = shouldBe2VotesMultiplicities1and2[0].n==2?shouldBe2VotesMultiplicities1and2[0]:shouldBe2VotesMultiplicities1and2[1];
        Vote vote2 = shouldBe2VotesMultiplicities1and2[0].n==2?shouldBe2VotesMultiplicities1and2[1]:shouldBe2VotesMultiplicities1and2[0];
        assertEquals(2,vote01.n);
        assertArrayEquals(new int[]{0, 1},vote01.prefs);
        assertEquals(1,vote2.n);
        assertArrayEquals(new int[]{2},vote2.prefs);
    }

    @Test
    void testWithNames() {
        VoteConsolidator consolidator=new VoteConsolidator(new String[]{"A","B","C"});
        assertEquals(0,consolidator.getVotes().length);
        consolidator.addVoteNames(new String[]{"A","B"});
        assertEquals(1,consolidator.getVotes().length);
        consolidator.addVoteNames(new String[]{"A","B"});
        Vote[] shouldBe1VoteMultiplicity2 = consolidator.getVotes();
        assertEquals(1,shouldBe1VoteMultiplicity2.length);
        assertEquals(2,shouldBe1VoteMultiplicity2[0].n);
        assertArrayEquals(new int[]{0, 1},shouldBe1VoteMultiplicity2[0].prefs);
        consolidator.addVoteNames(new String[]{"C"});
        Vote[] shouldBe2VotesMultiplicities1and2 = consolidator.getVotes();
        assertEquals(2,shouldBe2VotesMultiplicities1and2.length);
        // order is not guaranteed. Work out which is which.
        Vote vote01 = shouldBe2VotesMultiplicities1and2[0].n==2?shouldBe2VotesMultiplicities1and2[0]:shouldBe2VotesMultiplicities1and2[1];
        Vote vote2 = shouldBe2VotesMultiplicities1and2[0].n==2?shouldBe2VotesMultiplicities1and2[1]:shouldBe2VotesMultiplicities1and2[0];
        assertEquals(2,vote01.n);
        assertArrayEquals(new int[]{0, 1},vote01.prefs);
        assertEquals(1,vote2.n);
        assertArrayEquals(new int[]{2},vote2.prefs);
    }
}
