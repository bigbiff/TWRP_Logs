package TWRPLogs

import java.io.{File, FileInputStream, InputStream}
import java.net.{InetAddress, UnknownHostException}

import com.maxmind.geoip2._
import com.maxmind.geoip2.model.CityResponse
import com.maxmind.geoip2.record.Country
import com.maxmind.geoip2.DatabaseReader
import com.maxmind.geoip2.exception.AddressNotFoundException
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession

import scala.io.Source
import scala.util.{Failure, Success, Try}

object TWRPLogParser {
  def main(args: Array[String]): Unit = {
    val sparkSession = SparkSession.builder
      .appName("example")
      .getOrCreate()

    import sparkSession.implicits._
    val twrpDF = sparkSession.read.textFile(args(0))
      .map(_.split(" "))
      .map(t => createLogLine(t))
      .toDF()
//    val count = twrpDF.count()
//    val uniqCountries = twrpDF
//        .select(twrpDF.col("country"))
//      .distinct()

    val hadoopConf = new org.apache.hadoop.conf.Configuration()
    val jsonPath = "/user/hadoop/ips.json"
    val hdfs = org.apache.hadoop.fs.FileSystem.get(new java.net.URI("hdfs://localhost:9000"), hadoopConf)
    try { hdfs.delete(new org.apache.hadoop.fs.Path(jsonPath), true) } catch { case _ : Throwable => { } }
    twrpDF.write.format("json").save(jsonPath)
  }

  @throws(classOf[UnknownHostException])
  @throws(classOf[AddressNotFoundException])
  def getCountryForIp(ipAddress: String): String = {
    val dFile : File = new File("/datastore1/software/GeoLite2-City.mmdb")
    val database: InputStream = new FileInputStream(dFile)
    val reader : DatabaseReader = new DatabaseReader.Builder(database).build()
    val ipAdd : InetAddress = Try(InetAddress.getByName(ipAddress)) getOrElse InetAddress.getByName("127.0.0.1")
    val response = Try(reader.city(ipAdd)) getOrElse reader.city(InetAddress.getByName("4.4.4.4"))
    val country = response.getCountry
    country.getName
  }

  def createLogLine(t: Array[String]) : TWRPLog = {
    if (t.length < 13)
      TWRPLog("", "", "", "", 0, 0L, "", "", "")
    else
      TWRPLog(t(0), t(3), t(5), t(6), changeDataToInt(t(8)), changeDataToLong(t(9)),
        t(10), t(11), getCountryForIp(t(0)))
  }

  def changeDataToInt(obj: Any) = obj match {
    case n : Number => n.intValue()
    case _ => 0
  }

  def changeDataToLong(obj: Any) = obj match {
    case n : Number => n.longValue()
    case _ => 0L
  }
}
