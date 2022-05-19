package com.snowplow.storage

import com.snowplow.models.DomainError
import zio.IO

trait JsonSchemaStorage {

  val schemaStorage: JsonSchemaStorage.Service
}

object JsonSchemaStorage {
  trait Service {
    def saveSchema(schemaId: String, schema: String): IO[DomainError, Unit]

    def getSchema(schemaId: String): IO[DomainError, Option[String]]
  }
}