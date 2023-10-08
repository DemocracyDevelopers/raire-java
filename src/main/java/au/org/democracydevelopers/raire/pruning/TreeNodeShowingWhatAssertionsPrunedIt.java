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

import au.org.democracydevelopers.raire.RaireException;
import au.org.democracydevelopers.raire.assertions.Assertion;
import au.org.democracydevelopers.raire.time.TimeOut;

import java.util.ArrayList;
import java.util.Arrays;

/** Produce a tree of reverse-elimination-order descending down until either
 * * At least one assertion prunes all subsequent orders
 * * No assertions prune any subsequent order
 *
 * One can optionally ask for an extended, which extends beyond pruned nodes if it is possible
 * for their children to be pruned. See HowFarToContinueSearchTreeWhenPruningAssertionFound for details.
 * This is useful for finding redundant assertions
 * that can be removed, at the cost of making the frontier larger.
 *
 */
public class TreeNodeShowingWhatAssertionsPrunedIt {
    public final int candidate_being_eliminated_at_this_node; // The candidate eliminated at this step.
    public int[] pruning_assertions; // if any assertions prune it, their index in the main assertion list.
    public TreeNodeShowingWhatAssertionsPrunedIt[] children; // its children, if any.
    public boolean valid; // true if this node or a child thereof is not eliminated by any assertion.


    /**
     * Create a new tree node with a given path back to the root and candidate being eliminated.
     */
    public TreeNodeShowingWhatAssertionsPrunedIt(int[] parent_elimination_order_suffix, int candidate_being_eliminated_at_this_node, ArrayList<Integer> relevant_assertions, Assertion[] all_assertions, int num_candidates, HowFarToContinueSearchTreeWhenPruningAssertionFound consider_children_of_eliminated_nodes, TimeOut timeout) throws RaireException.TimeoutTrimmingAssertions {
        this.candidate_being_eliminated_at_this_node = candidate_being_eliminated_at_this_node;
        if (timeout.quickCheckTimeout()) throw new RaireException.TimeoutTrimmingAssertions();
        final int[] elimination_order_suffix = new int[parent_elimination_order_suffix.length+1]; // candidate_being_eliminated_at_this_node prepended to parent_elimination_order_suffix
        elimination_order_suffix[0]=candidate_being_eliminated_at_this_node;
        System.arraycopy(parent_elimination_order_suffix,0,elimination_order_suffix,1,parent_elimination_order_suffix.length);
        final ArrayList<Integer> pruning_assertions = new ArrayList<>();
        final ArrayList<Integer> still_relevant_assertions = new ArrayList<>();
        for (int assertion_index:relevant_assertions) {
            switch (all_assertions[assertion_index].okEliminationOrderSuffix(elimination_order_suffix)) {
                case Contradiction: pruning_assertions.add(assertion_index); break;
                case Ok: break; // can ignore
                case NeedsMoreDetail:still_relevant_assertions.add(assertion_index); break;
            }
        }
        final ArrayList<TreeNodeShowingWhatAssertionsPrunedIt> children = new ArrayList<>();
        boolean valid = pruning_assertions.isEmpty() && still_relevant_assertions.isEmpty();
        boolean pruned_by_neb = pruning_assertions.stream().anyMatch(a->all_assertions[a].isNEB());
        if ((pruning_assertions.isEmpty()||consider_children_of_eliminated_nodes.should_continue_if_pruning_assertion_found(pruned_by_neb)) && !still_relevant_assertions.isEmpty()) {
            HowFarToContinueSearchTreeWhenPruningAssertionFound next_consider_children_of_eliminated_nodes = pruning_assertions.isEmpty()?consider_children_of_eliminated_nodes:consider_children_of_eliminated_nodes.next_level_if_pruning_assertion_found();
            for (int candidate=0;candidate<num_candidates;candidate++) {
                final int finalCandidate = candidate;
                if (Arrays.stream(elimination_order_suffix).noneMatch(c->c==finalCandidate)) { // candidate has not already been eliminated.
                    TreeNodeShowingWhatAssertionsPrunedIt child = new TreeNodeShowingWhatAssertionsPrunedIt(elimination_order_suffix,candidate,still_relevant_assertions,all_assertions,num_candidates,next_consider_children_of_eliminated_nodes,timeout);
                    if (child.valid) {
                        if (pruning_assertions.isEmpty()) valid=true;
                        else  {// we were continuing searching beyond a pruned branch. There is no point doing this.
                            children.clear();
                            break;
                        }
                    }
                    children.add(child);
                }
            }
        }
        this.valid=valid;
        this.children=children.toArray(new TreeNodeShowingWhatAssertionsPrunedIt[children.size()]);
        this.pruning_assertions=pruning_assertions.stream().mapToInt(Integer::intValue).toArray();
    }
}
