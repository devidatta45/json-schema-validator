package com.snowplow.services

import cats.implicits._
import com.snowplow.models._
import com.snowplow.storage.JsonSchemaStorage
import io.circe._
import io.circe.parser._
import io.circe.schema.Schema
import zio.ZIO


trait JsonSchemaService {

  def saveSchema(schemaId: String, rawJsonSchema: String): ZIO[JsonSchemaStorage, DomainError, ValidatedResponse]

  def getSchema(schemaId: String): ZIO[JsonSchemaStorage, DomainError, String]

  def validateJsonWithSchemaId(schemaId: String, rawJson: String): ZIO[JsonSchemaStorage, DomainError, ValidatedResponse]
}

object JsonSchemaService {
  val service = new JsonSchemaService {
    override def saveSchema(schemaId: String, rawJsonSchema: String): ZIO[JsonSchemaStorage, DomainError, ValidatedResponse] = {
      // Validating json here even though from the route side only valid json can come because service method should be self-sufficient
      // for handling such errors. In future if it will be called via some queue(not from current endpoint) with raw string it will validate
      // as expected
      parse(rawJsonSchema) match {
        case Right(_) =>
          ZIO.accessM[JsonSchemaStorage](_.schemaStorage.saveSchema(schemaId, rawJsonSchema))
            .map(_ => ValidatedResponse("uploadSchema",
              schemaId, "success", None))
        case Left(_) =>
          ZIO.fromEither {
            ValidatedResponse("uploadSchema", schemaId, "error", Some(List("Invalid Json"))).asRight
          }
      }
    }

    override def getSchema(schemaId: String): ZIO[JsonSchemaStorage, DomainError, String] = {
      for {
        schemaResponse <- ZIO.accessM[JsonSchemaStorage](_.schemaStorage.getSchema(schemaId))
        finalResponse <- schemaResponse match {
          case Some(response) => ZIO.fromEither(response.asRight)
          case None => ZIO.fromEither(SchemaDoesNotExist(schemaId).asLeft)
        }
      } yield finalResponse
    }

    override def validateJsonWithSchemaId(schemaId: String, rawJson: String): ZIO[JsonSchemaStorage, DomainError, ValidatedResponse] = {
      // Validating json here even though from the route side only valid json can come because service method should be self-sufficient
      // for handling such errors. In future if it will be called via some queue(not from current endpoint) with raw string it will validate
      // as expected
      parse(rawJson) match {
        case Right(_) =>
          for {
            schemaResponse <- ZIO.accessM[JsonSchemaStorage](_.schemaStorage.getSchema(schemaId))
            validSchema <- schemaResponse match {
              case Some(response) =>
                ZIO.fromTry {
                  Schema.loadFromString(response)
                }.mapError(_ => InvalidStoredJsonError(schemaId))

              case None => ZIO.fromEither(SchemaDoesNotExist(schemaId).asLeft)
            }
            finalResult = validateSchema(validSchema, rawJson, schemaId)
          } yield finalResult
        case Left(_) =>
          ZIO.fromEither {
            ValidatedResponse("validateDocument", schemaId, "error", Some(List("Invalid Json document"))).asRight
          }
      }
    }

    private def validateSchema(schema: Schema, rawJson: String, schemaId: String): ValidatedResponse = {
      val json = parse(rawJson).getOrElse(Json.Null).deepDropNullValues
      val validatedJson = schema.validate(json)
      if (validatedJson.isValid)
        ValidatedResponse("validateDocument", schemaId, "success", None)
      else {
        val errors = validatedJson.swap.map(_.toList).toList.flatten.map(_.getMessage)
        ValidatedResponse("validateDocument", schemaId, "error", Some(errors))
      }
    }

  }
}