package models

import java.text.DecimalFormat

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._
import play.api.libs.json.Reads._

/**
 * Created by fatihdonmez on 15/12/15
 */
object JsonParsers {

  implicit val currencyFormat = Json.format[Currency]
  implicit val stockFormat = Json.format[Stock]
  implicit val stockOpFormat = Json.format[StockOp]
  implicit val forexFormat = Json.format[Forex]
  implicit val statFormat = Json.format[Stat]


  implicit val transactionWriteFormat = new Writes[Transaction] {
    override def writes(t: Transaction): JsValue = {
      val formatter = new DecimalFormat("#.####")
      JsObject(Seq(
        "quantity" -> JsNumber(t.quantity),
        "stock" -> Json.toJson(t.stock),
        "fx" -> Json.toJson(t.fx),
        "op" -> Json.toJson(t.op),
        "opDate" -> JsString(DateTimeFormat.forPattern("dd-MMM-yy").print(t.opDate)),
        "totalAmount" -> JsString(formatter.format(t.getTotalAmount)),
        "stat" -> Json.toJson(t.stat)
      ))
    }
  }

  implicit val transactionReadFormat = new Reads[Transaction] {

    def reads(json: JsValue): JsResult[Transaction] = {
      try {
        val opdateStr = (json \ "opdate").as[String]
        val stockName = (json \ "stock").as[String]
        val op = (json \ "op").as[String]
        val quantity = (json \ "quantity").as[Int]

        val opdate = DateTime.parse(opdateStr, DateTimeFormat.forPattern("dd-MMM-yy"))

        if (opdate.getMonthOfYear != 8 && opdate.getYear != 2015) {
          JsError("Invalid operation date")
        } else {

          Market getMarketEntry opdate match {
            case Some(e) => {

              var err: Option[JsError] = None

              if (e.stocks.get(stockName).isEmpty)
                err = Some(JsError("Invalid stock"))

              if (Market.StockOperations.get(op).isEmpty) {
                err match {
                  case Some(jsErr) => err = Some(jsErr ++ JsError("Invalid operation"))
                  case None => err = Some(JsError("Invalid operation"))
                }
              }

              err match {
                case Some(jsErr) => jsErr
                case None => {

                  val stock = e.stocks.get(stockName).get
                  val forex = e.exchangeRate.get(stock.currency).get
                  val stockOp = Market.StockOperations.get(op).get

                  JsSuccess(Transaction(stockOp, opdate, stock, quantity, forex, None))
                }
              }
            }
            case _ => JsError("Market data couldn't found")
          }
        }
      } catch {
        case e: Exception => JsError(e.getMessage)
      }
    }

  }
}
