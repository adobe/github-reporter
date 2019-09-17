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

import java.io.File
import java.nio.charset.StandardCharsets.UTF_8

import org.apache.commons.io.FileUtils
import spray.json._
import spray.json.DefaultJsonProtocol._

/**
 * This is a utility class to render from json serialized report file.
 * This can be used to quickly try out various rendition changes which do not imapct the data model
 * without incurring remote calls delay
 *
 * You can generate the json format file via
 *
 * java -jar github-reporter.jar --since "2 days" --json-mode apache/openwhisk
 */
object SerializedReportRenderer extends App {
  val file = new File("report.json")
  val json = FileUtils.readFileToString(file, UTF_8).parseJson
  val reports = json.convertTo[Seq[RepoReport]]

  val reportRenderer = new ReportRenderer()
  val report = reportRenderer.render(reports, htmlMode = false)
  println(report)
}
