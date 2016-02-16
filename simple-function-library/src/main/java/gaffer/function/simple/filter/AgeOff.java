/*
 * Copyright 2016 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gaffer.function.simple.filter;

import gaffer.function.SimpleFilterFunction;
import gaffer.function.annotation.Inputs;

/**
 * An <code>AgeOff</code> is a {@link SimpleFilterFunction} that ages off old data based on a provided age of time in milliseconds.
 */
@Inputs(Long.class)
public class AgeOff extends SimpleFilterFunction<Long> {
    public static final int HOURS_TO_MILLISECONDS = 24 * 60 * 60 * 1000;
    public static final int DAYS_TO_MILLISECONDS = 24 * HOURS_TO_MILLISECONDS;

    /**
     * The default age of time (1 year) in milliseconds.
     */
    public static final long AGE_OFF_TIME_DEFAULT = 365L * DAYS_TO_MILLISECONDS;

    private long ageOffTime = AGE_OFF_TIME_DEFAULT;

    // Default constructor for serialisation
    public AgeOff() {
    }

    public AgeOff(final long ageOffTime) {
        this.ageOffTime = ageOffTime;
    }

    public AgeOff statelessClone() {
        AgeOff clone = new AgeOff(ageOffTime);
        clone.setAgeOffTime(ageOffTime);

        return clone;
    }

    @Override
    protected boolean _isValid(final Long input) {
        return null != input && input > (System.currentTimeMillis() - ageOffTime);
    }

    public long getAgeOffTime() {
        return ageOffTime;
    }

    public void setAgeOffTime(final long ageOffTime) {
        this.ageOffTime = ageOffTime;
    }

    public void setAgeOffDays(final long ageOfDays) {
        setAgeOffTime(ageOfDays * DAYS_TO_MILLISECONDS);
    }

    public void setAgeOffHours(final long ageOfHours) {
        setAgeOffTime(ageOfHours * HOURS_TO_MILLISECONDS);
    }
}
