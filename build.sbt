import org.scalajs.linker.interface.ModuleKind
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

ThisBuild / scalaVersion := "2.13.12"

val http4sVersion = "0.23.23"
val circeVersion = "0.14.5"
val laminarVersion = "16.0.0"

lazy val common = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("common"))
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core" % circeVersion,
      "io.circe" %%% "circe-generic" % circeVersion,
      "io.circe" %%% "circe-parser" % circeVersion,
    ),
  )

lazy val commonJVM = common.jvm
lazy val commonJS = common.js

lazy val backend = project
  .in(file("backend"))
  .dependsOn(commonJVM)
  .settings(
    name := "backend",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect" % "3.5.1",
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.mindrot" % "jbcrypt" % "0.4",
      "com.github.pureconfig" %% "pureconfig-core" % "0.17.4"
    ),
    // Копируем JS фронтенда в ресурсы backend
    Compile / resourceGenerators += Def.task {
      val _ = (frontend / Compile / fastOptJS).value
      val jsFile = (frontend / Compile / fastOptJS / artifactPath).value
      val targetDir = (Compile / resourceManaged).value / "web"
      IO.copyFile(jsFile, targetDir / "frontend.js")
      Seq(targetDir / "frontend.js")
    }.taskValue,
  )

lazy val frontend = project
  .in(file("frontend"))
  .enablePlugins(ScalaJSPlugin)
  .dependsOn(commonJS)
  .settings(
    name := "frontend",
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.ESModule) },
    libraryDependencies ++= Seq(
      "com.raquo" %%% "laminar" % laminarVersion,
      "org.scala-js" %%% "scalajs-dom" % "2.4.0",
      "com.lihaoyi" %%% "scalatags" % "0.12.0",
    ),
  )
