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

import com.jcabi.github.{Coordinates, Github, Repo, RtGithub, RtPagination}
import com.jcabi.http.wire.RetryWire
import javax.json.JsonObject

import scala.collection.JavaConverters._
import scala.collection.mutable

case class GithubConfig(accessToken: Option[String])

case class GithubReporter(github: Github, config: GithubConfig) {

  def generateReport(repoName: String, since: LocalDate): RepoReport = {
    val coords = new Coordinates.Simple(repoName)
    val repo = github.repos().get(coords)

    val issueItr = issuePaginator(coords, since)

    val issues = mutable.ListBuffer.empty[Issue]
    val pulls = mutable.ListBuffer.empty[PullRequest]
    issueItr.foreach { json =>
      if (isPull(json)) {
        pulls += PullRequest(getPull(json, repo), since)
      } else {
        issues += Issue(json, since)
      }
    }
    RepoReport(repoName, issues.toList, pulls.toList)
  }

  def generateReport(repoNames: Seq[String], since: LocalDate): Seq[RepoReport] = {
    //Include only reports from repo which had any change
    repoNames.map(r => generateReport(r, since)).filter(_.notEmpty).sortBy(_.name)
  }

  private def isPull(issueJson: JsonObject): Boolean = issueJson.containsKey("pull_request")

  private def getPull(issueJson: JsonObject, repo: Repo): JsonObject = {
    val uri = issueJson.getJsonObject("pull_request").getString("html_url")
    val prId = uri.substring(uri.lastIndexOf("/") + 1).toInt
    repo.pulls().get(prId).json()
  }

  private def issuePaginator(coords: Coordinates, since: LocalDate) = {
    val request = github
      .entry()
      .uri()
      .path("/repos")
      .path(coords.user())
      .path(coords.repo())
      .path("/issues")
      .back()

    val params = Map(
      "sort" -> "updated",
      "direction" -> "desc",
      "state" -> "all",
      "since" -> since.format(DateTimeFormatter.ISO_DATE))

    val p = new RtPagination[JsonObject](request.uri.queryParams(params.asJava).back, x => x)
    p.asScala
  }

}

object GithubReporter {
  def apply(config: GithubConfig): GithubReporter = {
    GithubReporter(createGithub(config), config)
  }

  def createGithub(config: GithubConfig): Github = {
    val base = config.accessToken.map(new RtGithub(_)).getOrElse(new RtGithub())
    new RtGithub(base.entry().through(classOf[RetryWire]))
  }
}
