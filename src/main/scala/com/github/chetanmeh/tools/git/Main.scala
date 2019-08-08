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

import java.io.File
import java.nio.charset.StandardCharsets.UTF_8
import java.time.LocalDate

import com.google.common.base.Stopwatch
import org.apache.commons.io.FileUtils
import org.rogach.scallop.{singleArgConverter, ScallopConf}
import org.slf4j.LoggerFactory

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  footer("\nGithub change reporter")
  this.printedName = "github"

  val token = opt[String](descr =
    "Github access token. See https://help.github.com/en/articles/creating-a-personal-access-token-for-the-command-line")

  val since = opt[LocalDate](descr = "Date since changes need tobe reported in yyyy-MM-dd format", required = true)(
    singleArgConverter[LocalDate](LocalDate.parse(_)))

  val out = opt[File](descr = "Output file path", default = Some(new File("report.md")))

  val repoNames =
    trailArg[List[String]](
      descr = "List of repository names for which report needs to be generated e.g. apache/openwhisk",
      required = false,
      default = Some(List.empty))

  verify()
}

object Main {
  private val log = LoggerFactory.getLogger("github-reporter")
  def main(args: Array[String]): Unit = {
    val w = Stopwatch.createStarted()
    val conf = new Conf(args)
    val config = GithubConfig(conf.token.toOption)
    val reporter = GithubReporter(GithubReporter.createGithub(config), config)
    val reports = reporter.generateReport(conf.repoNames(), conf.since())

    val reportRenderer = new ReportRenderer()
    FileUtils.write(conf.out(), reportRenderer.render(reports), UTF_8)
    log.info(s"Report generated in $w")
    log.info(s"Report written to ${conf.out().getAbsolutePath}")
  }
}
