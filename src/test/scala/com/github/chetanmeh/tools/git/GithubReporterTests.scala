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

import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class GithubReporterTests extends FlatSpec with Matchers with ReporterTestBase {
  behavior of "Reporter"

  val reporter = GithubReporter()

  it should "get repo info" in {
    println(reporter.generateReport("apache/openwhisk", LocalDate.parse("2019-08-01")))
  }

  it should "render" in {
    val reportRenderer = new ReportRenderer()
    val r = RepoReport(
      "openwhisk",
      List(
        Issue("SungHoHong2", 4577, "Feature request: Checkpoint for recovering failed actions", true, false),
        Issue(
          "steven0711dong",
          4574,
          "Update cache-invalidator build.gradle file to resolve bluemix-openwhiskl-cli build issue. ",
          true,
          false),
        Issue("chetanmeh", 4576, "Update to Scala 2.12.9", true, true)),
      List.empty)
    val o = reportRenderer.render(r)
    println("-------")
    println(o)
    println("-------")
  }
}
