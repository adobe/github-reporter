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

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FlatSpec, Matchers}

@RunWith(classOf[JUnitRunner])
class MainTests extends FlatSpec with Matchers {
  behavior of "Main"

  it should "parse since date" in {
    Main.parseSinceDate("2019-11-01") shouldBe LocalDate.of(2019, 11, 1)
    Main.parseSinceDate("2 days") shouldBe LocalDate.now().minusDays(2)
  }

  it should "adapt uri" in {
    Main.adaptUri("https://api.github.com") shouldBe "https://api.github.com"
    Main.adaptUri("https://git.example.com") shouldBe "https://git.example.com/api/v3"
    Main.adaptUri("https://git.example.com/") shouldBe "https://git.example.com/api/v3"
  }
}
