package com.meltwater.fawn.auth

import org.http4s.{Header, HeaderKey, ParseFailure, ParseResult}
import org.http4s.util.{CaseInsensitiveString, Writer}

import java.time.{LocalDateTime, ZoneOffset, ZonedDateTime}
import java.time.format.{DateTimeFormatter, DateTimeFormatterBuilder}
import java.time.temporal.ChronoField._
import scala.util.Try
import cats.implicits._

final case class AmzDate(datetime: ZonedDateTime) extends Header.Parsed {
  override def key: HeaderKey = AmzDate

  override def renderValue(writer: Writer): writer.type =
    writer.append(AmzDate.renderZDT(datetime))
}

object AmzDate extends HeaderKey.Singleton {

  def renderZDT(zdt: ZonedDateTime): String =
    zdt.withZoneSameInstant(ZoneOffset.UTC).format(BASIC_ISO_DATETIME)

  val BASIC_ISO_DATETIME: DateTimeFormatter = new DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .parseLenient()
    .appendValue(YEAR, 4)
    .appendValue(MONTH_OF_YEAR, 2)
    .appendValue(DAY_OF_MONTH, 2)
    .appendLiteral('T')
    .appendValue(HOUR_OF_DAY, 2)
    .appendValue(MINUTE_OF_HOUR, 2)
    .appendValue(SECOND_OF_MINUTE, 2)
    .appendLiteral('Z')
    .toFormatter

  override type HeaderT = AmzDate

  override def name: CaseInsensitiveString = CaseInsensitiveString("x-amz-date")

  override def matchHeader(header: Header): Option[AmzDate] = if (header.name == name) {
    parse(header.value).toOption
  } else None

  override def parse(s: String): ParseResult[AmzDate] =
    Try {
      LocalDateTime.parse(s, BASIC_ISO_DATETIME).atZone(ZoneOffset.UTC)
    }.toEither
      .leftMap { t => ParseFailure("Could not parse an AmzDate", t.getMessage) }
      .map { AmzDate(_) }
}
