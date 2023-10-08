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

import au.org.democracydevelopers.raire.assertions.Assertion;
import au.org.democracydevelopers.raire.assertions.NotEliminatedBefore;
import au.org.democracydevelopers.raire.assertions.NotEliminatedNext;
import au.org.democracydevelopers.raire.pruning.HowFarToContinueSearchTreeWhenPruningAssertionFound;
import au.org.democracydevelopers.raire.pruning.TreeNodeShowingWhatAssertionsPrunedIt;
import au.org.democracydevelopers.raire.time.TimeOut;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This tests the HowFarToContinueSearchTreeWhenPruningAssertionFound creation.
 *
 * This matches the tests in raire-rs in src/tree_showing_what_assertions_pruned_leaves.rs
 */
public class TestPruningTreeCreation {
    /// Get the assertions listed in "A guide to RAIRE".
    Assertion[] raire_guide_assertions() {
        return new Assertion[]{
                new NotEliminatedNext(0,1,new int[]{0,1,2,3}),
                new NotEliminatedNext(0,3,new int[]{0,2,3}),
                new NotEliminatedNext(2,0,new int[]{0,2}),
                new NotEliminatedNext(2,3,new int[]{0,2,3}),
                new NotEliminatedBefore(2,1),
                new NotEliminatedNext(0,3,new int[]{0,3}),
        };
    }

    @Test
    void tree_creation_correct() throws RaireException.TimeoutTrimmingAssertions {
        Assertion[] all_assertions = raire_guide_assertions();
        ArrayList<Integer> relevant_assertions = IntStream.range(0, all_assertions.length).boxed().collect(Collectors.toCollection(ArrayList::new)); // 0 to all_assertions.length
        TimeOut timeout = new TimeOut(1000L,null);
        TimeOut timeout_instantly = new TimeOut(1L,null);
        assertThrows(RaireException.TimeoutTrimmingAssertions.class,()->{ // check timeout instantly actually happens
            new TreeNodeShowingWhatAssertionsPrunedIt(new int[0],0,relevant_assertions,all_assertions,4, HowFarToContinueSearchTreeWhenPruningAssertionFound.StopImmediately,timeout_instantly);
        });
        TreeNodeShowingWhatAssertionsPrunedIt tree0=new TreeNodeShowingWhatAssertionsPrunedIt(new int[0],0,relevant_assertions,all_assertions,4,HowFarToContinueSearchTreeWhenPruningAssertionFound.StopImmediately,timeout);
        TreeNodeShowingWhatAssertionsPrunedIt tree1=new TreeNodeShowingWhatAssertionsPrunedIt(new int[0],1,relevant_assertions,all_assertions,4,HowFarToContinueSearchTreeWhenPruningAssertionFound.StopImmediately,timeout);
        TreeNodeShowingWhatAssertionsPrunedIt tree2=new TreeNodeShowingWhatAssertionsPrunedIt(new int[0],2,relevant_assertions,all_assertions,4,HowFarToContinueSearchTreeWhenPruningAssertionFound.StopImmediately,timeout);
        TreeNodeShowingWhatAssertionsPrunedIt tree3=new TreeNodeShowingWhatAssertionsPrunedIt(new int[0],3,relevant_assertions,all_assertions,4,HowFarToContinueSearchTreeWhenPruningAssertionFound.StopImmediately,timeout);
        // check tree0 (candidate 0 elimination)
        assertFalse(tree0.valid);
        assertEquals(3,tree0.children.length);
        assertArrayEquals(new int[]{4},tree0.children[0].pruning_assertions);
        assertArrayEquals(new int[]{2},tree0.children[1].pruning_assertions);
        assertArrayEquals(new int[]{},tree0.children[2].pruning_assertions);
        assertEquals(2,tree0.children[2].children.length);
        assertArrayEquals(new int[]{4},tree0.children[2].children[0].pruning_assertions);
        assertArrayEquals(new int[]{3},tree0.children[2].children[1].pruning_assertions);
        // check tree1
        assertFalse(tree1.valid);
        assertArrayEquals(new int[]{4},tree1.pruning_assertions);
        // check tree2
        assertTrue(tree2.valid);// candidate 2 won.
        // check tree3
        assertFalse(tree3.valid);
        assertEquals(3,tree3.children.length);
        assertArrayEquals(new int[]{5},tree3.children[0].pruning_assertions);
        assertArrayEquals(new int[]{4},tree3.children[1].pruning_assertions);
        assertArrayEquals(new int[]{},tree3.children[2].pruning_assertions);
        assertEquals(2,tree3.children[2].children.length);
        assertArrayEquals(new int[]{1},tree3.children[2].children[0].pruning_assertions);
        assertArrayEquals(new int[]{},tree3.children[2].children[1].pruning_assertions);
        assertEquals(1,tree3.children[2].children[1].children.length);
        assertArrayEquals(new int[]{0},tree3.children[2].children[1].children[0].pruning_assertions);
    }
}
