package com.snowplow.specs

import com.redis.RedisClient
import com.snowplow.storage.{JsonSchemaStorage, RedisJsonSchemaStorage}
import zio.Runtime
import zio.internal.Platform

// Test with redis storage
class JsonSchemaServiceISpec extends JsonSchemaServiceBehaviour {

  object IntegrationTestEnvironment extends JsonSchemaStorage {
    override val schemaStorage: JsonSchemaStorage.Service = new RedisJsonSchemaStorage(new RedisClient("localhost", 6379)).schemaStorage
  }

  override val myRuntime: zio.Runtime[JsonSchemaStorage] = Runtime(IntegrationTestEnvironment, Platform.default)
}
