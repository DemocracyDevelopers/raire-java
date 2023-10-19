/*
  Copyright 2023 Democracy Developers
  This is a Java re-implementation of raire-rs https://github.com/DemocracyDevelopers/raire-rs
  It attempts to copy the design, API, and naming as much as possible subject to being idiomatic and efficient Java.

  This file is part of raire-java.
  raire-java is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  raire-java is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
  You should have received a copy of the GNU Affero General Public License along with ConcreteSTV.  If not, see <https://www.gnu.org/licenses/>.

 */


package au.org.democracydevelopers.raire.audittype;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * An audit type is a method for determining a difficulty (higher means more difficult, infinite means impossible) for
 * a comparison of two claimed tallys.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BallotComparisonMACRO.class, name = "MACRO"),
        @JsonSubTypes.Type(value = BallotPollingBRAVO.class, name = "BRAVO"),
        @JsonSubTypes.Type(value = BallotComparisonOneOnDilutedMargin.class, name = "OneOnMargin"),
        @JsonSubTypes.Type(value = BallotComparisonOneOnDilutedMarginSquared.class, name = "OneOnMarginSq")
})
public interface AuditType {
    /**
     *
     * @param lowestTallyWinner the number of votes that the winner has
     * @param highestTallyLoser the number of votes that the loser has
     * @return a difficulty measure for the audit
     */
    double difficulty(int lowestTallyWinner,int highestTallyLoser);
}
