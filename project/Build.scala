import sbt._
import Keys._
import play.Play._
import scala.scalajs.sbtplugin.ScalaJSPlugin._
import ScalaJSKeys._
import com.typesafe.sbt.packager.universal.UniversalKeys
import com.typesafe.sbteclipse.core.EclipsePlugin.EclipseKeys

object Versions {
	val app = "0.1.0-SNAPSHOT"
	val scala = "2.11.1"
	val dom = "0.6"
}

object ApplicationBuild extends Build with UniversalKeys {

	val clientOutputDir = Def.settingKey[File]("directory for javascript files output by client")

	override def rootProject = Some(webapp)

	val sharedSrcDir = "shared"

	lazy val webapp = Project(
		id = "webapp",
		base = file("webapp")
	) enablePlugins (play.PlayScala) settings (webappSettings: _*) aggregate (shared, client)

	lazy val client = Project(
		id   = "client",
		base = file("client")
	) settings (clientSettings: _*)

	lazy val shared = Project(
		id = "shared",
		base = file(sharedSrcDir)
	) settings (sharedSettings: _*)

	lazy val webappSettings =
		Seq(
			name := "webapp",
			version := Versions.app,
			scalaVersion := Versions.scala,
			clientOutputDir := (crossTarget in Compile).value / "classes" / "public" / "javascripts",
			compile in Compile <<= (compile in Compile) dependsOn (fastOptJS in (client, Compile)),
			dist <<= dist dependsOn (fullOptJS in (client, Compile)),
			commands += preStartCommand,
			EclipseKeys.skipParents in ThisBuild := false
		) ++ (
			// ask client project to put its outputs in clientOutputDir
			Seq(packageExternalDepsJS, packageInternalDepsJS, packageExportedProductsJS, packageLauncher, fastOptJS, fullOptJS) map { packageJSKey =>
				crossTarget in (client, Compile, packageJSKey) := clientOutputDir.value
			}
		) ++ sharedDirectorySettings

	lazy val clientSettings =
		scalaJSSettings ++ Seq(
			name := "client",
			version := Versions.app,
			scalaVersion := Versions.scala,
			persistLauncher := true,
			persistLauncher in Test := false
		) ++ sharedDirectorySettings

	lazy val sharedSettings =
		Seq(
			name := "shared",
			scalaSource in Compile := baseDirectory.value,
			EclipseKeys.skipProject := true
		)

	lazy val sharedDirectorySettings = Seq(
		unmanagedSourceDirectories in Compile += new File((file(".") / sharedSrcDir / "src" / "main" / "scala").getCanonicalPath),
		unmanagedSourceDirectories in Test += new File((file(".") / sharedSrcDir / "src" / "test" / "scala").getCanonicalPath),
		unmanagedResourceDirectories in Compile += file(".") / sharedSrcDir / "src" / "main" / "resources",
		unmanagedResourceDirectories in Test += file(".") / sharedSrcDir / "src" / "test" / "resources"
	)

	// Use reflection to rename the 'start' command to 'play-start'
	Option(play.Play.playStartCommand.getClass.getDeclaredField("name")) map { field =>
		field.setAccessible(true)
		field.set(playStartCommand, "play-start")
	}

	// The new 'start' command optimises the JS before calling the Play 'start' renamed 'play-start'
	val preStartCommand = Command.args("start", "<port>") { (state: State, args: Seq[String]) =>
		Project.runTask(fullOptJS in (client, Compile), state)
		state.copy(remainingCommands = ("play-start " + args.mkString(" ")) +: state.remainingCommands)
	}
}
