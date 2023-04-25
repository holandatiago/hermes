name := "Hermes"

version := "0.1"

resolvers += Resolver.sonatypeRepo("releases")
libraryDependencies += "com.github.alexarchambault" %% "case-app" % "2.1.0-M24"
libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.17.2"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.8.0"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.5.0"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.5.0"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.8.0"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.6"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.15" % "test"
libraryDependencies += "com.opengamma.strata" % "strata-measure" % "2.12.21"
libraryDependencies += "com.github.piotr-kalanski" % "splot" % "0.1.0"
libraryDependencies += "org.apache.spark" %% "spark-core" % "3.4.0"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.4.0" % "provided"