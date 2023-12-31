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

public class RaireSolution {
    public final Map<String,Object> metadata;
    public final RaireResultOrError solution;

    @ConstructorProperties({"metadata","solution"})
    public RaireSolution(Map<String, Object> metadata, RaireResultOrError solution) {
        this.metadata = metadata;
        this.solution = solution;
    }


    /// A wrapper around the Rust Error type. Exactly one of the fields will be null.
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
