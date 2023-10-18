/*
  Copyright 2023 Democracy Developers
  This is a Java re-implementation of raire-rs https://github.com/DemocracyDevelopers/raire-rs
  It attempts to copy the design, API, and naming as much as possible subject to being idiomatic and efficient Java.

  This file is part of raire-java.
  raire-java is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  raire-java is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
  You should have received a copy of the GNU Affero General Public License along with ConcreteSTV.  If not, see <https://www.gnu.org/licenses/>.

 */

package au.org.democracydevelopers.raire;

/**
 * Everything that could go wrong in Raire. Typically this will be returned as a thrown RaireException with this as its argument.
 *
 * It is implemented as a class rather than an Exception hierarchy to facilitate detailed error serialization.
 **/
public abstract class RaireError {
    public static class InvalidTimeout extends RaireError {}
    public static class InvalidCandidateNumber extends RaireError {}
    public static class TimeoutCheckingWinner extends RaireError {}
    public static class TimeoutFindingAssertions extends RaireError { final double difficultyAtTimeOfStopping;
        public TimeoutFindingAssertions(double difficultyAtTimeOfStopping) {
            this.difficultyAtTimeOfStopping = difficultyAtTimeOfStopping;
        }
    }
    public static class TimeoutTrimmingAssertions extends RaireError {}
    public static class TiedWinners extends RaireError { final int[] expected;
        public TiedWinners(int[] expected) {
            this.expected = expected;
        }
    }
    public static class WrongWinner extends RaireError { final int[] expected;
        public WrongWinner(int[] expected) {
            this.expected = expected;
        }
    }
    public static class CouldNotRuleOut extends RaireError { final int[] eliminationOrder;
        public CouldNotRuleOut(int[] eliminationOrder) {
            this.eliminationOrder = eliminationOrder;
        }
    }
    public static class InternalErrorRuledOutWinner extends RaireError {}
    public static class InternalErrorDidntRuleOutLoser extends RaireError {}
    public static class InternalErrorTrimming extends RaireError {}

}
