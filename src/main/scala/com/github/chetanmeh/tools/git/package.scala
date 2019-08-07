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

import com.jcabi.github.{Pull, Issue => JIssue}
import javax.json.JsonObject

package object git {
  case class Issue(creator: String, id: Int, title: String)

  object Issue {
    def apply(i: JIssue.Smart): Issue = {
      Issue(i.author().login(), i.number(), i.title())
    }

    def apply(json: JsonObject): Issue = {
      // https://developer.github.com/v3/pulls/#get-a-single-pull-request
      Issue(json.getJsonObject("user").getString("login"), json.getInt("number"), json.getString("title"))
    }
  }

  case class PullRequest(creator: String, id: Int, title: String)

  object PullRequest {
    def apply(p: Pull.Smart): PullRequest = {
      PullRequest(p.author().login(), p.number(), p.title())
    }

    def apply(json: JsonObject): PullRequest = {
      // https://developer.github.com/v3/pulls/#get-a-single-pull-request
      PullRequest(json.getJsonObject("user").getString("login"), json.getInt("number"), json.getString("title"))
    }
  }

  case class RepoReport(name: String, issues: List[Issue], pulls: List[PullRequest])
}
