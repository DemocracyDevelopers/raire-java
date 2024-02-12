/*
  Copyright 2023 Democracy Developers
  This is a Java re-implementation of raire-rs https://github.com/DemocracyDevelopers/raire-rs
  It attempts to copy the design, API, and naming as much as possible subject to being idiomatic and efficient Java.

  This file is part of raire-java.
  raire-java is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  raire-java is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
  You should have received a copy of the GNU Affero General Public License along with ConcreteSTV.  If not, see <https://www.gnu.org/licenses/>.

*/

package au.org.democracydevelopers.raire.assertions;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;
import java.util.Arrays;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NotEliminatedBefore.class, name = "NEB"),
        @JsonSubTypes.Type(value = NotEliminatedNext.class, name = "NEN")
})

/** RAIRE generates a set of assertions for a given contest. The different types of assertion
 * that RAIRE can generate are defined as subclasses of this base Assertion class. */
public abstract class Assertion {

    /** Returns true if the assertion is a NotEliminatedBefore assertion. When filtering
     * out redundant assertions in the final stage of computation, some trimming algorithms
     * will need to know whether an assertion is an NEB or not. */
    @JsonIgnore
    public abstract boolean isNEB();

    /** Given an elimination order suffix (a sequence of candidates that represents the ending of a
     * set of possible elimination orders), this method checks whether this assertion rules out none, some,
     * or all elimination orders that end in the suffix. Note that a suffix of [3, 2] represents the
     * set of elimination orders that end with candidate '3' as the runner-up and '2' as the winner. */
    public abstract EffectOfAssertionOnEliminationOrderSuffix okEliminationOrderSuffix(int[] eliminationOrderSuffix);

    /** This method is used for testing purposes. Given an elimination order suffix,
      * let it through if it is allowed,
      * block if it is contradicted,
      * expand if it is not enough information.
     * Returns an array of elimination order suffixes.
     *
     * This is not an efficient thing to do; this is only useful for consistency checks in tests on tiny data sets.
      */
    public int[][] allowed_suffixes(int[] eliminationOrderSuffix, int numCandidates) {
        switch (okEliminationOrderSuffix(eliminationOrderSuffix)) {
            case Contradiction: return new int[0][];
            case Ok: return new int[][]{eliminationOrderSuffix};
            case NeedsMoreDetail: // needs to expand
                ArrayList<int []> res = new ArrayList<>();
                for (int c=0;c<numCandidates;c++) {
                    final int candidate = c;
                    if (Arrays.stream(eliminationOrderSuffix).noneMatch(e->e==candidate)) { // if candidate is not in eliminationOrderSuffix
                        final int[] v = new int[eliminationOrderSuffix.length+1];
                        v[0]=c;
                        System.arraycopy(eliminationOrderSuffix,0,v,1,eliminationOrderSuffix.length);
                        // v is now c prepended to eliminationOrderSuffix
                        final int[][] sub = allowed_suffixes(v,numCandidates);
                        res.addAll(Arrays.asList(sub));
                    }
                }
                return res.toArray(new int[res.size()][]);
        }
        throw new RuntimeException("Not caught by any element of switch");
    }
}
