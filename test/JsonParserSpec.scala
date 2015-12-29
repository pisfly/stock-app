import models._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.specs2.mutable._
import models.JsonParsers._
import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.test.WithApplication

/**
 * Created by fatihdonmez on 29/12/15
 */
class JsonParserSpec extends Specification {

  "Json utility" should {

    "convert Transaction instance to valid json string" in {

      val transactionAsInstance =
        Transaction(
          StockOp("BUY"),
          DateTime.parse("01/01/2016",DateTimeFormat.forPattern("MM/dd/yyyy")),
          Market.AvailableStocks.head,
          20,
          Forex(Currency.USD,Currency.EURO,1.1),
          None
        )

      val transactionAsJsonStr = """{"quantity":20,"stock":{"name":"ABC","price":0,"currency":{"name":"Usd","short":"$"}},"fx":{"base":{"name":"Usd","short":"$"},"to":{"name":"Euro","short":"€"},"rate":1.1},"op":{"order":"BUY"},"opDate":"01-Jan-16","totalAmount":"0","stat":null}"""


      val jsonResult = Json.toJson(transactionAsInstance).toString
      jsonResult must equalTo(transactionAsJsonStr)
    }

    "parse valid json string into Transaction instance" in new WithApplication {

      val transactionAsJsonStr = """{"opdate" : "12-Aug-15",
                                   |"stock" : "ABC",
                                   |"op" : "BUY",
                                   |"quantity" : 2
                                   |}""".stripMargin


      Json.parse(transactionAsJsonStr).validate[Transaction] match {
        case t: JsSuccess[Transaction] => success
        case err: JsError => failure(err.errors.mkString)
      }

    }

    "not parse invalid json" in new WithApplication {

      val transactionAsJsonStr = """{"opdate" : "12Aug-15",
                                   |"stock" : "ABC",
                                   |"op" : "BUY",
                                   |"quantity" : 2
                                   |}""".stripMargin


      Json.parse(transactionAsJsonStr).validate[Transaction] match {
        case t: JsSuccess[Transaction] => failure
        case err: JsError => success
      }

    }
  }
}
