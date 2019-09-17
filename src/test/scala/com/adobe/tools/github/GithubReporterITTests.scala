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

package com.adobe.tools.github

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class GithubReporterITTests extends IntegrationTestBase {
  behavior of "ReporterIT"

  val reporter = GithubReporter(GithubConfig(Some(token)))

  it should "collect all repos" in {
    val repos = reporter.collectRepoInfo(Seq("apache/openwhisk"), creationDate, Some(testOrg), Some("reporter"))
    repos should contain allOf ("apache/openwhisk", "github-reporter-test/reporter-ui", "github-reporter-test/reporter-app")
    repos should not contain ("tools")
  }

  it should "generate report for test repo" in {
    val appReport = reporter.generateReport(s"$testOrg/reporter-app", creationDate)
    appReport.issues.count(_.isClosed) shouldBe 1
    appReport.issues.count(_.open) shouldBe 2

    appReport.pulls.count(_.merged) shouldBe 1
    appReport.pulls.count(_.open) shouldBe 1

    appReport.notEmpty shouldBe true
  }

  it should "generate report for non updated repo" in {
    val appReport = reporter.generateReport(s"$testOrg/reporter-ui", creationDate)
    appReport.notEmpty shouldBe false
  }
}
