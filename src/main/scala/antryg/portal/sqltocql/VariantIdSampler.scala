package antryg.portal.sqltocql

trait VariantIdSampler extends (String => Boolean) {

}

object VariantIdSampler {
  val acceptAll: VariantIdSampler = (_: String) => true

  def decimateBy(factor: Int): VariantIdSampler = (id: String) => (id.hashCode % factor) == 0
}
