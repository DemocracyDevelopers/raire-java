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

import java.beans.ConstructorProperties;
import java.util.stream.IntStream;


/** A NotEliminatedBefore assertion (or NEB) says that a candidate _winner_ will always have
 * a higher tally than a candidate _loser_. What this means is that the minimum possible tally
 * that _winner_ will have at any stage of tabulation is greater than the maximum possible
 * tally _loser_ can ever achieve. For more detail on NEB assertions, refer to the Guide to RAIRE.*/
public class NotEliminatedBefore extends Assertion {
    public final int winner;
    public final int loser;

    @ConstructorProperties({"winner","loser"})
    public NotEliminatedBefore(int winner, int loser) {
        this.winner = winner;
        this.loser = loser;
    }


    @Override
    public boolean equals(Object other) {
        if (other instanceof NotEliminatedBefore) {
            final NotEliminatedBefore o = (NotEliminatedBefore) other;
            return o.winner==winner && o.loser==loser;
        } else { return false; }
    }

    @Override
    public boolean isNEB() {
        return true;
    }

    @Override
    public EffectOfAssertionOnEliminationOrderSuffix okEliminationOrderSuffix(int[] eliminationOrderSuffix) {
        for (int i=eliminationOrderSuffix.length-1;i>=0;i--) {
            int c = eliminationOrderSuffix[i]; // iterate in reverse order over eliminationOrderSuffix
            if (c==winner) return EffectOfAssertionOnEliminationOrderSuffix.Ok; // winner is after loser
            else if (c==loser) return EffectOfAssertionOnEliminationOrderSuffix.Contradiction; // loser is after winner
        }
        return EffectOfAssertionOnEliminationOrderSuffix.NeedsMoreDetail; // no information on relative order
    }

    /** Compute and return the difficulty estimate associated with this assertion. This method
     * computes the minimum tally of the assertion's winner (its first preference tally) and the
     * maximum tally of the assertion's loser, according to the given set of Votes (votes). This
     * maximum tally contains all votes that preference the loser higher than the winner, or on
     * which the loser appears and the winner does not. The given AuditType, audit, defines the
     * chosen method of computing assertion difficulty given these winner and loser tallies.*/
    public DifficultyAndMargin difficulty(Votes votes, AuditType audit) {
        int tallyWinner = votes.firstPreferenceOnlyTally(winner);
        int[] tallies = votes.restrictedTallies(new int[]{winner,loser});
        int tallyLoser = tallies[1];
        double difficulty = audit.difficulty(tallyWinner, tallyLoser); // active paper count = tally_winner+tally_loser for historical reenactment
        return new DifficultyAndMargin(difficulty,tallyWinner>=tallyLoser? tallyWinner-tallyLoser : 0);
    }

    /**
     * Find the NEB assertion that best rules out the given candidate being the next eliminated, given that
     * candidatesLaterInPi are the other continuing candidates.
     * @return null or an assertion with an associated (finite) difficulty.
     */
    public static AssertionAndDifficulty findBestAssertion(int candidate, int[] candidatesLaterInPi, Votes votes, AuditType audit) {
        double bestDifficulty = Double.MAX_VALUE;
        NotEliminatedBefore bestAssertion =null;
        int bestMargin = 0;
        for (int altC=0;altC<votes.numCandidates();altC++) if (altC!=candidate) {
            int finalAltC = altC;
            final NotEliminatedBefore contest = IntStream.of(candidatesLaterInPi).anyMatch(x -> x == finalAltC)?
                    // consider WO(c,c′): Assertion that c beats c′ ∈ π, where c′ != c appears later in π
                    new NotEliminatedBefore(candidate,altC):
                    // consider WO(c′′,c): Assertion that c′′ ∈ C\π beats c in a winner-only audit with winner c′′ and loser c
                    new NotEliminatedBefore(altC,candidate);
            DifficultyAndMargin dam = contest.difficulty(votes, audit);
            if (dam.difficulty<bestDifficulty) {
                bestDifficulty=dam.difficulty;
                bestAssertion=contest;
                bestMargin=dam.margin;
            }
        }
        if (bestAssertion!=null) {
            return new AssertionAndDifficulty(bestAssertion,bestDifficulty,bestMargin);
        } else {
            return null;
        }
    }

    /**
     * Find the NEB assertion that best rules out the given candidate being the next eliminated, given that
     * candidatesLaterInPi are the other continuing candidates.
     * @return null or an assertion with an associated (finite) difficulty.
     */
    public static AssertionAndDifficulty findBestAssertionUsingCache(int candidate, int[] candidatesLaterInPi, Votes votes, NotEliminatedBeforeCache cache) {
        double bestDifficulty = Double.MAX_VALUE;
        NotEliminatedBefore bestAssertion =null;
        int bestMargin = 0;
        for (int altC=0;altC<votes.numCandidates();altC++) if (altC!=candidate) {
            int finalAltC = altC;
            final NotEliminatedBefore contest = IntStream.of(candidatesLaterInPi).anyMatch(x -> x == finalAltC)?
                    // consider WO(c,c′): Assertion that c beats c′ ∈ π, where c′ != c appears later in π
                    new NotEliminatedBefore(candidate,altC):
                    // consider WO(c′′,c): Assertion that c′′ ∈ C\π beats c in a winner-only audit with winner c′′ and loser c
                    new NotEliminatedBefore(altC,candidate);
            DifficultyAndMargin dam = cache.difficulty(contest);
            if (dam.difficulty<bestDifficulty) {
                bestDifficulty=dam.difficulty;
                bestAssertion=contest;
                bestMargin=dam.margin;
            }
        }
        if (bestAssertion!=null) {
            return new AssertionAndDifficulty(bestAssertion,bestDifficulty,bestMargin);
        } else {
            return null;
        }
    }

}
