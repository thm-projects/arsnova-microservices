package de.thm.arsnova.testdata

import java.util.UUID

import de.thm.arsnova.shared.entities.User

class UserDataGenerator {
  val alpha = fabricator.Alphanumeric()
  val contact = fabricator.Contact()

  def gen(count: Int): Seq[User] = {
    val users = collection.mutable.Seq.empty[User]
    for (i <- 0 to count) {
      users :+ User(Some(UUID.randomUUID()), contact.fullName(true, true), alpha.botify("?????????"))
    }
    users
  }
}
