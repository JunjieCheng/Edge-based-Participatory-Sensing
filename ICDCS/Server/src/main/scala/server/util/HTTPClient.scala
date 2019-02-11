package server.util

import scalaj.http.{Http, HttpResponse}

object HTTPClient {

  def get(url:String, params: Map[String, String]): String = {
    val response: HttpResponse[String] = Http(url).params(params).asString
    print(response.statusLine + ": " + response.body + "\n")
    response.body
  }
}
