package com.meltwater.fawn.auth

import org.http4s.{Header, HeaderKey, ParseFailure, ParseResult}
import org.http4s.util.{CaseInsensitiveString, Writer}
import cats.parse.{Parser => P}
import cats.implicits._

case class AWS4Authorization(
    algorithm: String,
    keyId: String,
    scope: String,
    signedHeaders: List[String],
    signature: String)
    extends Header.Parsed {
  override def key: HeaderKey = AWS4Authorization

  override def renderValue(writer: Writer): writer.type = {
    writer.append(algorithm)
    writer.append(" ")
    writer.append("Credential=")
    writer.append(keyId)
    writer.append("/")
    writer.append(scope)
    writer.append(", ")
    writer.append("SignedHeaders=")
    writer.append(signedHeaders.mkString(";"))
    writer.append(", ")
    writer.append("Signature=")
    writer.append(signature)
    writer
  }
}

object AWS4Authorization extends HeaderKey.Singleton {
  override type HeaderT = AWS4Authorization

  override def name: CaseInsensitiveString = CaseInsensitiveString("Authorization")

  override def matchHeader(header: Header): Option[AWS4Authorization] = header match {
    case Header.Raw(n, value) if n == name => parse(value).toOption
    case h: AWS4Authorization              => Some(h)
    case _                                 => None
  }

  val parser: P[AWS4Authorization] = for {
    alg   <- P.charsWhile(_ != ' ')
    _     <- P.string(" Credential=")
    key   <- P.charsWhile(_ != '/')
    _     <- P.char('/')
    scope <- P.charsWhile(_ != ',')
    _     <- P.string(", SignedHeaders=")
    hs    <- P.repSep(P.charsWhile(c => c != ';' && c != ','), P.char(';'))
    _     <- P.string(", Signature=")
    sig   <- P.charsWhile(_ != ' ')
    _     <- P.end
  } yield AWS4Authorization(alg, key, scope, hs.toList, sig)

  override def parse(s: String): ParseResult[AWS4Authorization] =
    parser.parseAll(s).leftMap { e =>
      ParseFailure("Could not parse an AWS4 Auth header", e.toString)
    }
}
