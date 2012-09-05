/*
 * Copyright 2012 the original author or authors.
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

package org.gradle.launcher.daemon.testing

/**
 * by Szczepan Faber, created at: 9/3/12
 */
class DaemonLogsAnalyzer {

    List<File> daemonLogs

    DaemonLogsAnalyzer(File baseDir) {
        assert baseDir.listFiles().length == 1
        def daemonFiles = baseDir.listFiles()[0].listFiles()
        daemonLogs = daemonFiles.findAll { it.name.endsWith('.log') }
    }

    List<TheDaemon> getDaemons() {
        def out = new LinkedList<TheDaemon>()
        daemonLogs.each {
            out << new TheDaemon(it)
        }
        out
    }

    TheDaemon getIdleDaemon() {
        def daemons = getDaemons()
        assert daemons.size() == 1: "Expected only a single daemon."
        TheDaemon daemon = daemons[0]
        assert daemon.idle : "Expected the daemon to be idle."
        daemon
    }
}