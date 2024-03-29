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

import java.beans.ConstructorProperties;

/** A comparison where the difficulty = 1/(diluted margin^2).
   Useful for Ballot Polling audits. */
public class BallotComparisonOneOnDilutedMarginSquared implements AuditType {
    /** The total number of ballots in the auditing universe of the contest we are generating assertions for. */
    public final int total_auditable_ballots;

    @ConstructorProperties("total_auditable_ballots")
    public BallotComparisonOneOnDilutedMarginSquared(int total_auditable_ballots) {
        this.total_auditable_ballots = total_auditable_ballots;
    }

    @Override
    public double difficulty(int lowestTallyWinner, int highestTallyLoser) {
        if (lowestTallyWinner<=highestTallyLoser) return Double.POSITIVE_INFINITY;
        else {
            final double margin = lowestTallyWinner-highestTallyLoser;
            final double reciprocalDilutedMargin = ((double)total_auditable_ballots)/margin;
            return reciprocalDilutedMargin*reciprocalDilutedMargin;
        }
    }
}
