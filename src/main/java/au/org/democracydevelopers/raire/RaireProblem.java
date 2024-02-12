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

import au.org.democracydevelopers.raire.algorithm.RaireResult;
import au.org.democracydevelopers.raire.audittype.AuditType;
import au.org.democracydevelopers.raire.irv.Vote;
import au.org.democracydevelopers.raire.irv.Votes;
import au.org.democracydevelopers.raire.pruning.TrimAlgorithm;
import au.org.democracydevelopers.raire.time.TimeOut;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.beans.ConstructorProperties;
import java.util.Map;

/** Defines a contest for which we want to generate assertions, metadata for that contest, and all algorithmic
 * settings to be used by RAIRE when generating assertions. */
public class RaireProblem {
    /** The input to raire-java will contain metadata that, while not used by raire-java for computing assertions,
     * may be useful information for assertion visualisation or information that election administrators would like
     * to associate with any assertions generated. */
    public final Map<String,Object> metadata;

    /** The consolidated set of votes cast in the election. Note that each Vote is a ranking and the number of times
     * that ranking appeared on a vote cast in the contest. */
    public final Vote[] votes;

    /** The number of candidates in the contest. */
    public final int num_candidates;

    /** The reported winner of the contest (if provided as input to raire-java). If this information was not
     * provided as input, this field will be null. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public final Integer winner; // may be null.

    /** The method that RAIRE should use to assess the difficulty of auditing a generated assertion. */
    public final AuditType audit;

    /** The algorithm that raire-java will use to filter the set of generated assertions, removing those that
     * are redundant. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public final TrimAlgorithm trim_algorithm; // may be null.

    /** An estimate of the expected overall difficulty of the audit, optionally provided as input. RAIRE may
     * be able to use this estimate to generate assertions more efficiently. Note that the overall difficulty
     * of an audit is the difficulty of the most-difficulty-to-audit-assertion generated. See AuditType and its
     * implementations for more information on different approaches for computing assertion difficulty. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public final Double difficulty_estimate; // may be null.

    /** Optional time limit to impose across all stages of computation by raire-java. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public final Double time_limit_seconds; // may be null.

    @ConstructorProperties({"metadata","votes", "num_candidates","winner","audit","trim_algorithm","difficulty_estimate","time_limit_seconds"})
    public RaireProblem(Map<String, Object> metadata, Vote[] votes, int num_candidates, Integer winner, AuditType audit, TrimAlgorithm trim_algorithm, Double difficulty_estimate, Double time_limit_seconds) {
        this.metadata = metadata;
        this.votes = votes;
        this.num_candidates = num_candidates;
        this.winner = winner;
        this.audit = audit;
        this.trim_algorithm = trim_algorithm;
        this.difficulty_estimate = difficulty_estimate;
        this.time_limit_seconds = time_limit_seconds;
    }

    /** Generate assertions for the given contest, and return those assertions as a RaireSolution. */
    public RaireSolution solve() {
        RaireSolution.RaireResultOrError result;
        if (time_limit_seconds!=null && (time_limit_seconds <=0.0 || time_limit_seconds.isNaN() )) result=new RaireSolution.RaireResultOrError(new RaireError.InvalidTimeout());
        else {
            TimeOut timeout = new TimeOut(null,time_limit_seconds);
            try {
                Votes votes = new Votes(this.votes,this.num_candidates);
                result=new RaireSolution.RaireResultOrError(new RaireResult(votes,winner,audit,trim_algorithm==null?TrimAlgorithm.MinimizeTree:trim_algorithm,timeout));
            } catch (RaireException e) {
                result=new RaireSolution.RaireResultOrError(e.error);
            }
        }
        return new RaireSolution(metadata,result);
    }
}
