package de.thm.arsnova.testdata

object Generator extends App {
  case class Config(speakerData: Int = 0, participantData: Int = 0)

  val parser = new scopt.OptionParser[Config]("testdata") {
    opt[Int]('s', "speaker").action( (x, c) =>
      c.copy(speakerData = x)
    ).text("Amount of speakers")
      .required()

    opt[Int]('p', "participant").action( (x, c) =>
      c.copy(participantData = x)
    ).text("Amount of participants")
      .required()

    help("help").text("prints this usage text")
  }

  parser.parse(args, Config()) match {
    case Some(config) => {
    }
    case None => {
      println("wrong arguments")
    }
  }
}