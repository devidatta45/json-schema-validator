package com.snowplow.routes

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives
import com.snowplow.models.DomainError
import com.snowplow.services.JsonSchemaService
import com.snowplow.storage.JsonSchemaStorage
import com.snowplow.utils.{DomainErrorMapper, ErrorMapper, JsonSupport, ZioToRoutes}
import org.json4s.JObject
import zio.internal.Platform

import scala.concurrent.ExecutionContext

class JsonSchemaValidatorRoutes(env: JsonSchemaStorage)(
  implicit executionContext: ExecutionContext,
  system: ActorSystem,
) extends ZioToRoutes[JsonSchemaStorage] with Directives with JsonSupport {

  override def environment: JsonSchemaStorage = env

  override def platform: Platform = Platform.default

  private lazy val service = JsonSchemaService.service

  implicit val errorMapper: ErrorMapper[DomainError] = DomainErrorMapper.domainErrorMapper

  val routes = pathPrefix("schema" / Segment) { schemaId =>
    post {
      // Even though service validate the json it's a bad practice to allow everything in the rest layer.
      // Therefore allowing only json object
      entity(as[JObject]) { jsonSchema =>
        for {
          serviceResponse <- service.saveSchema(schemaId, jsonSchema)
          statusCode = serviceResponse.status match {
            case "success" => StatusCodes.Created
            case "error" => StatusCodes.BadRequest
          }
        } yield complete(
          statusCode,
          serviceResponse
        )
      }
    } ~ get {
      for {
        schema <- service.getSchema(schemaId)
      } yield complete(
        StatusCodes.OK,
        schema
      )
    }
  } ~ pathPrefix("validate" / Segment) { schemaId =>
    post {
      // Even though service validate the json it's a bad practice to allow everything in the rest layer.
      // Therefore allowing only json object
      entity(as[JObject]) { rawJson =>
        for {
          serviceResponse <- service.validateJsonWithSchemaId(schemaId, rawJson)
          statusCode = serviceResponse.status match {
            case "success" => StatusCodes.Created
            case "error" => StatusCodes.InternalServerError
          }
        } yield complete(
          statusCode,
          serviceResponse
        )
      }
    }
  }
}
