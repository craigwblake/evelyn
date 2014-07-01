scalaJSSettings

name := "client"

ScalaJSKeys.persistLauncher := true

ScalaJSKeys.persistLauncher in Test := false

//libraryDependencies += "org.webjars" % "jquery" % "2.1.1"

libraryDependencies += "org.scala-lang.modules.scalajs" %%% "scalajs-dom" % "0.6"

//libraryDependencies += "org.scalajs" %%% "scalajs-pickling" % "0.3"

libraryDependencies += "org.scala-lang.modules.scalajs" %% "scalajs-jasmine-test-framework" % scalaJSVersion % "test"
