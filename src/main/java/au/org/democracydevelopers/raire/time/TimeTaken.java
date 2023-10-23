/*
  Copyright 2023 Democracy Developers
  This is a Java re-implementation of raire-rs https://github.com/DemocracyDevelopers/raire-rs
  It attempts to copy the design, API, and naming as much as possible subject to being idiomatic and efficient Java.

  This file is part of raire-java.
  raire-java is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  raire-java is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more details.
  You should have received a copy of the GNU Affero General Public License along with ConcreteSTV.  If not, see <https://www.gnu.org/licenses/>.

 */

package au.org.democracydevelopers.raire.time;

import java.beans.ConstructorProperties;

/** A measure of the time taken to do something, both in units of work and clock time */
public class TimeTaken {
    public final long work;
    public final double seconds;

    @ConstructorProperties({"work","seconds"})
    public TimeTaken(long work, double seconds) {
        this.work = work;
        this.seconds = seconds;
    }

    /** Get the difference between two times */
    public TimeTaken minus(TimeTaken rhs) {
        return new TimeTaken(work-rhs.work,seconds-rhs.seconds);
    }

    @Override
    public String toString() {
        if (seconds>=0.99999) {
            return String.format("%.3fs",seconds);
        } else {
            long ms = Math.round(seconds*1000.0);
            return ""+ms+"ms";
        }
    }
}


