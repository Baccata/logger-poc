Global / scalaVersion := "2.13.14"
Global / run / fork := true
ThisBuild / organization := "io.github.baccata"

lazy val kernel = project
  .in(file("modules/kernel"))
  .settings(
    moduleName := "logger-kernel"
  )

lazy val frontend = project
  .in(file("modules/frontend"))
  .dependsOn(kernel)
  .settings(
    moduleName := "logger-frontend",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "sourcecode" % "0.3.1"
    )
  )

lazy val `log4cats` = project
  .in(file("modules/interop-log4cats"))
  .dependsOn(kernel)
  .settings(
    moduleName := "logger-log4cats",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "log4cats-core" % "2.6.0"
    )
  )

lazy val backend = project
  .in(file("modules/backend"))
  .dependsOn(kernel)
  .settings(
    moduleName := "logger-backend",
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-io" % "3.9.4"
    )
  )

lazy val test = project
  .in(file("modules/test"))
  .dependsOn(frontend, backend)
