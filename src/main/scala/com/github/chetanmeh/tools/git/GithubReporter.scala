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

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util

import com.jcabi.github.Issues.{Qualifier, Sort}
import com.jcabi.github.Search.Order
import com.jcabi.github.{Coordinates, Github, Pull, RtGithub, Issue => JIssue}
import com.jcabi.http.wire.RetryWire
import com.typesafe.config.{Config, ConfigFactory}
import pureconfig.generic.auto._
import pureconfig.loadConfigOrThrow

import scala.collection.JavaConverters._
import scala.collection.mutable

case class GithubConfig(accessToken: String)

case class GithubReporter(github: Github, config: GithubConfig) {
  def generateReport(repoName: String, since: LocalDate): RepoReport = {
    val repo = github.repos().get(new Coordinates.Simple(repoName))

    val criteria = Map(Qualifier.STATE -> "all", Qualifier.SINCE -> since.format(DateTimeFormatter.ISO_DATE))
    val issueItr = repo.issues().search(Sort.UPDATED, Order.DESC, new util.EnumMap(criteria.asJava))

    val issues = mutable.ListBuffer.empty[Issue]
    val pulls = mutable.ListBuffer.empty[PullRequest]
    issueItr.asScala.foreach { i =>
      val si = new JIssue.Smart(i)
      if (si.isPull) {
        val p = new Pull.Smart(si.pull())
        pulls += PullRequest(p)
      } else {
        issues += Issue(si)
      }
    }
    RepoReport(repoName, issues.toList, pulls.toList)
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
