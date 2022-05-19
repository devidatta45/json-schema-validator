package com.snowplow.specs

import com.snowplow.storage.JsonSchemaStorage
import com.snowplow.utils.InMemoryJsonSchemaStorage
import zio.Runtime
import zio.internal.Platform

// Test with in memory storage
class JsonSchemaServiceSpec extends JsonSchemaServiceBehaviour {

  object TestEnvironment extends JsonSchemaStorage {
    override val schemaStorage: JsonSchemaStorage.Service = InMemoryJsonSchemaStorage.schemaStorage
  }

  override val myRuntime: zio.Runtime[JsonSchemaStorage] = Runtime(TestEnvironment, Platform.default)
}
