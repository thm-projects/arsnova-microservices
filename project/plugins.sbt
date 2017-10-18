resolvers += Resolver.bintrayIvyRepo("kamon-io", "sbt-plugins")
resolvers += "Fabricator" at "http://dl.bintray.com/biercoff/Fabricator"

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3")
addSbtPlugin("io.gatling" % "gatling-sbt" % "2.2.1")
addSbtPlugin("io.kamon" % "sbt-aspectj-runner" % "1.0.1")
