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

import java.time.LocalDate

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class GithubReporterTests extends FlatSpec with Matchers {
  behavior of "Reporter"

  it should "match prefix" in {
    GithubReporter.matchPrefix("openwhisk-runtime", None) shouldBe true
    GithubReporter.matchPrefix("openwhisk-runtime", Some("openwhisk")) shouldBe true
    GithubReporter.matchPrefix("openwhisk-runtime", Some("sling")) shouldBe false
  }

  it should "consider repo updated" in {
    val since = LocalDate.now()
    val r = RepoInfo("apache/openwhisk", "openwhisk", since.plusDays(1), 0, archived = false)

    GithubReporter.repoUpdatedSince(r, since) shouldBe true

    //For archived repo is not considered updated at all
    GithubReporter.repoUpdatedSince(r.copy(archived = true), since) shouldBe false

    //If updated in past then not updated
    GithubReporter.repoUpdatedSince(r, since.plusDays(2)) shouldBe false

    //Even if updated in past but having open issue then consider it updated
    GithubReporter.repoUpdatedSince(r.copy(open_issues_count = 2), since.plusDays(2)) shouldBe true
  }
}
