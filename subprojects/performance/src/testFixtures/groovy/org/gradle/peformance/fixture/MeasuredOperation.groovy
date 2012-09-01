/*
 * Copyright 2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.peformance.fixture

import org.gradle.util.Clock

/**
 * by Szczepan Faber, created at: 2/10/12
 */
public class MeasuredOperation {
    long executionTime
    Exception exception
    String prettyTime
    long totalMemoryUsed
    
    String toString() {
        (totalMemoryUsed == 0)? prettyTime : prettyTime + ", " + getPrettyBytes()
    }

    String getPrettyBytes() {
        humanReadableByteCount(totalMemoryUsed)
    }

    static MeasuredOperation measure(Closure operation) {
        def out = new MeasuredOperation()
        def clock = new Clock()
        clock.reset()
        try {
            operation(out)
        } catch (Exception e) {
            out.exception = e
        }
        //not very atomic... :)
        out.prettyTime = clock.time
        out.executionTime = clock.timeInMs
        return out
    }

    //stolen from the web, TODO SF, replace with commons or something
    static String humanReadableByteCount(long bytes) {
        int unit = 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "kMGTPE".charAt(exp-1)
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
