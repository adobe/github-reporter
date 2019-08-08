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
  case class Issue(creator: String, id: Int, title: String, isNew: Boolean, open: Boolean) {
    def isNewlyOpened: Boolean = isNew && open
    def isUpdate: Boolean = !isNew && open
    def isClosed: Boolean = !open
  }

  object Issue {
    def apply(json: JsonObject, since: LocalDate): Issue = {
      // https://developer.github.com/v3/pulls/#get-a-single-pull-request
      val createdAt = asDate(json.getString("created_at"))
      Issue(
        json.getJsonObject("user").getString("login"),
        json.getInt("number"),
        json.getString("title"),
        createdAt.isAfter(since.minusDays(1)),
        json.getString("state") == "open")
    }
  }

  case class PullRequest(creator: String, id: Int, title: String, isNew: Boolean, open: Boolean, merged: Boolean) {
    def isNewlyOpened: Boolean = isNew && open
    def isUpdate: Boolean = !isNew && open
    def isClosed: Boolean = !open && !merged
    def isMerged: Boolean = merged
  }

  object PullRequest {
    def apply(json: JsonObject, since: LocalDate): PullRequest = {
      // https://developer.github.com/v3/pulls/#get-a-single-pull-request
      val createdAt = asDate(json.getString("created_at"))
      PullRequest(
        json.getJsonObject("user").getString("login"),
        json.getInt("number"),
        json.getString("title"),
        createdAt.isAfter(since.minusDays(1)),
        json.getString("state") == "open",
        json.getBoolean("merged"))
    }
  }

  case class RepoReport(name: String, issues: List[Issue], pulls: List[PullRequest]) {
    def notEmpty: Boolean = issues.nonEmpty || pulls.nonEmpty
  }

  private def asDate(str: String): LocalDate = {
    DateTimeFormatter.ISO_DATE_TIME.parse(str, LocalDate.from _)
  }
}
