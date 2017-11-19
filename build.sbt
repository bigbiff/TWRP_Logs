name := "TWRP_Logs"
version := "1.0"
scalaVersion := "2.11.11"
retrieveManaged := true

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.2.0" % Provided
libraryDependencies += "org.apache.spark" %% "spark-sql" % "2.2.0" % Provided
libraryDependencies += "com.maxmind.geoip2" % "geoip2" % "2.10.0"
assemblyShadeRules in assembly := Seq(
  ShadeRule.rename("com.fasterxml.jackson.core.**"       -> "shadedjackson.core.@1").inAll,
  ShadeRule.rename("com.fasterxml.jackson.annotation.**" -> "shadedjackson.annotation.@1").inAll,
  ShadeRule.rename("com.fasterxml.jackson.databind.**"   -> "shadedjackson.databind.@1").inAll
)