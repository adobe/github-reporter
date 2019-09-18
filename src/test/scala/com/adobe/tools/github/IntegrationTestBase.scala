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
import java.time.format.DateTimeFormatter

import org.scalatest.{FlatSpec, Matchers}

abstract class IntegrationTestBase extends FlatSpec with Matchers {
  import TestUtils._
  val token = sys.env(tokenEnvName)

  val testOrg = "github-reporter-test"
  val creationDate = LocalDate.of(2019, 9, 16).minusDays(1)
  val creationDateStr = creationDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

  //For running these test a test org `github-reporter-test` is created in github
  //Which contain some repos to perform testing https://github.com/github-reporter-test

  override protected def withFixture(test: NoArgTest) = {
    assume(token != null, s"Github token not specified via env ${tokenEnvName}")
    super.withFixture(test)
  }
}

object TestUtils {
  val tokenEnvName = "GITHUB_TOKEN"
}
