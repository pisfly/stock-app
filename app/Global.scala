import models._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api._
import scala.io.{Codec, Source}

/**
 * Created by fatihdonmez on 17/09/15
 */
object Global extends GlobalSettings {

  override def onStart(application: Application) {

    Market.clear

    val stockHistoryDataFile = "./conf/data/stocks.txt"
    for (line <- Source.fromFile(stockHistoryDataFile)(Codec.UTF8).getLines) {

      val tokens = line.split("\\s+")

      val abcStock = Stock("ABC",tokens(1).substring(1).toDouble,Currency.USD)
      val defStock = Stock("DEF",tokens(2).substring(1).toDouble,Currency.GBP)
      val xyzStock = Stock("XYZ",tokens(3).substring(1).toDouble,Currency.EURO)
      val dateTime = DateTime.parse(tokens(0),DateTimeFormat.forPattern("dd-MMM-yy"))
      val forexMap = Map(
        Currency.USD -> Forex(Currency.USD, Currency.USD,1),
        Currency.GBP -> Forex(Currency.USD,Currency.GBP,tokens(4).toDouble),
        Currency.EURO -> Forex(Currency.USD,Currency.EURO,tokens(5).toDouble)
      )

      val entry = MarketEntry(
        dateTime,
        Map(
          abcStock.name -> abcStock,
          defStock.name -> defStock,
          xyzStock.name -> xyzStock
        ),
        forexMap)

      Market addEntry entry
    }
  }
}
