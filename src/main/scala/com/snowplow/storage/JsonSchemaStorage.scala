package com.snowplow.storage

import com.snowplow.models.{DomainError, JsonSchema}
import zio.IO

trait JsonSchemaStorage {

  val schemaStorage: JsonSchemaStorage.Service
}

object JsonSchemaStorage {
  trait Service {
    def saveSchema(schema: JsonSchema): IO[DomainError, Unit]

    def getSchema(schemaId: String): IO[DomainError, Option[JsonSchema]]
  }
}