package com.snowplow

import org.json4s.JObject
import org.json4s.native.JsonMethods
import org.json4s.native.JsonMethods.compact

import scala.language.implicitConversions

package object routes {

  implicit def convertJobjectToString(obj: JObject) = compact(JsonMethods.render(obj))
}
