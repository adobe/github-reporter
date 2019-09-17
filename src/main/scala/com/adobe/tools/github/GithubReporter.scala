/*
Copyright 2019 Adobe. All rights reserved.
This file is licensed to you under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License. You may obtain a copy
of the License at http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under
the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR REPRESENTATIONS
OF ANY KIND, either express or implied. See the License for the specific language
governing permissions and limitations under the License.
 */
package com.adobe.tools.github

import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.jcabi.github.{Coordinates, FromProperties, Github, RtGithub, RtPagination}
import com.jcabi.http.Request
import com.jcabi.http.request.ApacheRequest
import com.jcabi.http.wire.{AutoRedirectingWire, RetryWire}
import javax.json.JsonObject
import javax.ws.rs.core.{HttpHeaders, MediaType}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.mutable

case class GithubConfig(accessToken: Option[String], uri: String = "https://api.github.com")

case class GithubReporter(github: Github, config: GithubConfig) {
  private val log = LoggerFactory.getLogger(getClass)
  import GithubReporter._

  def generateReport(repoName: String, since: LocalDate): RepoReport = {
    val coords = new Coordinates.Simple(repoName)
    val repo = github.repos().get(coords)

    val issueItr = issuePaginator(coords, since)

    val issues = mutable.ListBuffer.empty[Issue]
    val pulls = mutable.ListBuffer.empty[PullRequest]
    issueItr.foreach { json =>
      if (isPull(json)) {
        val pr = if (PullRequest.isOpen(json)) {
          PullRequest(json, since, merged = false, repoName)
        } else {
          //Get pull details to determine the merged state
          PullRequest(json, since, merged = isMerged(json), repoName)
        }
        pulls += pr
      } else {
        issues += Issue(json, since, repoName)
      }
    }
    RepoReport(repoName, issues.toList, pulls.toList)
  }

  def generateReport(repoNames: Seq[String], since: LocalDate): Seq[RepoReport] = {
    //Include only reports from repo which had any change
    repoNames.map(r => generateReport(r, since)).filter(_.notEmpty).sortBy(_.name)
  }

  def collectRepoInfo(providedNames: Seq[String],
                      since: LocalDate,
                      orgName: Option[String] = None,
                      prefix: Option[String] = None): Seq[String] = {
    val orgRepos = orgName.map(collectRepoNames(_, since, prefix)).getOrElse(Seq.empty)
    val result = providedNames ++ orgRepos
    result.toSet.toList.sorted
  }

  def collectRepoNames(orgName: String, since: LocalDate, prefix: Option[String]): Seq[String] = {
    //Later we should see if `since` can be used to filter out repo here itself
    repoPaginator(orgName)
      .map(RepoInfo(_))
      .filter { r =>
        shouldIncludeRepo(r, since, prefix)
      }
      .map(_.fullName)
      .toList
  }

  private def shouldIncludeRepo(repo: RepoInfo, since: LocalDate, prefix: Option[String]): Boolean = {
    val prefixMatched = matchPrefix(repo.name, prefix)
    val repoUpdated = repoUpdatedSince(repo, since)
    if (prefixMatched && !repoUpdated) {
      log.info(s"Ignoring repo ${repo.fullName} as its not found to be updated")
    }
    prefixMatched && repoUpdated
  }

  private def isPull(issueJson: JsonObject): Boolean = issueJson.containsKey("pull_request")

  private def isMerged(issueJson: JsonObject): Boolean = {
    //https://developer.github.com/v3/issues/#response
    val uri = issueJson.getJsonObject("pull_request").getString("url")
    //https://developer.github.com/v3/pulls/#get-if-a-pull-request-has-been-merged
    val mergeUrl = uri + "/merge"
    github.entry().uri().set(new URI(mergeUrl)).back().fetch().status() == 204
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
    paginatedIterable(request, params)
  }

  private def repoPaginator(org: String) = {
    val request = github
      .entry()
      .uri()
      .path("/orgs")
      .path(org)
      .path("/repos")
      .back()

    val params = Map("per_page" -> "100")
    paginatedIterable(request, params)
  }

  private def paginatedIterable(r: Request, params: Map[String, String]) = {
    new RtPagination[JsonObject](r.uri.queryParams(params.asJava).back, x => x).asScala
  }
}

object GithubReporter {
  def apply(config: GithubConfig): GithubReporter = {
    GithubReporter(createGithub(config), config)
  }

  def createGithub(config: GithubConfig): Github = {
    val req = createRequest(config.uri)
    val reqWithToken = config.accessToken.map(authenticatedRequest(req, _)).getOrElse(req)
    val base = new RtGithub(reqWithToken)
    new RtGithub(base.entry().through(classOf[RetryWire]))
  }

  private def createRequest(uri: String) = {
    new ApacheRequest(uri)
      .header(HttpHeaders.USER_AGENT, new FromProperties("jcabigithub.properties").format)
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
      .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
      .through(classOf[AutoRedirectingWire])
  }

  private def authenticatedRequest(req: Request, token: String) =
    req.header(HttpHeaders.AUTHORIZATION, s"token $token")

  def matchPrefix(repoName: String, prefix: Option[String]): Boolean = {
    prefix match {
      case Some(p) => repoName.startsWith(p)
      case _       => true
    }
  }

  def repoUpdatedSince(repo: RepoInfo, since: LocalDate): Boolean = {
    // If any issue/pr is modified then it does not impact the updatedTime
    // So we select repo which are either actually updated after given date
    // or there open issue count is > 0 which indicates that some issue/pr has
    // been opened but repo is not updated yet
    // Untill we query for actual issue and PR updated/creation time we cannot
    // determine if no " interesting update" happened to the repo
    val isUpdated = repo.updated_at.isAfter(since) || repo.open_issues_count > 0
    !repo.archived && isUpdated
  }
}
