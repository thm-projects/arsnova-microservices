name := "arsnova-microservices"
organization in ThisBuild := "de.thm.arsnova"
version in ThisBuild := "0.1"
scalaVersion in ThisBuild := "2.11.8"

val akkaVersion = "2.5.3"
val akkaHTTPVersion = "10.0.5"
val scalaTestVersion = "3.0.0"
val scalaMockVersion = "3.2.2"
val slickVersion = "3.2.0"
val gatlingVersion = "2.2.4"

val akkaDependencies = Seq(
  "com.typesafe.akka"     %% "akka-actor"                           % akkaVersion,
  "com.typesafe.akka"     %% "akka-remote"                          % akkaVersion,
  "com.typesafe.akka"     %% "akka-cluster"                         % akkaVersion,
  "com.typesafe.akka"     %% "akka-cluster-tools"                   % akkaVersion,
  "com.typesafe.akka"     %% "akka-stream"                          % akkaVersion,
  "com.typesafe.akka"     %% "akka-testkit"                         % akkaVersion % "test",
  "com.typesafe.akka"     %% "akka-http"                            % akkaHTTPVersion,
  "com.typesafe.akka"     %% "akka-http-testkit"                    % akkaHTTPVersion,
  "com.typesafe.akka"     %% "akka-http-spray-json"                 % akkaHTTPVersion,
  "ch.qos.logback"        %  "logback-classic"                      % "1.2.3"
)

val slickDependencies = Seq(
  "com.typesafe.slick"    %% "slick"                                % slickVersion,
  "com.typesafe.slick"    %% "slick-hikaricp"                       % slickVersion,
  "org.postgresql"        %  "postgresql"                           % "9.4-1206-jdbc42",
//"mysql"                 %  "mysql-connector-java"                 % "6.0.3",
  "org.flywaydb"          %  "flyway-core"                          % "3.2.1"
)

val testDependencies = Seq(
  "org.scalatest"         %% "scalatest"                            % scalaTestVersion
)

val gatlingDeps = Seq(
  "io.gatling.highcharts" % "gatling-charts-highcharts"             % gatlingVersion % "test,it",
  "io.gatling"            % "gatling-test-framework"                % gatlingVersion % "test,it"
)

val kamonDeps = Seq(
  "io.kamon" %% "kamon-core" % "0.6.0",
  "io.kamon" %% "kamon-statsd" % "0.6.0",
  "io.kamon" %% "kamon-datadog" % "0.6.0"
)

// skip Tests in assembly job
// test in assembly := {}

lazy val root = (project in file("."))
  .aggregate(
    gateway,
    managementservice,
    commandservice,
    sessionservice,
    questionservice,
    commentservice
  )

lazy val shared = (project in file("shared"))
  .settings(
    libraryDependencies ++= akkaDependencies
  )

lazy val gateway = (project in file("gateway"))
  .settings(
    libraryDependencies ++= akkaDependencies ++ kamonDeps
  )
  .dependsOn(shared, authservice, sessionservice)

lazy val managementservice = (project in file("managementservice"))
  .settings(
    libraryDependencies ++= akkaDependencies
  )
  .dependsOn(shared)

lazy val commandservice = (project in file("commandservice"))
  .settings(
    libraryDependencies ++= akkaDependencies
  )
  .dependsOn(shared)

lazy val authservice = (project in file("authservice"))
  .settings(
    libraryDependencies ++= akkaDependencies ++ slickDependencies
  )
  .dependsOn(shared)

lazy val sessionservice = (project in file("sessionservice"))
  .settings(
    libraryDependencies ++= akkaDependencies ++ slickDependencies ++ kamonDeps
  )
  .dependsOn(shared, authservice)

lazy val questionservice = (project in file("questionservice"))
  .settings(
    libraryDependencies ++= akkaDependencies ++ slickDependencies
  )
  .dependsOn(shared)

lazy val commentservice = (project in file("commentservice"))
  .settings(
    libraryDependencies ++= akkaDependencies ++ slickDependencies
  )
  .dependsOn(shared)

lazy val stresstest = (project in file("stresstest"))
  .settings(
    libraryDependencies ++= gatlingDeps
  )
  .dependsOn(shared)


aspectjSettings

// Here we are effectively adding the `-javaagent` JVM startup
// option with the location of the AspectJ Weaver provided by
// the sbt-aspectj plugin.
javaOptions in run <++= AspectjKeys.weaverOptions in Aspectj

// We need to ensure that the JVM is forked for the
// AspectJ Weaver to kick in properly and do it's magic.
fork in run := true

// skip Tests in assembly job
test in assembly := {}