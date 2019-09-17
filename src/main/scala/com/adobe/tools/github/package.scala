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
package com.adobe.tools

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import javax.json.JsonObject
import spray.json.DefaultJsonProtocol._
import scala.collection.JavaConverters._

package object github {
  case class Issue(creator: String,
                   creatorUrl: String,
                   id: Int,
                   title: String,
                   isNew: Boolean,
                   open: Boolean,
                   url: String,
                   repoFullName: String,
                   labels: List[String]) {
    def isNewlyOpened: Boolean = isNew && open
    def isUpdate: Boolean = !isNew && open
    def isClosed: Boolean = !open
  }

  object Issue {
    implicit val serdes = jsonFormat9(Issue.apply)
    def apply(json: JsonObject, since: LocalDate, repoFullName: String): Issue = {
      val c = CommonAttr(json, since)
      Issue(c.creator, c.creatorUrl, c.id, c.title, c.isNew, c.open, c.url, repoFullName, c.labels)
    }
  }

  case class PullRequest(creator: String,
                         creatorUrl: String,
                         id: Int,
                         title: String,
                         isNew: Boolean,
                         open: Boolean,
                         merged: Boolean,
                         url: String,
                         repoFullName: String,
                         labels: List[String]) {
    def isNewlyOpened: Boolean = isNew && open
    def isUpdate: Boolean = !isNew && open
    def isClosed: Boolean = !open && !merged
    def isMerged: Boolean = merged
  }

  object PullRequest {
    implicit val serdes = jsonFormat10(PullRequest.apply)
    def apply(json: JsonObject, since: LocalDate, merged: Boolean, repoFullName: String): PullRequest = {
      val c = CommonAttr(json, since)
      PullRequest(c.creator, c.creatorUrl, c.id, c.title, c.isNew, c.open, merged, c.url, repoFullName, c.labels)
    }

    def isOpen(json: JsonObject): Boolean = json.getString("state") == "open"
  }

  case class RepoReport(name: String, issues: List[Issue], pulls: List[PullRequest]) {
    def notEmpty: Boolean = issues.nonEmpty || pulls.nonEmpty
  }

  object RepoReport {
    implicit val serdes = jsonFormat3(RepoReport.apply)
  }

  private def asDate(str: String): LocalDate = {
    DateTimeFormatter.ISO_DATE_TIME.parse(str, LocalDate.from _)
  }

  case class CommonAttr(creator: String,
                        creatorUrl: String,
                        id: Int,
                        title: String,
                        isNew: Boolean,
                        open: Boolean,
                        url: String,
                        labels: List[String])

  object CommonAttr {
    def apply(json: JsonObject, since: LocalDate): CommonAttr = {
      // https://developer.github.com/v3/issues/#list-issues-for-a-repository
      val createdAt = asDate(json.getString("created_at"))
      CommonAttr(
        json.getJsonObject("user").getString("login"),
        json.getJsonObject("user").getString("html_url"),
        json.getInt("number"),
        json.getString("title"),
        createdAt.isAfter(since),
        json.getString("state") == "open",
        json.getString("html_url"),
        json.getJsonArray("labels").asScala.map(x => x.asInstanceOf[JsonObject].getString("name")).toList)
    }
  }

  case class RepoInfo(fullName: String, name: String, updated_at: LocalDate, open_issues_count: Int, archived: Boolean)

  object RepoInfo {
    //https://developer.github.com/v3/repos/#list-organization-repositories
    def apply(json: JsonObject): RepoInfo =
      RepoInfo(
        json.getString("full_name"),
        json.getString("name"),
        asDate(json.getString("updated_at")),
        json.getInt("open_issues_count"),
        json.getBoolean("archived"))
  }
}
