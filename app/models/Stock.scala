package models

import org.joda.time.DateTime

/**
 * Created by fatihdonmez on 17/09/15
 */
case class MarketEntry(date: DateTime,stocks: Map[String, Stock], exchangeRate: Map[Currency,Forex])
case class Stock(name: String, price: Double, currency: Currency)
case class Forex(base: Currency, to: Currency, rate: Double)
case class Currency(name: String, short: String)
case class StockOp(order: String)


object Market {

  val AvailableStocks = List(
    Stock("ABC",0,Currency.USD),
    Stock("DEF",0,Currency.GBP),
    Stock("XYZ",0,Currency.EURO))

  val StockOperations = Map("SELL" -> StockOp("SELL"), "BUY" -> StockOp("BUY"))

  private val data: scala.collection.mutable.Map[DateTime,MarketEntry] = scala.collection.mutable.Map()

  def addEntry(entry: MarketEntry): Unit = {
    data += (entry.date -> entry)
  }

  def getMarketEntry(date: DateTime): Option[MarketEntry] = data.get(date)

  def clear: Unit = {
    data clear
  }

  def size: Int = data.size

  def getMarketOpenDays: List[DateTime] = data.toList.map(pair => pair._1).sortBy(_.getMillis)(Ordering[Long])
}

object Currency {
  val USD = Currency("Usd","$")
  val EURO = Currency("Euro","€")
  val GBP = Currency("Gbd","£")
}