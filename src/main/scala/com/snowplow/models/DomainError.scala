package com.snowplow.models

sealed trait DomainError extends Throwable {
  def code: String

  def message: String
}

case class RedisError(override val message: String,
                      override val code: String = DomainError.REDIS_ERROR) extends DomainError

final case class SerializationError(error: Throwable,
                                    override val code: String = DomainError.SERIALIZATION_ERROR) extends DomainError {
  override def message: String = s"Serialization failed while parsing data from storage with error ${error.getMessage} "
}

final case class InvalidStoredJsonError(schemaId: String,
                                        override val code: String = DomainError.INVALID_JSON) extends DomainError {
  override def message: String = s"The stored json with id $schemaId is invalid"
}

final case class SchemaDoesNotExist(schemaId: String,
                                    override val code: String = DomainError.SCHEMA_DOES_NOT_EXIST) extends DomainError {
  override def message: String = s"Schema with id $schemaId does not exist "
}

object DomainError {
  val REDIS_ERROR = "REDIS_ERROR"
  val SERIALIZATION_ERROR = "SERIALIZATION_ERROR"
  val INVALID_JSON = "INVALID_JSON"
  val SCHEMA_DOES_NOT_EXIST = "SCHEMA_DOES_NOT_EXIST"
}
