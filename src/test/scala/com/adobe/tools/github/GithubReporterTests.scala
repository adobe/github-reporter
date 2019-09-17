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

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, File, ObjectInputStream, ObjectOutputStream}
import java.nio.charset.StandardCharsets.UTF_8
import java.time.LocalDate
import java.util.Base64

import org.apache.commons.io.FileUtils
import org.junit.runner.RunWith
import org.scalatest.{FlatSpec, Ignore, Matchers}
import org.scalatest.junit.JUnitRunner

@Ignore
@RunWith(classOf[JUnitRunner])
class GithubReporterTests extends FlatSpec with Matchers with ReporterTestBase {
  behavior of "Reporter"

  val token = sys.env("GITHUB_TOKEN")
  val reporter = GithubReporter(GithubConfig(Some(token)))
  val repos = List(
    "apache/openwhisk",
    "apache/openwhisk-website",
    "apache/openwhisk-cli",
    "apache/openwhisk-runtime-swift",
    "apache/openwhisk-runtime-go",
    "apache/openwhisk-runtime-nodejs",
    "apache/openwhisk-runtime-rust",
    "apache/openwhisk-deploy-kube",
    "apache/openwhisk-client-js",
    "apache/openwhisk-runtime-python",
    "apache/openwhisk-devtools",
    "apache/openwhisk-utilities",
    "apache/openwhisk-release",
    "apache/openwhisk-catalog",
    "apache/openwhisk-wskdeploy",
    "apache/openwhisk-composer",
    "apache/openwhisk-runtime-php",
    "apache/openwhisk-runtime-java",
    "apache/openwhisk-apigateway",
    "apache/openwhisk-package-alarms",
    "apache/openwhisk-package-cloudant",
    "apache/openwhisk-package-kafka",
    "apache/openwhisk-runtime-ballerina",
    "apache/openwhisk-runtime-dotnet",
    "apache/openwhisk-runtime-ruby",
    "apache/openwhisk-runtime-docker",
    "apache/openwhisk-client-go",
    "apache/openwhisk-pluggable-provider")

  it should "get repo info" in {
    val reportRenderer = new ReportRenderer()
    val r = reporter.generateReport("apache/openwhisk", LocalDate.parse("2019-09-07"))
    println("-------")
    println(reportRenderer.render(r, false))
    println("-------")
  }

  it should "get repo name" in {
    val reportRenderer = new ReportRenderer()
    val r =
      reporter.collectRepoNames(Seq("apache/foo"), Some("apache"), Some("openwhisk"))
    println("-------")
    println(r.mkString("\n"))
    println("-------")
  }

  it should "get repo info private" in {
    val reporter = GithubReporter(GithubConfig(Some(token)).copy(uri = "https://git.corp.adobe.com/api/v3"))
    val reportRenderer = new ReportRenderer()
    val r = reporter.generateReport("adobe-apis/dcos-deploy", LocalDate.parse("2019-09-07"))
    println("-------")
    println(reportRenderer.render(r, true))
    println("-------")
  }

  it should "get repo info for all" in {
    val reportRenderer = new ReportRenderer()
    val reports = reporter.generateReport(repos, LocalDate.parse("2019-08-07"))
    FileUtils.write(new File("report-serialized2.txt"), serialise(reports), UTF_8)
    println("-------")
    println(reportRenderer.render(reports, false))
    println("-------")
  }

  it should "render from serialized info" in {
    println("here")
    val reportRenderer = new ReportRenderer()
    val reports =
      deserialise(FileUtils.readFileToString(new File("report-serialized.txt"), UTF_8)).asInstanceOf[Seq[RepoReport]]

    println("-------")
    println(reportRenderer.render(reports, false))
    println("-------")
  }

  def serialise(value: Any): String = {
    val stream: ByteArrayOutputStream = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(stream)
    oos.writeObject(value)
    oos.close
    new String(Base64.getEncoder().encode(stream.toByteArray), UTF_8)
  }

  def deserialise(str: String): Any = {
    val bytes = Base64.getDecoder().decode(str.getBytes(UTF_8))
    val ois = new ObjectInputStream(new ByteArrayInputStream(bytes))
    val value = ois.readObject
    ois.close
    value
  }
}
