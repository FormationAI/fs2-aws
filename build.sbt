name := "fs2-aws"
organization in ThisBuild := "io.github.dmateusp"

scalaVersion := "2.12.8"

val desiredScalaVersions = settingKey[List[String]]("The List of Scala versions used for cross-building.")
desiredScalaVersions in ThisBuild := List("2.11.12", scalaVersion.value)

scalacOptions in ThisBuild ++= Seq(
  "-encoding",
  "UTF-8", // source files are in UTF-8
  "-deprecation", // warn about use of deprecated APIs
  "-unchecked", // warn about unchecked type parameters
  "-feature", // warn about misused language features
  "-language:higherKinds", // allow higher kinded types without `import scala.language.higherKinds`
  "-language:implicitConversions", // allow use of implicit conversions
  "-Xlint", // enable handy linter warnings
  "-Xfatal-warnings", // turn compiler warnings into errors
  "-Ypartial-unification", // allow the compiler to unify type constructors of different arities
  "-target:jvm-1.8"         // Needed for calling static methods on interfaces, which AWS SDK 2.x needs
)

lazy val root = (project in file("."))
  .aggregate(`fs2-aws`, `fs2-aws-testkit`)
  .settings(
    crossScalaVersions := Nil,
    skip in publish := true
  )

lazy val `fs2-aws`         = (project in file("fs2-aws"))
  .settings(
    crossScalaVersions := desiredScalaVersions.value,
    artifact in (Compile, packageBin) := {
      val previous: Artifact = (artifact in (Compile, packageBin)).value
      previous.withClassifier("form")
    } 
  )
lazy val `fs2-aws-testkit` = (project in file("fs2-aws-testkit")).dependsOn(`fs2-aws`)
  .settings(
    crossScalaVersions := desiredScalaVersions.value
  )

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9")

// publishing
publishTo in ThisBuild := {
  val prefix = if (isSnapshot.value) "snapshots" else "releases"
  Some(s3resolver.value(s"Formation ${prefix} S3 bucket", s3(if (isSnapshot.value) "mvn.takt.com/snapshots" else "mvn.takt.com/releases")))
}

licenses in ThisBuild := Seq(
  "MIT" -> url("https://github.com/dmateusp/fs2-aws/blob/master/LICENSE"))
developers in ThisBuild := List(
  Developer(id = "dmateusp",
            name = "Daniel Mateus Pires",
            email = "dmateusp@gmail.com",
            url = url("https://github.com/dmateusp"))
)
homepage in ThisBuild := Some(url("https://github.com/dmateusp/fs2-aws"))
scmInfo in ThisBuild := Some(
  ScmInfo(url("https://github.com/dmateusp/fs2-aws"),
          "scm:git:git@github.com:dmateusp/fs2-aws.git"))

// release
import ReleaseTransformations._

// signed releases
releasePublishArtifactsAction in ThisBuild := PgpKeys.publishSigned.value
credentials in ThisBuild += Credentials("Sonatype Nexus Repository Manager",
                                        "oss.sonatype.org",
                                        sys.env.getOrElse("SONATYPE_USERNAME", ""),
                                        sys.env.getOrElse("SONATYPE_PASSWORD", ""))

publishArtifact in ThisBuild in Test := true

updateOptions in ThisBuild := updateOptions.value.withGigahorse(false)

// release steps
releaseProcess := Seq[ReleaseStep](
  inquireVersions,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  pushChanges
)

releaseTagComment := s"Releasing ${(version in ThisBuild).value}"
releaseCommitMessage := s"[skip travis] Setting version to ${(version in ThisBuild).value}"
