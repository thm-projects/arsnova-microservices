package de.thm.arsnova.shared.entities.export

import de.thm.arsnova.shared.entities.FreetextAnswer

case class FreetextAnswerExport(
  subject: String,
  text: String
)

object FreetextAnswerExport {
  def apply(f: FreetextAnswer): FreetextAnswerExport =
    FreetextAnswerExport(
      f.subject,
      f.text
    )
}
