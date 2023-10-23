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

/**
 * A check to see that we are not taking too long.
 * Allows efficient checking against clock time taken or work done.
 */
public class TimeOut {
    private final long start_time_ms;
    private long work_done;
    private final Long work_limit;
    private final Long duration_limit_ms;

    /**  In case the clock is expensive to check, only check every UNITS_OF_WORK_PER_CLOCK_CHECK units of work. */
    public static final long UNITS_OF_WORK_PER_CLOCK_CHECK=100;

    /** Make a new timeout structure. null entries mean that the duration doesn't apply */
    public TimeOut(Long work_limit,Double duration_limit_seconds) {
        this.start_time_ms=System.currentTimeMillis();
        this.work_done=0;
        this.work_limit=work_limit;
        this.duration_limit_ms=duration_limit_seconds==null?null:(long)Math.ceil(duration_limit_seconds*1000.0);
    }

    /** make a dummy timer that will never timeout */
    public static TimeOut never() { return new TimeOut(null,null); }

    public long clockTimeTakenSinceStartMillis() { return System.currentTimeMillis()-start_time_ms; }
    /** Get the total number of units of work done */
    public long getWorkDone() { return work_done; }

    public TimeTaken timeTaken() {
        return new TimeTaken(work_done,clockTimeTakenSinceStartMillis()/1000.0);
    }

    /**
     * increments work_done by 1, and returns true if a limit is exceeded
     * * only checks duration every 100 calls.
     * @return true iff a limit is exceeded
     */
    public boolean quickCheckTimeout() {
        work_done+=1;
        if (work_limit!=null && work_done>work_limit) return true;
        return duration_limit_ms != null && (work_done % UNITS_OF_WORK_PER_CLOCK_CHECK == 0) && clockTimeTakenSinceStartMillis() > duration_limit_ms;
    }
}
