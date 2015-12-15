package service


import java.sql.Connection

import anorm.SqlParser._
import models._
import org.joda.time.format.DateTimeFormat
import play.api.db.DB
import anorm._
import play.api.Play.current

/**
 * Created by fatihdonmez on 18/09/15
 */
object TransactionService {

  val INSERT_SQL = """INSERT INTO user_transaction
                     | (op_date,name,operation,quantity,price,currency,fx_rate,total_cost,profit_loss,cumulative_profit_loss) VALUES(
                     | {op_date},
                     | {name},
                     | {operation},
                     | {quantity},
                     | {price},
                     | {currency},
                     | {fx_rate},
                     | {total_cost},
                     | {profit_loss},
                     | {cumulative_profit}
                     |)""".stripMargin

  val SELECT_SQL = "SELECT * FROM user_transaction WHERE name={name}"
  val SELECT_TRANSACTION_SUM_SQL = "SELECT sum(profit_loss) FROM user_transaction"

  /**
   * Handle transaction
   * Calculate profit or loss and insert into transaction table
   * @param transaction
   */
  def handleTransaction(transaction: Transaction): Unit = {
    DB.withTransaction { implicit connection =>

      val transactionList = getTransactionHistoryByName(transaction.stock.name)

      var profitOrLoss = 0.0
      var cumulativeProfitOrLoss = getCumulativeProfitOrLoss

      val availableStocks = transactionList.foldLeft((0, 0.0))((count, t) => {
        if (t.op == StockOp("BUY"))
          (count._1 + t.quantity, count._2 - t.getTotalAmount)
        else
          (count._1 - t.quantity, count._2 + t.getTotalAmount)
      })

      transaction.op match {
        case StockOp("BUY") => {
          //No need for extra operations
        }
        case StockOp("SELL") => {

          if (availableStocks._1 < transaction.quantity)
            throw new Exception("There aren't enough stocks for this operation, available [" + availableStocks._1 + "]")
          else if (availableStocks._1 == transaction.quantity) {
            profitOrLoss = availableStocks._2 + transaction.getTotalAmount
            cumulativeProfitOrLoss += profitOrLoss
          }
        }
      }

      insert(transaction, profitOrLoss, cumulativeProfitOrLoss)
    }
  }

  private def getTransactionHistoryByName(name: String)
                                         (implicit connection: Connection): List[Transaction] = {
    SQL(SELECT_SQL)
      .on("name" -> name)
      .as(Transaction.transactionParser *)
  }

  private def getCumulativeProfitOrLoss(implicit connection: Connection): Double = {
    SQL(SELECT_TRANSACTION_SUM_SQL).as(scalar[Double].singleOpt).getOrElse(0)
  }

  private def insert(transaction: Transaction, profitOrLoss: Double, cumulativeProfitOrLoss: Double)
                    (implicit connection: Connection): Unit = {

    val date = DateTimeFormat.forPattern("yyyy-MM-dd").print(transaction.opDate)

    SQL(INSERT_SQL)
      .on(
        "op_date" -> date,
        "name" -> transaction.stock.name,
        "operation" -> transaction.op.order,
        "quantity" -> transaction.quantity,
        "price" -> transaction.stock.price,
        "currency" -> transaction.stock.currency.name,
        "fx_rate" -> transaction.fx.rate,
        "total_cost" -> transaction.getTotalAmount,
        "profit_loss" -> profitOrLoss,
        "cumulative_profit" -> cumulativeProfitOrLoss)
      .execute
  }
}
