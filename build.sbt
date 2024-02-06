Global / scalaVersion := "3.3.0"

lazy val kernel = project.in(file("modules/kernel"))

lazy val frontend = project
  .in(file("modules/frontend"))
  .dependsOn(kernel)
  .settings(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "sourcecode" % "0.3.1"
    )
  )

lazy val `log4cats` = project
  .in(file("modules/interop-log4cats"))
  .dependsOn(kernel)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "log4cats-core" % "2.6.0"
    )
  )

lazy val backend = project
  .in(file("modules/backend"))
  .dependsOn(kernel)
  .settings(
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-core" % "3.9.4"
    )
  )
