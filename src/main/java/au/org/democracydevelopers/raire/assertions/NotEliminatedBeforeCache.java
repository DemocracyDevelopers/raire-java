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

import au.org.democracydevelopers.raire.audittype.AuditType;
import au.org.democracydevelopers.raire.irv.Votes;

public class NotEliminatedBeforeCache {
    public final DifficultyAndMargin[][] cache;

    public NotEliminatedBeforeCache(Votes votes, AuditType audit) {
        this.cache = new DifficultyAndMargin[votes.numCandidates()][votes.numCandidates()];
        for (int winner=0;winner<votes.numCandidates();winner++) {
            for (int loser=0;loser<votes.numCandidates();loser++) {
                cache[winner][loser] = winner==loser?new DifficultyAndMargin(Double.POSITIVE_INFINITY,0) : (new NotEliminatedBefore(winner,loser)).difficulty(votes,audit);
            }
        }
    }

    public DifficultyAndMargin difficulty(NotEliminatedBefore entry) { return cache[entry.winner][entry.loser]; }
}
