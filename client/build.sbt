scalaJSSettings

name := "client"

ScalaJSKeys.persistLauncher := true

ScalaJSKeys.persistLauncher in Test := false

skip in ScalaJSKeys.packageJSDependencies := false

libraryDependencies += "org.webjars" % "jquery" % "2.1.1"

libraryDependencies += "org.scala-lang.modules.scalajs" %%% "scalajs-jquery" % "0.6"

libraryDependencies += "org.scala-lang.modules.scalajs" %%% "scalajs-dom" % "0.6"

libraryDependencies += "org.scala-lang.modules.scalajs" %% "scalajs-jasmine-test-framework" % scalaJSVersion % "test"
