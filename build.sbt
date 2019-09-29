name := "Hermes"

version := "0.1"

resolvers += Resolver.sonatypeRepo("releases")
libraryDependencies += "com.github.alexarchambault" %% "case-app" % "2.0.0-M3"
libraryDependencies += "com.github.pureconfig" %% "pureconfig" % "0.11.1"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.25"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.10"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.10"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.5.25"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"