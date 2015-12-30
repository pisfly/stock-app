import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

/**
 * Created by fatihdonmez on 30/12/15
 */
class ApiSpec extends Specification {

  "Api" should {

    "send 404 on a bad request" in new WithApplication {
      route(FakeRequest(GET, "/invalidurl")) must beSome.which(status(_) == NOT_FOUND)
    }

    "send 400 on invalid transaction post request" in new WithApplication {

      val result = route(FakeRequest(POST,"/api")
        .withHeaders("Content-Type" -> "application/json")
        .withBody("""
             { "opdate" : "12Aug-15",
               |"stock" : "ABC",
               |"op" : "BUY",
               |"quantity" : 2
               |}""".stripMargin)).get


      status(result) must equalTo(BAD_REQUEST)
      contentType(result) must beSome("application/json")

    }

    "send 200 on valid transaction post request" in new WithApplication {

      val result = route(FakeRequest(POST,"/api")
        .withHeaders("Content-Type" -> "application/json")
        .withBody("""
             { "opdate" : "12-Aug-15",
                    |"stock" : "ABC",
                    |"op" : "BUY",
                    |"quantity" : 2
                    |}""".stripMargin)).get


      status(result) must equalTo(OK)

    }
  }

  "return list of existing transactions on get /api request" in new WithApplication {

    val result = route(FakeRequest(GET,"/api")).get
    status(result) must equalTo(OK)

  }

  "return meta data on get /api/meta request" in new WithApplication {

    val result = route(FakeRequest(GET,"/api/meta")).get
    status(result) must equalTo(OK)

    (contentAsJson(result) \ "availableStocks").toEither match {
      case Left(err) => failure(err.messages.mkString)
      case Right(value) => success
    }

    (contentAsJson(result) \ "openDays").toEither match {
      case Left(err) => failure(err.messages.mkString)
      case Right(value) => success
    }

  }

}
