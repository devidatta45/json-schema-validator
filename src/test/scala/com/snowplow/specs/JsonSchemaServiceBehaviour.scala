package com.snowplow.specs

import com.snowplow.models.{SchemaDoesNotExist, ValidatedResponse}
import com.snowplow.services.JsonSchemaService
import com.snowplow.storage.JsonSchemaStorage
import com.snowplow.utils.Helper
import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should
import zio.Runtime

// Behaviour based tests which only care about the service logic and not the internal storage
trait JsonSchemaServiceBehaviour extends AnyFlatSpec
  with Suite with should.Matchers
  with OptionValues with EitherValues {

  val myRuntime: Runtime[JsonSchemaStorage]

  behavior of "JsonSchemaService"

  it should "save schema correctly" in {
    val schemaId = "config-test"
    myRuntime.unsafeRun(JsonSchemaService.service.
      saveSchema(schemaId, Helper.inputSchema)) shouldBe ValidatedResponse("uploadSchema", schemaId, "success", None)
  }

  it should "fail to save in case of invalid schema" in {
    val schemaId = "config-test"
    myRuntime.unsafeRun(JsonSchemaService.service.
      saveSchema(schemaId, "Invalid_json")) shouldBe ValidatedResponse("uploadSchema", schemaId, "error", Some(List("Invalid Json")))
  }

  it should "get schema correctly" in {
    val schemaId = "config-test"
    myRuntime.unsafeRun(JsonSchemaService.service.
      saveSchema(schemaId, Helper.inputSchema)) shouldBe ValidatedResponse("uploadSchema", schemaId, "success", None)
    myRuntime.unsafeRun(JsonSchemaService.service.
      getSchema(schemaId)) shouldBe Helper.inputSchema
  }

  it should "give proper error if schema does not exist" in {
    val schemaId = "config-test-wrong"
    val errorResult = myRuntime.unsafeRun(JsonSchemaService.service.getSchema(schemaId).either)
    errorResult shouldBe Left(SchemaDoesNotExist(schemaId))
  }

  it should "validate json with schema correctly" in {
    val schemaId = "config-test"
    myRuntime.unsafeRun(JsonSchemaService.service.
      saveSchema(schemaId, Helper.inputSchema)) shouldBe ValidatedResponse("uploadSchema", schemaId, "success", None)
    myRuntime.unsafeRun(JsonSchemaService.service.
      validateJsonWithSchemaId(schemaId, Helper.inputJson)) shouldBe ValidatedResponse("validateDocument", schemaId, "success", None)
  }

  it should "give proper error for invalid json " in {
    val schemaId = "config-test"
    myRuntime.unsafeRun(JsonSchemaService.service.
      validateJsonWithSchemaId(schemaId, "Invalid_json")) shouldBe ValidatedResponse("validateDocument", schemaId, "error",
      Some(List("Invalid Json document")))
    myRuntime.unsafeRun(JsonSchemaService.service.
      validateJsonWithSchemaId(schemaId, Helper.inValidJson)) shouldBe ValidatedResponse("validateDocument", schemaId, "error",
      Some(List("#/chunks/number: expected type: Integer, found: String")))
  }
}
