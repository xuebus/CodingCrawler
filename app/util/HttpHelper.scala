package util

import java.io.InputStream
import java.util.Scanner

import org.apache.http.HttpStatus
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients

class HttpHelper {
  def downloadPageByGet(url: String): Option[String] = {
    val httpClient = HttpClients.createDefault()
    val httpGetter = new HttpGet(url)
    val response = httpClient.execute(httpGetter)

    if (response.getStatusLine.getStatusCode == HttpStatus.SC_OK) {
      var is: InputStream = null
      var sc: Scanner = null

      try {
        val entity = response.getEntity
        val buffer = new StringBuilder()
        is = entity.getContent
        sc = new Scanner(is)
        while (sc.hasNext) {
          buffer.append(sc.nextLine())
        }
        Some(buffer.toString())
      } catch {
        case ex: Exception =>
          if (is != null) is.close()
          if (sc != null) sc.close()
          if (response != null) response.close()
          None
      }
    } else None
  }
}
