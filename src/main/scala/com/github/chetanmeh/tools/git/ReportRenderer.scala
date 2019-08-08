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

import java.nio.charset.StandardCharsets.UTF_8

import org.apache.commons.io.IOUtils
import org.fusesource.scalate._
import org.fusesource.scalate.support.StringTemplateSource

class ReportRenderer(template: String = "repo-report.ssp") {
  private val engine = new TemplateEngine
  private val source = new StringTemplateSource(template, IOUtils.resourceToString("/" + template, UTF_8))

  def render(report: RepoReport): String = {
    engine.layout(source, Map("repo" -> report))
  }

  def render(reports: Seq[RepoReport]): String = {
    reports.map(render).mkString("\n")
  }
}