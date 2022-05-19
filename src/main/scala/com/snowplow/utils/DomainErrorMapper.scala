package com.snowplow.utils

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives
import com.snowplow.models._

object DomainErrorMapper extends Directives with JsonSupport {
  val domainErrorMapper: ErrorMapper[DomainError] = {
    case RedisError(message, code) =>
      HttpResponse(StatusCodes.InternalServerError, entity = json4sToHttpEntityMarshaller(GenericErrorResponseBody(code, message)))

    case error: SerializationError =>
      HttpResponse(StatusCodes.BadRequest, entity = json4sToHttpEntityMarshaller(GenericErrorResponseBody(error.code, error.message)))

    case error: InvalidStoredJsonError =>
      HttpResponse(StatusCodes.BadRequest, entity = json4sToHttpEntityMarshaller(GenericErrorResponseBody(error.code, error.message)))

    case error: SchemaDoesNotExist =>
      HttpResponse(StatusCodes.InternalServerError, entity = json4sToHttpEntityMarshaller(GenericErrorResponseBody(error.code, error.message)))
  }

  case class GenericErrorResponseBody(code: String, message: String, errorDetails: Option[String] = None)

}
