name := "scala-rabbit-utils"
version := "1.0"
scalaVersion := "2.11.7"

resolvers ++= Seq(
  "SpinGo OSS" at "http://spingo-oss.s3.amazonaws.com/repositories/releases"
)

val akkaVersion = "2.4-M1"
val opRabbitVersion = "1.0.0-RC3"

libraryDependencies ++= Seq(
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.thenewmotion.akka" %% "akka-rabbitmq" % "1.2.4",
  "com.spingo" %% "op-rabbit-core"        % opRabbitVersion,
  "com.typesafe.play" %% "play-json" % "2.4.2"
)

scalacOptions ++= Seq (
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",                
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-unchecked",
 // "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",   
  "-Ywarn-value-discard",
  "-Ywarn-unused-import",
  "-Xfuture"
)

