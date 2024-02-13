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
import java.util.Arrays;

/**
 * Assert that _winner_ beats _loser_ in an audit when all candidates other that
 * those in _remaining_ have been removed.
 *
 * In particular, this means that _winner_ can not be the next candidate eliminated.
 *
 * This assertion type is also referred to as an NEN assertion in A Guide to RAIRE.
 */
public class NotEliminatedNext extends Assertion {
    /** The winning candidate of this NotEliminatedNext assertion. */
    public final int winner;

    /** The losing candidate of this NotEliminatedNext assertion. */
    public final int loser;

    /** Each NotEliminatedNext assertion has an associated context. This context is
     * a set of candidates that we assume are 'continuing' (have not yet been eliminated).
     * All candidates not in this list are assumed to have been already eliminated.
     * Continuing candidates are sorted in ascending order of their identifier.
     *
     * This ordering makes it easy to check if two assertions are actually the same, and
     * it allows binary search for seeing if a particular candidate is in this list. */
    public final int[] continuing;


    @ConstructorProperties({"winner","loser","continuing"})
    public NotEliminatedNext(int winner, int loser, int[] continuing) {
        this.winner = winner;
        this.loser = loser;
        this.continuing = continuing.clone();
        Arrays.sort(this.continuing);
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof NotEliminatedNext) {
            final NotEliminatedNext o = (NotEliminatedNext) other;
            return o.winner==winner && o.loser==loser && Arrays.equals(continuing,o.continuing);
        } else { return false; }
    }

    /** Compute and return the difficulty estimate associated with this assertion. This method
     * computes the tallies of the assertion's winner and loser, in the relevant context,
     * according to the set of Votes (votes) provided as input. The given AuditType, audit,
     * defines the chosen method of computing assertion difficulty given these winner and loser
     * tallies.*/
    public double difficulty(Votes votes,AuditType audit) {
        int[] tallies = votes.restrictedTallies(continuing);
        int tally_winner = Integer.MAX_VALUE;
        int tally_loser = 0;
        for (int i=0;i<continuing.length;i++) {
            if (winner==continuing[i]) tally_winner=tallies[i];
            else if (loser==continuing[i]) tally_loser=tallies[i];
        }
        return audit.difficulty(tally_winner, tally_loser);
    }

    /** Find the best NEN assertion that will rule out the outcome where the given winner is eliminated
     * next when only the specified candidates are continuing, on the basis of the set of Votes (votes)
     * cast in the contest and the chosen method of computing assertion difficulty (audit). May return null
     * if no such assertions exist. The 'continuing' candidates must include the given winner.  */
    public static AssertionAndDifficulty findBestDifficulty(Votes votes, AuditType audit, int [] continuing, int winner)  {
        int[] tallies = votes.restrictedTallies(continuing);
        int tally_winner = Integer.MAX_VALUE;
        int tally_loser = Integer.MAX_VALUE;
        Integer best_loser = null;
        for (int i=0;i<continuing.length;i++) {
            if (winner==continuing[i]) tally_winner=tallies[i];
            else if (tallies[i]<=tally_loser) { best_loser=continuing[i]; tally_loser=tallies[i]; }
        }
        if (best_loser!=null) {
            double difficulty = audit.difficulty(tally_winner, tally_loser);
            int margin = Math.max(0,tally_winner-tally_loser);
            NotEliminatedNext assertion = new NotEliminatedNext(winner,best_loser,continuing);
            return new AssertionAndDifficulty(assertion,difficulty,margin);
        } else { return null; }
    }

    /** Returns true if the given candidate is in this assertion's continuing list. */
    private boolean isContinuing(int c) {
        return Arrays.binarySearch(continuing,c)>=0;
    }

    @Override
    public boolean isNEB() {
        return false;
    }

    @Override
    public EffectOfAssertionOnEliminationOrderSuffix okEliminationOrderSuffix(int[] eliminationOrderSuffix) {
        // the order of the people who are left when down to the same length as self.continuing(). Or the whole thing if sub-prefix
        int startInclusive = Math.max(eliminationOrderSuffix.length-continuing.length,0);
        // check to see the last candidates in the elimination order match the continuing candidates.
        if (Arrays.stream(eliminationOrderSuffix,startInclusive,eliminationOrderSuffix.length).anyMatch(c->!isContinuing(c))) {
            return EffectOfAssertionOnEliminationOrderSuffix.Ok; // the elimination order is not affected by this rule as the continuing candidates are wrong.
        }
        if (eliminationOrderSuffix.length>=continuing.length) { // the whole elimination order is all present. The winner cannot be the first eliminated, as self.winner has more votes than self.loser at this point.
            return eliminationOrderSuffix[startInclusive]==winner?EffectOfAssertionOnEliminationOrderSuffix.Contradiction:EffectOfAssertionOnEliminationOrderSuffix.Ok;
        } else {
            if (Arrays.stream(eliminationOrderSuffix,startInclusive,eliminationOrderSuffix.length).anyMatch(c->c==winner)) return EffectOfAssertionOnEliminationOrderSuffix.Ok; // winner wasn't the first eliminated.
            else return EffectOfAssertionOnEliminationOrderSuffix.NeedsMoreDetail;
        }
    }
}
