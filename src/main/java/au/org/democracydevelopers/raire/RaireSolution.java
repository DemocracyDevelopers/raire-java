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
import com.fasterxml.jackson.annotation.JsonInclude;

import java.beans.ConstructorProperties;
import java.util.Map;

/** */
public class RaireSolution {
    /** A replication of the metadata provided in RaireProblem. This is designed to include
     * information that is useful for election administrators (to associate with generated assertions)
     * or for assertion visualisation. */
    public final Map<String,Object> metadata;

    /** If no error arose during assertion generation, this attribute will store the set of generated
     * assertions in the form of a RaireResult. Otherwise, it will provide information on the error
     * in the form of a RaireError. */
    public final RaireResultOrError solution;

    @ConstructorProperties({"metadata","solution"})
    public RaireSolution(Map<String, Object> metadata, RaireResultOrError solution) {
        this.metadata = metadata;
        this.solution = solution;
    }


    /** A wrapper around the outcome of RAIRE. The outcome is either a RaireResult (if no error arose) or
     * a RaireError (if an error did arise). Exactly one of the fields will be null. */
    public static class RaireResultOrError {
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public final RaireResult Ok;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public final RaireError Err;

        /** Only used by the Jackson serialization which can only have one constructor annotated :-( */
        @ConstructorProperties({"Ok","Err"})
        public RaireResultOrError(RaireResult Ok,RaireError Err) { this.Ok=Ok; this.Err=Err;}
        public RaireResultOrError(RaireResult Ok) { this.Ok=Ok; this.Err=null;}
        public RaireResultOrError(RaireError Err) { this.Ok=null; this.Err=Err;}
    }

}
