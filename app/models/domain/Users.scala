package models.domain

import java.util.UUID
import play.api.libs.json._


case class User(id: UUID, username: String, password: String, email: String)

object User {
    implicit val formatter: Format[User] = Json.format[User]
    val tupled = (apply: (UUID, String, String, String) => User).tupled
    def apply(username: String, password: String, email: String): User = apply(UUID.randomUUID(), username, password, email)
}