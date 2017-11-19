package TWRPLogs

case class TWRPLog (
                   ipAddress: String,
                   timeStamp: String,
                   httpMethod: String,
                   httpURL: String,
                   httpCode: Int,
                   Bytes: Long,
                   referrer: String,
                   userAgent: String,
                   country: String
                   )
