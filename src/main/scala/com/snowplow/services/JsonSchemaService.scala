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
      parse(rawJsonSchema) match {
        case Right(_) =>
          val jsonSchema = JsonSchema(schemaId, rawJsonSchema)
          ZIO.accessM[JsonSchemaStorage](_.schemaStorage.saveSchema(jsonSchema))
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
          case Some(response) => ZIO.fromEither(response.validatedJsonSchema.asRight)
          case None => ZIO.fromEither(SchemaDoesNotExist(schemaId).asLeft)
        }
      } yield finalResponse
    }

    override def validateJsonWithSchemaId(schemaId: String, rawJson: String): ZIO[JsonSchemaStorage, DomainError, ValidatedResponse] = {
      parse(rawJson) match {
        case Right(_) =>
          for {
            schemaResponse <- ZIO.accessM[JsonSchemaStorage](_.schemaStorage.getSchema(schemaId))
            validSchema <- schemaResponse match {
              case Some(response) =>
                ZIO.fromTry {
                  Schema.loadFromString(response.validatedJsonSchema)
                }.mapError(_ => InvalidStoredJsonError(schemaId))

              case None => ZIO.fromEither(SchemaDoesNotExist(schemaId).asLeft)
            }
            json = parse(rawJson).getOrElse(Json.Null).dropNullValues
            validatedJson = validSchema.validate(json)
            finalResult = if (validatedJson.isValid)
              ValidatedResponse("validateDocument", schemaId, "success", None)
            else {
              val errors = validatedJson.swap.map(_.toList).toList.flatten.map(_.getMessage)
              ValidatedResponse("validateDocument", schemaId, "error", Some(errors))
            }
          } yield finalResult
        case Left(_) =>
          ZIO.fromEither {
            ValidatedResponse("validateDocument", schemaId, "error", Some(List("Invalid Json document"))).asRight
          }
      }
    }

  }
}