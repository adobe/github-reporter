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

package com.github.chetanmeh.tools.git

import com.jcabi.github.{Coordinates, Github, RtGithub}
import com.jcabi.http.wire.RetryWire
import com.typesafe.config.{Config, ConfigFactory}
import pureconfig.generic.auto._
import pureconfig.loadConfigOrThrow

case class GithubConfig(accessToken: String)

case class GithubReporter(github: Github, config: GithubConfig) {
  def generateReport(repoName: String): String = {
    val repo = github.repos().get(new Coordinates.Simple(repoName))
    repo.toString
  }

}

object GithubReporter {

  def apply(): GithubReporter = apply(ConfigFactory.load())

  def apply(globalConfig: Config): GithubReporter = {
    val config = loadConfigOrThrow[GithubConfig](globalConfig.getConfig(ConfigKeys.github))
    val github = new RtGithub(new RtGithub(config.accessToken).entry().through(classOf[RetryWire]))
    new GithubReporter(github, config)
  }
}

object ConfigKeys {
  val github = "reporter.github"

}
