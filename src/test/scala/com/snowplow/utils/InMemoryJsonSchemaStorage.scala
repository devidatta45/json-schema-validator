package com.snowplow.utils

import com.snowplow.models.DomainError
import com.snowplow.storage.JsonSchemaStorage
import com.snowplow.utils.InMemoryJsonSchemaStorage.SchemaStore
import zio.Runtime.default
import zio.{IO, Ref}

trait InMemoryJsonSchemaStorage extends JsonSchemaStorage {

  override val schemaStorage: JsonSchemaStorage.Service = new JsonSchemaStorage.Service {

    val ref: Ref[SchemaStore] = default.unsafeRun(Ref.make(SchemaStore(Map())))

    override def saveSchema(schemaId: String, schema: String): IO[DomainError, Unit] = {
      ref.modify(_.save(schemaId, schema))
    }

    override def getSchema(schemaId: String): IO[DomainError, Option[String]] = {
      ref.modify(_.get(schemaId))
    }
  }
}

object InMemoryJsonSchemaStorage extends InMemoryJsonSchemaStorage {
  final case class SchemaStore(storage: Map[String, String]) {
    def save(schemaId: String, schema: String): (Unit, SchemaStore) = {
      ((), copy(storage = storage + (schemaId -> schema)))
    }

    def get(schemaId: String): (Option[String], SchemaStore) = (storage.get(schemaId), this)
  }
}