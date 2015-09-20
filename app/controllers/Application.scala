package controllers

import models.{Event, Market, Transaction}
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import service.TransactionService

class Application extends Controller {

  val transactionForm = Form(
    tuple(
      "opdate" -> text.verifying("Invalid operation date",dateStr => {
        try {
          val opdate = DateTime.parse(dateStr, DateTimeFormat.forPattern("dd-MMM-yy"))
          opdate.getMonthOfYear == 8 || opdate.getYear == 2015
        } catch {
          case e: Exception => false
        }
      }),
      "stock" -> text.verifying("Invalid stock",stock => Market.AvailableStocks.exists(s => s.name == stock)),
      "op" -> text.verifying("Invalid operation",opStr => Market.StockOperations.exists(op => op._1 == opStr)),
      "quantity" -> number
    )
  )

  def index = Action {
    val transactionList = Transaction.getAll
    Ok(views.html.index(transactionList,transactionForm))
  }

  def transaction = Action { implicit request =>
    transactionForm.bindFromRequest.fold(
      formWithErrors => {
        val transactionList = Transaction.getAll
        BadRequest(views.html.index(transactionList,formWithErrors))
      },
      transactionData => {

        val opdate = DateTime.parse(transactionData._1,DateTimeFormat.forPattern("dd-MMM-yy"))
        Market getMarketEntry opdate match {
          case Some(e) => {

            //Assume market stock and currency data accurate
            //Otherwise need to handle Option
            val stock = e.stocks.get(transactionData._2).get
            val forex = e.exchangeRate.get(stock.currency).get
            val stockOp = Market.StockOperations.get(transactionData._3).get
            val transaction = Transaction(stockOp,opdate,stock,transactionData._4,forex,None)

            try {
              Event.run(Event(None,transaction))
              Ok(views.html.index(Transaction.getAll,transactionForm))
            } catch {
              case e: Exception => BadRequest(views.html.index(Transaction.getAll,transactionForm.withError("quantity",e.getMessage)))
            }

          }
          case None => InternalServerError("Some unfortunate error")
        }
      }
    )
  }

}
