/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import groovy.util.logging.Slf4j
import org.gradle.api.Plugin
import org.gradle.api.Project

@Slf4j
class ScalatePlugin implements Plugin<Project> {

    static final String SCALATE_PRECOMPILE = 'scalatePreCompile'

    @Override
    public void apply(Project project) {

        PrecompileTask precompileTask = project.tasks.create(SCALATE_PRECOMPILE, PrecompileTask)
        precompileTask.description = 'Precompile the scalate templates'
        precompileTask.group = 'Scalate'

        project.afterEvaluate {
            precompileTask.dependsOn project.compileScala
            project.classes.dependsOn precompileTask
        }
    }

}