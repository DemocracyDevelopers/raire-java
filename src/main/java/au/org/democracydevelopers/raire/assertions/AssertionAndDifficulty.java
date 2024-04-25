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


import com.fasterxml.jackson.annotation.JsonInclude;

import java.beans.ConstructorProperties;
import java.util.Map;

/** Simple tuple like structure that stores an Assertion alongside its difficulty and margin.
 * The difficulty of an assertion is a measure that reflects how much auditing effort is required
 * to check the assertion in an RLA. We expect that assertions with a higher difficulty will require
 * more ballot samples to check. A range of possible difficulty measures can be used by RAIRE (see
 * the AuditType interface and its implementations). */
public class AssertionAndDifficulty {
    public final Assertion assertion;

    /** A measure of how hard this assertion will be to audit. Assertions with a higher difficulty
     * will require more ballot samples to check in an audit. */
    public final double difficulty;

    /** Each assertion has a winner, a loser, and a context which determines whether a given
     * votes falls into the winner's pile or the loser's. The margin of the assertion is equal to
     * the difference in these tallies. */
    public final int margin;

    /** This field is not used by raire-java for computing assertions,
     * may be useful information for assertion visualisation or information that election administrators would like
     * to associate with this specific assertion. This field will be created as null by raire-java for efficiency
     * reasons (rather than containing an empty object). If you want to use it, create an instance with
     * a non-null value using the constructor. This is useful primarily to people using this data type
     * in external software to annotate a set of assertions being verified. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public final Map<String,Object> status;

    /** Backwards compatability constructor not containing status. Use if you don't want any status. */
    public AssertionAndDifficulty(Assertion assertion, double difficulty, int margin) {
        this.assertion = assertion;
        this.difficulty = difficulty;
        this.margin = margin;
        this.status = null;
    }
    /** Use this constructor if status is required. */
    @ConstructorProperties({"assertion","difficulty","margin","status"})
    public AssertionAndDifficulty(Assertion assertion, double difficulty, int margin,Map<String,Object> status) {
        this.assertion = assertion;
        this.difficulty = difficulty;
        this.margin = margin;
        this.status = status;
    }
}
