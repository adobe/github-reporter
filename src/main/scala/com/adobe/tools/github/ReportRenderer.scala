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

import java.nio.charset.StandardCharsets.UTF_8

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.data.MutableDataSet
import org.apache.commons.io.IOUtils
import org.fusesource.scalate._
import org.fusesource.scalate.support.StringTemplateSource
import org.fusesource.scalate.util.{Resource, ResourceLoader}
import spray.json._
import spray.json.DefaultJsonProtocol._

class ReportRenderer(template: String = "repo-report.ssp") {
  private val engine = {
    val e = new TemplateEngine
    e.resourceLoader = ClassPathResourceLoader
    e.allowReload = false
    e
  }

  def render(report: RepoReport, htmlMode: Boolean = false): String = {
    engine.layout(template, Map("repo" -> report, "htmlMode" -> htmlMode))
  }

  def render(reports: Seq[RepoReport], htmlMode: Boolean): String = {
    val r = reports.map(render(_, htmlMode)).mkString("\n")
    if (htmlMode) mdToHtml(r) else r
  }

  def renderJson(reports: Seq[RepoReport]): String = {
    reports.toJson.prettyPrint
  }

  def mdToHtml(report: String): String = {
    val options = new MutableDataSet()
    val parser = Parser.builder(options).build()
    val renderer = HtmlRenderer.builder(options).build()
    val doc = parser.parse(report)
    renderer.render(doc)
  }
}

object ClassPathResourceLoader extends ResourceLoader {
  override def resource(uri: String): Option[Resource] = {
    val text = IOUtils.resourceToString("/" + uri, UTF_8)
    Some(new StringTemplateSource(uri, text))
  }
}
