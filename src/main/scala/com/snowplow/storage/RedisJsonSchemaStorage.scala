package com.snowplow.storage

import com.redis.RedisClient
import com.snowplow.models.{DomainError, JsonSchema, RedisError, SerializationError}
import com.snowplow.utils.JsonSupport
import zio.{IO, ZIO}
import org.json4s.native.JsonMethods.parse
import cats.implicits._

class RedisJsonSchemaStorage(client: RedisClient) extends JsonSchemaStorage with JsonSupport {

  override val schemaStorage: JsonSchemaStorage.Service = new JsonSchemaStorage.Service {
    override def saveSchema(schema: JsonSchema): IO[DomainError, Unit] = {
      ZIO.fromEither {
        Either.cond(client.set(schema.schemaId, schema), (), RedisError("Failed while connecting to the redis client"))
      }
    }

    override def getSchema(schemaId: String): IO[DomainError, Option[JsonSchema]] = {
      client.get(schemaId) match {
        case Some(response) =>
          ZIO.fromEither(
            Either.catchNonFatal {
              Some(parse(response).extract[JsonSchema])
            }.leftMap(error => SerializationError(error))
          )
        case None => ZIO.none
      }

    }
  }
}
