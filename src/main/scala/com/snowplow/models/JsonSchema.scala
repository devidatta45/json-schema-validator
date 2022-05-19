package com.snowplow.models

final case class JsonSchema(
                             schemaId: String,
                             validatedJsonSchema: String
                           )

final case class ValidatedResponse(
                                    action: String,
                                    id: String,
                                    status: String,
                                    message: Option[List[String]],
                                  )