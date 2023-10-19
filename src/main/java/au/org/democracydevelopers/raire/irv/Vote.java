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

public class Vote {
    public final int n; // The number of voters who voted this way
    public final int[] prefs; //  prefs[0] is the first preferenced candidate.

    @ConstructorProperties({"n","prefs"})
    public Vote(int n, int[] prefs) {
        this.n = n;
        this.prefs = prefs;
    }

    /** find the highest preferenced candidate amongst the continuing candidates
     return null or a candidate index. */
    public Integer topPreference(BitSet continuing) {
        for (final int c : prefs) {
            if (continuing.get(c)) { return c; }
        }
        return null;
    }
    /** find the highest preferenced candidate amongst the continuing candidates
      return null or the argument of the hashmap for the key of the continuing candidate. */
    public Integer topSubPreference(HashMap<Integer,Integer> continuing) {
        for (final int c : prefs) {
            Integer found = continuing.get(c);
            if (found!=null) { return found; }
        }
        return null;
    }
}
