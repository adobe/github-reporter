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
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class MainITTests extends IntegrationTestBase {
  behavior of "MainIT"

  it should "render markdown report" in {
    val tmpFile = File.createTempFile("greporter", null)
    val args = Array("-t", token, "--org", testOrg, "--since", creationDateStr, "--out", tmpFile.getAbsolutePath)
    Main.main(args)
    val report = FileUtils.readFileToString(tmpFile, UTF_8)
    report should include("Create README-2.md")
    report should include("Test issue 1")

    println("-------")
    println(report)
    println("-------")
    FileUtils.forceDelete(tmpFile)
  }
}
