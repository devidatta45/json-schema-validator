package com.snowplow.storage

import com.redis.RedisClient
import com.snowplow.models._
import zio.{IO, ZIO}

class RedisJsonSchemaStorage(client: RedisClient) extends JsonSchemaStorage {

  override val schemaStorage: JsonSchemaStorage.Service = new JsonSchemaStorage.Service {
    override def saveSchema(schemaId: String, schema: String): IO[DomainError, Unit] = {
      ZIO.fromEither {
        Either.cond(client.set(schemaId, schema), (), RedisError("Failed while connecting to the redis client"))
      }
    }

    override def getSchema(schemaId: String): IO[DomainError, Option[String]] = {
      ZIO.succeed(
        client.get(schemaId)
      )
    }
  }
}
