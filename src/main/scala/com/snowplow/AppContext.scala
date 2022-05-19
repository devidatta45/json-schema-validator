package com.snowplow

import akka.actor.ActorSystem
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import com.redis.RedisClient
import com.snowplow.routes.JsonSchemaValidatorRoutes
import com.snowplow.storage.{JsonSchemaStorage, RedisJsonSchemaStorage}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

trait AppContext extends Directives {

  implicit def executionContext: ExecutionContext = system.dispatcher

  implicit def system: ActorSystem

  implicit def timeout: Timeout = Duration.fromNanos(100000)

  lazy val config = system.settings.config

  lazy val redisClient = new RedisClient(config.getString("redis.host"), config.getInt("redis.port"))

  // Live environment for the application with all required dependency
  object LiveEnvironment extends JsonSchemaStorage {
    override val schemaStorage: JsonSchemaStorage.Service = new RedisJsonSchemaStorage(redisClient).schemaStorage
  }

  lazy val routes: Route = new JsonSchemaValidatorRoutes(LiveEnvironment).routes
}