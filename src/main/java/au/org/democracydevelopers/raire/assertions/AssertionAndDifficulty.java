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


import java.beans.ConstructorProperties;

/** Simple tuple like structure */
public class AssertionAndDifficulty {
    public final Assertion assertion;
    public final double difficulty;
    public final int margin;

    @ConstructorProperties({"assertion","difficulty","margin"})
    public AssertionAndDifficulty(Assertion assertion, double difficulty, int margin) {
        this.assertion = assertion;
        this.difficulty = difficulty;
        this.margin = margin;
    }
}
