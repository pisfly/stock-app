package models

import anorm.SqlParser._
import anorm._
import org.joda.time.DateTime
import play.api.db.DB
import play.api.Play.current

/**
 * Created by fatihdonmez on 18/09/15
 */
case class Transaction(op: StockOp, opDate: DateTime, stock: Stock, quantity: Int, fx: Forex, stat: Option[Stat]) {
  def getTotalAmount: Double = stock.price*quantity*fx.rate
}

case class Stat(current: Double, cumulative: Double)

object Transaction {

  val SELECT_SQL = "SELECT * FROM user_transaction ORDER BY op_date DESC"

  val transactionParser =
    get[DateTime]("op_date") ~
    get[String]("name") ~
    get[String]("operation") ~
    get[Int]("quantity") ~
    get[Double]("price") ~
    get[String]("currency") ~
    get[Double]("fx_rate") ~
    get[Double]("total_cost") ~
    get[Double]("profit_loss") ~
    get[Double]("cumulative_profit_loss") map {
    case opDate ~ name ~ op ~ quantity ~ price ~ currency ~ fxRate ~ totalCost ~ profitLoss ~ cumulativeProfit => {

      val currencyObj = currency match {
        case Currency.USD.name => Currency.USD
        case Currency.GBP.name => Currency.GBP
        case Currency.EURO.name => Currency.EURO
      }

      Transaction(StockOp(op),opDate,
                  Stock(name,price,currencyObj),quantity,
                  Forex(Currency.USD,currencyObj,fxRate),
                  Some(Stat(profitLoss,cumulativeProfit)))
    }
  }

  def getAll: List[Transaction] = {
    DB.withConnection { implicit connection =>
        SQL(SELECT_SQL)
        .as(transactionParser *)
    }
  }
}
