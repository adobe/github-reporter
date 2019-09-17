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

import java.io.File
import java.net.URI
import java.nio.charset.StandardCharsets.UTF_8
import java.time.LocalDate

import com.google.common.base.Stopwatch
import org.apache.commons.io.{FileUtils, FilenameUtils}
import org.rogach.scallop.{singleArgConverter, ScallopConf}
import org.slf4j.LoggerFactory

import scala.concurrent.duration.Duration
import scala.util.{Failure, Success, Try}

class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
  footer("\nGithub change reporter")
  this.printedName = "github"

  val token = opt[String](descr =
    "Github access token. See https://help.github.com/en/articles/creating-a-personal-access-token-for-the-command-line")

  val since = opt[LocalDate](
    descr = "Date since changes need tobe reported in yyyy-MM-dd format. " +
      "One can also provide duration like '4 days`, '1 month'",
    required = true)(singleArgConverter[LocalDate](Main.parseSinceDate))

  val out = opt[File](descr = "Output file path", default = Some(new File("report.md")))

  val repoNames =
    trailArg[List[String]](
      descr = "List of repository names for which report needs to be generated e.g. apache/openwhisk",
      required = false,
      default = Some(List.empty))

  val githubUri = opt[String](
    descr =
      "Github server uri. By default it refers to public github. If your repo is on an internal enterprise deployment then set this to the server url",
    default = Some("https://api.github.com"),
    noshort = true)

  val org = opt[String](
    descr = "Organization name. Reporter would find its repositories and generate report " +
      "for them. If any prefix if provided then only those repo would be selected",
    required = false)

  val repoPrefix = opt[String](
    descr = "Repo name prefix. If provided only repo whose name start with the provided " +
      "prefix would be used",
    required = false,
    noshort = true)

  val htmlMode =
    opt[Boolean](
      descr = "Render in HTML mode. By default report is rendered in Markdown format",
      required = false,
      noshort = true)

  val jsonMode =
    opt[Boolean](
      descr = "Render in json mode. If enables then another report in json mode would also be rendered",
      required = false,
      noshort = true)

  dependsOnAll(repoPrefix, List(org))
  verify()
}

object Main {
  private val log = LoggerFactory.getLogger("github-reporter")
  def main(args: Array[String]): Unit = {
    val w = Stopwatch.createStarted()
    val conf = new Conf(args)
    val config = GithubConfig(conf.token.toOption, adaptUri(conf.githubUri()))
    val reporter = GithubReporter(config)

    log.info(s"Connecting to ${config.uri}")
    log.info(s"Collecting changes since ${conf.since()}")

    val repoNames = reporter.collectRepoNames(conf.repoNames(), conf.org.toOption, conf.repoPrefix.toOption)
    require(repoNames.nonEmpty, "No repository name provided")
    log.info(s"Report would be generated for ${repoNames.size} repositories")

    val reports = reporter.generateReport(repoNames, conf.since())

    val reportRenderer = new ReportRenderer()
    val file = getOutputFile(conf)
    FileUtils.write(file, reportRenderer.render(reports, conf.htmlMode()), UTF_8)
    log.info(s"Report generated in $w")

    if (conf.jsonMode()) {
      val jsonFile = getFileWithExtension(file, "json")
      FileUtils.write(jsonFile, reportRenderer.renderJson(reports), UTF_8)
      log.info(s"Report written in json to ${jsonFile.getAbsolutePath}")
    }

    log.info(s"Report written to ${file.getAbsolutePath}")
  }

  def adaptUri(uri: String): String = {
    val u = new URI(uri)
    val ru = if (uri.endsWith("/")) uri.dropRight(1) else uri
    if (u.getHost != "api.github.com") ru + "/api/v3" else ru
  }

  def parseSinceDate(dateStr: String): LocalDate = {
    Try(LocalDate.parse(dateStr)) match {
      case Success(d) => d
      case Failure(_) =>
        Try {
          val d = Duration(dateStr)
          LocalDate.now().minusDays(d.toDays)
        }.getOrElse(throw new IllegalArgumentException("Cannot parse since time " + dateStr))
    }
  }

  def getOutputFile(conf: Conf): File = {
    val f = conf.out()
    if (conf.htmlMode()) {
      getFileWithExtension(f, "html")
    } else f
  }

  def getFileWithExtension(f: File, ext: String): File = {
    new File(f.getParentFile, FilenameUtils.removeExtension(f.getName) + "." + ext)
  }
}
