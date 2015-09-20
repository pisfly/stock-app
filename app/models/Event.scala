package models

import anorm._
import org.joda.time.format.DateTimeFormat
import play.api.db.DB
import service.TransactionService
import play.api.Play.current

/**
 * Created by fatihdonmez on 20/09/15
 */
case class Event(id: Option[Int], t: Transaction)

object Event {

  /**
   * Run transaction and register event for logging
   * @param e
   */
  def run(e: Event): Unit = {

    //TODO can have multiple event type ?
    TransactionService.handleTransaction(e.t)
    Event.register(e)
  }

  /**
   * Run multi[le transaction and register events for logging
   * @param eventList
   */
  def run(eventList: List[Event]): Unit = {
    eventList.foreach { e =>
      TransactionService.handleTransaction(e.t)
      Event.register(e)
    }
  }

  /**
   * Replay bunch of transaction event
   * @param eventList
   */
  def replay(eventList: List[Event]): Unit = {
    eventList.foreach { e =>
      TransactionService.handleTransaction(e.t)
    }
  }

  /**
   * Event register for EventSourcing purpose, it can be replayed later at some point
   * @param event
   */
  def register(event: Event) = {

    DB.withConnection { implicit connection =>

      val date = DateTimeFormat.forPattern("yyyy-MM-dd").print(event.t.opDate)

      SQL(
        """INSERT INTO transaction_events
          | (op_date,name,operation,quantity,price,currency,fx_rate) VALUES(
          | {op_date},
          | {name},
          | {operation},
          | {quantity},
          | {price},
          | {currency},
          | {fx_rate}
          |)""".
          stripMargin)
        .on(
          "op_date" -> date,
          "name" -> event.t.stock.name,
          "operation" -> event.t.op.order,
          "quantity" -> event.t.quantity,
          "price" -> event.t.stock.price,
          "currency" -> event.t.stock.currency.name,
          "fx_rate" -> event.t.fx.rate)
        .execute
    }
  }
}
