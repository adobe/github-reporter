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

import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ReportRendererTests extends FlatSpec with Matchers {
  behavior of "Report Renderer"
  val reportRenderer = new ReportRenderer()

  it should "render in markdown" in {
    val o = renderReport(createSampleDataSet(), false)
    printReport(o)
  }

  it should "render in html" in {
    val o = renderReport(createSampleDataSet(), true)
    printReport(o)
  }

  private def printReport(r: String) = {
    println("-------")
    println(r)
    println("-------")
  }

  private def renderReport(r: RepoReport, html: Boolean) = {
    reportRenderer.render(Seq(r), html)
  }

  private def createSampleDataSet(): RepoReport = {
    val issueList = List(
      ("SungHoHong2", 4577, "Feature request: Checkpoint for recovering failed ", true, false),
      ("SungHoHong2", 4574, "Update cache-invalidator build.gradle file to resolve  build issue.", true, false),
      ("chetanmeh", 4576, "Update to Scala 2.12.9", true, true))

    val issues = issueList.map(
      i =>
        Issue(
          i._1,
          s"https://github.com/${i._1}",
          i._2,
          i._3,
          i._4,
          i._5,
          s"https://github.com/apache/openwhisk/issues/${i._2}",
          "foo/bar"))

    val prList = List(
      ("BillZong", 4618, "fix: controller ambiguous task env setting", true, false, true),
      ("BillZong", 4619, "Add ansible deploy options", true, false, false),
      ("jiangpengcheng", 4617, "Add kind &quot;unknown&quot; to fallback activations", true, true, false))

    val pulls = prList.map(
      i =>
        PullRequest(
          i._1,
          s"https://github.com/${i._1}",
          i._2,
          i._3,
          i._4,
          i._5,
          i._6,
          s"https://github.com/apache/openwhisk/issues/${i._2}",
          "foo/bar"))
    RepoReport("openwhisk", issues, pulls)
  }
}
