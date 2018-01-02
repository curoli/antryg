package antryg.cql.builder

import com.datastax.driver.core.RegularStatement

trait CqlStatement {

  def asJava: RegularStatement

  def asQueryString: String = asJava.getQueryString

}
