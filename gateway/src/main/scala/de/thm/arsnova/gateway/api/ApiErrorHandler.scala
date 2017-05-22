package de.thm.arsnova.gateway.api

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler

import de.thm.arsnova.shared.Exceptions._

/*
This interface catches Exceptions that are thrown by any api route.
All exception handlers musst be implicit to automatically handle these.
*/
trait ApiErrorHandler {
  implicit def myExceptionHandler: ExceptionHandler = ExceptionHandler {
    case e: NoSuchElementException =>
      extractUri { uri =>
        complete(HttpResponse(NotFound, entity = s"Invalid id: ${e.getMessage}"))
      }

    case e: NoUserException =>
      complete(HttpResponse(Unauthorized, entity = s"No user given: ${e.getMessage}"))
  }
}
