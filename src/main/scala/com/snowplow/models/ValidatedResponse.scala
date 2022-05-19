package com.snowplow.models

final case class ValidatedResponse(
                                    action: String,
                                    id: String,
                                    status: String,
                                    message: Option[List[String]],
                                  )