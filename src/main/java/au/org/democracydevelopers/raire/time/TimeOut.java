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
    /** Record of the time at which computation, for which a time limit applies, began. */
    private final long start_time_ms;

    /** Total work done thus far in units 'of work'. Note that work is incremented with each
     * check on whether a timeout has occurred (see TimeOut::quickCheckTimeout()). */
    private long work_done;

    /** Limit on 'work' done. */
    private final Long work_limit;

    /** Limit on the time, in ms, allowed to RAIRE for its computation. */
    private final Long duration_limit_ms;

    /**  In case the clock is expensive to check, only check every UNITS_OF_WORK_PER_CLOCK_CHECK units of work. */
    public static final long UNITS_OF_WORK_PER_CLOCK_CHECK=100;

    /** Make a new timeout structure, null entries mean that the particular limit doesn't apply.
     * That is, if work_limit is null, but duration_limit_seconds is not null, then elapsed time
     * will be a constraint but the amount of work done is not a constraint. Similarly, if duration_limit_seconds
     * is null and work_limit is not, the clock time is irrelevant, and only work_limit matters. If
     * both are null then timeouts will never occur. */
    public TimeOut(Long work_limit,Double duration_limit_seconds) {
        this.start_time_ms=System.currentTimeMillis();
        this.work_done=0;
        this.work_limit=work_limit;
        this.duration_limit_ms=duration_limit_seconds==null?null:(long)Math.ceil(duration_limit_seconds*1000.0);
    }

    /** Make a dummy timer that will never timeout. */
    public static TimeOut never() { return new TimeOut(null,null); }

    /** Return the time (in ms) since the timer */
    public long clockTimeTakenSinceStartMillis() { return System.currentTimeMillis()-start_time_ms; }

    /** Get the total number of units of work done. */
    public long getWorkDone() { return work_done; }

    /** Return the time taken (as a TimeTaken structure) by RAIRE thus far. This data structure
     * indicates both the time and units of work consumed thus far by RAIRE. */
    public TimeTaken timeTaken() {
        return new TimeTaken(work_done,clockTimeTakenSinceStartMillis()/1000.0);
    }

    /**
     * Increments work_done by 1, and returns true if a limit is exceeded.
     * Only checks duration every 100 calls.
     * @return true if and only if a limit (time or work) has been exceeded.
     */
    public boolean quickCheckTimeout() {
        work_done+=1;
        if (work_limit!=null && work_done>work_limit) return true;
        return duration_limit_ms != null && (work_done % UNITS_OF_WORK_PER_CLOCK_CHECK == 0) && clockTimeTakenSinceStartMillis() > duration_limit_ms;
    }
}
