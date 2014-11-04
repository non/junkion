name := "junkion"

organization := "us.mocul"

version := "0.0.2"

scalaVersion := "2.11.4"

crossScalaVersions := Seq("2.10.4", "2.11.4")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.1.3" % "test",
  "org.scalacheck" %% "scalacheck" % "1.11.3" % "test")

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature")

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

homepage := Some(url("http://github.com/non/junkion"))

seq(bintrayResolverSettings: _*)

seq(bintrayPublishSettings: _*)
