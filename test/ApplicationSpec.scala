import models.Market
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

/**
 * Functional tests for Stock App
 * It tests api through fake requests 
 **/
@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {

  "Application" should {

    "send 404 on a bad request" in new WithApplication {
      route(FakeRequest(GET, "/boum")) must beSome.which (status(_) == NOT_FOUND)
    }

    "have stock data" in new WithApplication {

      Market.size mustEqual(31)
    }

    "returns 200 for valid transaction requests for BUY" in new WithApplication {
      val request = FakeRequest(POST,"/").withFormUrlEncodedBody(
        "opdate" -> "01-Aug-15","stock" -> "DEF","op" -> "BUY","quantity" -> "10")

      val Some(result) = route(request)
      status(result) must equalTo(OK)
    }

    "returns 200 for valid transaction requests for SELL" in new WithApplication {
      val request = FakeRequest(POST,"/").withFormUrlEncodedBody(
        "opdate" -> "06-Aug-15","stock" -> "DEF","op" -> "SELL","quantity" -> "10")

      val Some(result) = route(request)
      status(result) must equalTo(OK)
    }

    "returns 400 for valid transaction requests for SELL" in new WithApplication {
      val request = FakeRequest(POST,"/").withFormUrlEncodedBody(
        "opdate" -> "06-Aug-15","stock" -> "DEF","op" -> "SELL","quantity" -> "10")

      val Some(result) = route(request)
      status(result) must equalTo(BAD_REQUEST)
    }

    "returns 400 for invalid transaction requests" in new WithApplication {
      val request = FakeRequest(POST,"/").withFormUrlEncodedBody(
        "opdate" -> "day1","stock" -> "DEF","op" -> "BUY","quantity" -> "sds4")

      val Some(result) = route(request)
      status(result) must equalTo(BAD_REQUEST)
    }
  }
}
