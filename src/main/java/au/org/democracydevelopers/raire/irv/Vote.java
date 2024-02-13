/*
  Copyright 2023 Democracy Developers
  This is a Java re-implementation of raire-rs https://github.com/DemocracyDevelopers/raire-rs
  It attempts to copy the design, API, and naming as much as possible subject to being idiomatic and efficient Java.

  This file is part of raire-java.
  raire-java is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  raire-java is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
  You should have received a copy of the GNU Affero General Public License along with ConcreteSTV.  If not, see <https://www.gnu.org/licenses/>.

 */

package au.org.democracydevelopers.raire.irv;

import java.beans.ConstructorProperties;
import java.util.BitSet;
import java.util.HashMap;

/** RAIRE operates on a consolidated collection of votes cast in a given contest. This consolidated set of
 * votes takes each unique ranking that appears on a vote in the contest, and counts the number of votes with that
 * ranking. A 'Vote' is now defined by a ranking, and the number of times that ranking appears on a vote. This
 * consolidation means that RAIRE can be more efficient by iterating over a smaller set of votes than if we
 * considered each ballot individually. */
public class Vote {
    /** The number of votes that expressed the ranking 'prefs' on their ballot. */
    public final int n;

    /** A preference ranking. Note that prefs[0] denotes the first (highest) ranked candidate. */
    public final int[] prefs;

    @ConstructorProperties({"n","prefs"})
    public Vote(int n, int[] prefs) {
        this.n = n;
        this.prefs = prefs;
    }

    /** Find the highest preferenced candidate on this vote amongst the given set of continuing candidates.
     * return null or a candidate index. */
    public Integer topPreference(BitSet continuing) {
        for (final int c : prefs) {
            if (continuing.get(c)) { return c; }
        }
        return null;
    }
    /** Find the highest preferenced candidate on this vote amongst the given set of continuing candidates.
     * return null or the argument of the hashmap for the key of the continuing candidate. */
    public Integer topSubPreference(HashMap<Integer,Integer> continuing) {
        for (final int c : prefs) {
            Integer found = continuing.get(c);
            if (found!=null) { return found; }
        }
        return null;
    }
    /** Find the highest preferenced candidate on this vote amongst the given set of continuing candidates.
     * return null or the argument of the hashmap for the key of the continuing candidate. */
    public Integer topSubPreferenceArray(Integer[] continuing) {
        for (final int c : prefs) {
            if (c<continuing.length) {
                Integer found = continuing[c];
                if (found!=null) { return found; }
            }
        }
        return null;
    }
}
