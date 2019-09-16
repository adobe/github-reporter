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

package com.github.chetanmeh.tools

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import javax.json.JsonObject

package object git {
  case class Issue(creator: String,
                   creatorUrl: String,
                   id: Int,
                   title: String,
                   isNew: Boolean,
                   open: Boolean,
                   url: String) {
    def isNewlyOpened: Boolean = isNew && open
    def isUpdate: Boolean = !isNew && open
    def isClosed: Boolean = !open
  }

  object Issue {
    def apply(json: JsonObject, since: LocalDate): Issue = {
      val c = CommonAttr(json, since)
      Issue(c.creator, c.creatorUrl, c.id, c.title, c.isNew, c.open, c.url)
    }
  }

  case class PullRequest(creator: String,
                         creatorUrl: String,
                         id: Int,
                         title: String,
                         isNew: Boolean,
                         open: Boolean,
                         merged: Boolean,
                         url: String) {
    def isNewlyOpened: Boolean = isNew && open
    def isUpdate: Boolean = !isNew && open
    def isClosed: Boolean = !open && !merged
    def isMerged: Boolean = merged
  }

  object PullRequest {
    def apply(json: JsonObject, since: LocalDate): PullRequest = {
      val c = CommonAttr(json, since)
      PullRequest(c.creator, c.creatorUrl, c.id, c.title, c.isNew, c.open, json.getBoolean("merged"), c.url)
    }
  }

  case class RepoReport(name: String, issues: List[Issue], pulls: List[PullRequest]) {
    def notEmpty: Boolean = issues.nonEmpty || pulls.nonEmpty
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
                        url: String)

  object CommonAttr {
    def apply(json: JsonObject, since: LocalDate): CommonAttr = {
      // https://developer.github.com/v3/issues/#list-issues-for-a-repository
      val createdAt = asDate(json.getString("created_at"))
      CommonAttr(
        json.getJsonObject("user").getString("login"),
        json.getJsonObject("user").getString("html_url"),
        json.getInt("number"),
        json.getString("title"),
        createdAt.isAfter(since.minusDays(1)),
        json.getString("state") == "open",
        json.getString("url"))
    }
  }

  case class RepoInfo(fullName: String, name: String)

  object RepoInfo {
    def apply(json: JsonObject): RepoInfo = RepoInfo(json.getString("full_name"), json.getString("name"))
  }
}
