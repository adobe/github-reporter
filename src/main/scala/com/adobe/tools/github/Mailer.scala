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
import java.time.format.DateTimeFormatter
import java.util.Properties

import com.typesafe.config.{Config, ConfigFactory}
import javax.mail.Message.RecipientType
import javax.mail.internet.{InternetAddress, MimeMessage}
import javax.mail.{Authenticator, PasswordAuthentication, Session, Transport}
import pureconfig._
import pureconfig.generic.auto._
import scala.collection.JavaConverters._

case class MailConfig(host: String,
                      port: Int,
                      startTls: Boolean,
                      ssl: Boolean,
                      auth: Boolean,
                      username: String,
                      password: String,
                      from: String,
                      to: String,
                      subjectPrefix: String,
                      debug: Boolean)

class Mailer(config: MailConfig, content: String, subjectSuffix: String, html: Boolean) {

  def send(): Unit = {
    val props = mailProps()
    val session = Session.getInstance(props, authenticator())
    val msg = new MimeMessage(session)
    msg.setFrom(new InternetAddress(config.from))
    msg.setRecipient(RecipientType.TO, new InternetAddress(config.to))
    msg.setSubject(subject)
    msg.setContent(content, "text/html; charset=utf-8")
    Transport.send(msg)
  }

  private val subject = {
    if (config.subjectPrefix.isEmpty) subjectSuffix else config.subjectPrefix + " " + subjectSuffix
  }

  private def authenticator(): Authenticator = {
    if (config.auth) {
      new Authenticator {
        protected override def getPasswordAuthentication() =
          new PasswordAuthentication(config.username, config.password)
      }
    } else {
      null
    }
  }

  private def mailProps() = {
    val props = new Properties(System.getProperties)
    props.setProperty("mail.smtp.host", config.host)
    props.setProperty("mail.smtp.port", config.port.toString)
    props.setProperty("mail.smtp.auth", config.auth.toString)
    props.setProperty("mail.smtp.starttls.enable", config.startTls.toString)
    props
  }
}

object Mailer {

  def apply(content: String, html: Boolean, since: LocalDate, config: Config = ConfigFactory.load()): Mailer = {
    val mailConfig = ConfigSource.fromConfig(config).at("mail").loadOrThrow[MailConfig]
    new Mailer(mailConfig, content, subjectSuffix(since), html)
  }

  def apply(content: String, html: Boolean, since: LocalDate, props: Map[String, String]): Mailer = {
    val config = ConfigFactory.parseMap(props.asJava).withFallback(ConfigFactory.load())
    apply(content, html, since, config)
  }

  def subjectSuffix(since: LocalDate): String = {
    s"${str(since)} - ${str(LocalDate.now())}"
  }

  private def str(date: LocalDate) = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
}
