package controllers

import models.{Market, Event, Transaction}
import models.JsonParsers._
import org.joda.time.format.DateTimeFormat
import play.api.libs.json.{JsString, JsArray, JsError, Json}
import play.api.mvc.{Action, Controller}

/**
 * Created by fatihdonmez on 15/12/15
 */
class Api extends Controller {

  def list = Action {
    Ok(Json.toJson(Transaction.getAll))
  }

  def getMetaData = Action {

    val dateTimeFormatter = DateTimeFormat.forPattern("dd-MMM-yy")

    Ok(Json.obj(
      "availableStocks" -> Json.toJson(Market.AvailableStocks),
      "openDays" -> Json.toJson(Market.getMarketOpenDays.map(day => dateTimeFormatter.print(day)))))
  }

  def transaction = Action(parse.json) { request =>
    val transactionResult = request.body.validate[Transaction]

    transactionResult.fold(
      errors => {
        BadRequest(JsError.toJson(errors))
      },
      transaction => {

        try {
          Event.run(Event(None,transaction))
          Ok(Json.toJson(Transaction.getAll))
        } catch {
          case e: Exception => BadRequest(JsError.toJson(JsError(e.getMessage)))
        }
      }
    )

  }

}
