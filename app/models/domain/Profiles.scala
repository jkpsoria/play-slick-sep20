package models.domain

import java.util.UUID

case class Profile(
    profileID: Long,
    userId: UUID,
    firstName: String,
    middleName: Option[String],
    lastName: String,
    birthdate: String,
    livingCountry: String,
    firstLanguage: String,
    secondLanguage: String
)