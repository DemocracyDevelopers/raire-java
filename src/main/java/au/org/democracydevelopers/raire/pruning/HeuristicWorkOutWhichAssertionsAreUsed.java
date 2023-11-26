/*
  Copyright 2023 Democracy Developers
  This is a Java re-implementation of raire-rs https://github.com/DemocracyDevelopers/raire-rs
  It attempts to copy the design, API, and naming as much as possible subject to being idiomatic and efficient Java.

  This file is part of raire-java.
  raire-java is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  raire-java is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
  You should have received a copy of the GNU Affero General Public License along with ConcreteSTV.  If not, see <https://www.gnu.org/licenses/>.

 */

package au.org.democracydevelopers.raire.pruning;

import au.org.democracydevelopers.raire.RaireError;
import au.org.democracydevelopers.raire.RaireException;
import au.org.democracydevelopers.raire.assertions.RaireAssertion;
import au.org.democracydevelopers.raire.assertions.AssertionAndDifficulty;
import au.org.democracydevelopers.raire.assertions.NotEliminatedBefore;
import au.org.democracydevelopers.raire.assertions.NotEliminatedNext;
import au.org.democracydevelopers.raire.time.TimeOut;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A pretty simple method of computing which assertions are used which may not always
 * be optimal, but is fast, and, in practice, has turned out to be optimal for every case
 * I tried it on.
 *
 * The general problem can be converted to a problem of selection at least one of a combination
 * of expressions. The heuristic is a first pass choosing ones where there is no choice, and
 * a second pass of choosing arbitrarily amongst the remaining ones where prior choices have
 * not solved it.
 */
public class HeuristicWorkOutWhichAssertionsAreUsed {
    private final BitSet assertions_used=new BitSet();

    private boolean uses(int index) { return assertions_used.get(index); }

    /** Some (most) nodes have exactly one assertion. Assign these assertions, as they MUST be used. */
    private void add_tree_forced(TreeNodeShowingWhatAssertionsPrunedIt node) {
        if (node.pruning_assertions.length>0) {
            if (node.children.length==0 && node.pruning_assertions.length==1) { // must be used
                assertions_used.set(node.pruning_assertions[0]);
            }
        } else {
            for (TreeNodeShowingWhatAssertionsPrunedIt child : node.children) add_tree_forced(child);
        }
    }
    /** See if a node is already eliminated by the assertions marked as being used. */
    private boolean node_already_eliminated(TreeNodeShowingWhatAssertionsPrunedIt node) {
        if (Arrays.stream(node.pruning_assertions).anyMatch(this::uses)) return true; // one of the assertions eliminates the node.
        // now check to see if all the children are eliminated.
        return node.children.length!=0 && Arrays.stream(node.children).allMatch(this::node_already_eliminated);
    }
    private void add_tree_second_pass(TreeNodeShowingWhatAssertionsPrunedIt node, TimeOut timeout) throws RaireException {
        if (timeout.quickCheckTimeout()) throw new RaireException(new RaireError.TimeoutTrimmingAssertions());
        if (node.pruning_assertions.length>0) {
            if (!node_already_eliminated(node)) {
                assertions_used.set(node.pruning_assertions[0]);
            }
        } else {
            for (TreeNodeShowingWhatAssertionsPrunedIt child : node.children) add_tree_second_pass(child,timeout);
        }
    }

    /** Sort the assertions in a human sensible manner, and then trim them.
     *
     * Note that if a timeout error is produced, the assertions array will be sorted but otherwise unchanged
     * from the original call.
     *
     * The algorithm is described in [AssertionTrimmingAlgorithm.md](https://github.com/DemocracyDevelopers/raire-rs/blob/main/raire/AssertionTrimmingAlgorithm.md)
     */
    public static void order_assertions_and_remove_unnecessary(ArrayList<AssertionAndDifficulty> assertions,int winner,int num_candidates,TrimAlgorithm trim_algorithm,TimeOut timeout) throws RaireException {
        // sort all NEBs before NENs,
        // sort NENs by length
        // ties - sort by winner, then loser, then continuing
        assertions.sort((o1, o2) -> {
            if (o1.assertion instanceof NotEliminatedBefore) {
                if (o2.assertion instanceof NotEliminatedBefore) {
                    final NotEliminatedBefore neb1 = (NotEliminatedBefore)o1.assertion;
                    final NotEliminatedBefore neb2 = (NotEliminatedBefore)o2.assertion;
                    int d1 = neb1.winner-neb2.winner;
                    if (d1!=0) return d1;
                    return neb1.loser-neb2.loser;
                } else return -1; // o1 is NEB, o2 is NEN, o1<o2.
            } else {
                if (o2.assertion instanceof NotEliminatedNext) {
                    final NotEliminatedNext neb1 = (NotEliminatedNext)o1.assertion;
                    final NotEliminatedNext neb2 = (NotEliminatedNext)o2.assertion;
                    int d0 = neb1.continuing.length-neb2.continuing.length;
                    if (d0!=0) return d0;
                    int d1 = neb1.winner-neb2.winner;
                    if (d1!=0) return d1;
                    int d2 = neb1.loser-neb2.loser;
                    if (d2!=0) return d2;
                    return Arrays.compare(neb1.continuing,neb2.continuing);
                } else return 1; // o1 is NEN, o2 is NEB, o1>o2.
            }
        });
        HowFarToContinueSearchTreeWhenPruningAssertionFound consider_children_of_eliminated_nodes=null;
        switch (trim_algorithm) {
            case None:
                return;
            case MinimizeTree:
                consider_children_of_eliminated_nodes=HowFarToContinueSearchTreeWhenPruningAssertionFound.StopImmediately;
                break;
            case MinimizeAssertions:
                consider_children_of_eliminated_nodes=HowFarToContinueSearchTreeWhenPruningAssertionFound.StopOnNEB;
                break;
        }
        // do the actual trimming
        RaireAssertion[] all_assertions = assertions.stream().map(a->a.assertion).toArray(RaireAssertion[]::new);
        ArrayList<Integer> all_assertion_indices = IntStream.range(0, all_assertions.length).boxed().collect(Collectors.toCollection(ArrayList::new)); // 0 to all_assertions.length
        HeuristicWorkOutWhichAssertionsAreUsed find_used = new HeuristicWorkOutWhichAssertionsAreUsed();
        ArrayList<TreeNodeShowingWhatAssertionsPrunedIt> trees=new ArrayList<>();
        for (int candidate=0;candidate<num_candidates;candidate++) { // create trees and do first pass
            if (candidate!=winner) {
                TreeNodeShowingWhatAssertionsPrunedIt tree = new TreeNodeShowingWhatAssertionsPrunedIt(new int[0],candidate,all_assertion_indices,all_assertions,num_candidates,consider_children_of_eliminated_nodes,timeout);
                if (tree.valid) throw new RaireException(new RaireError.InternalErrorDidntRuleOutLoser());
                find_used.add_tree_forced(tree);
                trees.add(tree);
            }
        }
        // do second pass
        for (TreeNodeShowingWhatAssertionsPrunedIt tree:trees) find_used.add_tree_second_pass(tree,timeout);
        AssertionAndDifficulty[] copy = assertions.toArray(AssertionAndDifficulty[]::new);
        assertions.clear();
        for (int i=0;i<copy.length;i++) {
            if (find_used.uses(i)) assertions.add(copy[i]);
        }
    }
}
