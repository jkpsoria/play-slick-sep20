package models.repo

import javax.inject._
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent.{ExecutionContext, Future}
import models.domain._
import java.util.UUID

trait ProfileRepository {
  def addProfile(profile: Profile): Future[Int]
  def updateProfile(id: UUID, livingCountry: String, firstLanguage: String, secondLanguage: String): Future[Int]
  def getProfiles: Future[Seq[Profile]]
}

@Singleton
final class ProfileRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider, val userRepo: UserRepo)(implicit ex: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile]
    with ProfileRepository {
    import profile.api._
    protected class Profiles (tag: Tag) extends Table[Profile](tag, "PROFILES") {
        def profileID = column[Long]("PROFILE_ID", O.PrimaryKey, O.AutoInc)
        def userId = column[UUID]("ID")//O.PrimaryKey
        def firstName = column[String]("FIRST_NAME")
        def middleName = column[Option[String]]("MIDDLE_NAME")
        def lastName = column[String]("LAST_NAME")
        def birthdate = column[String]("BIRTH_DATE")
        def livingCountry = column[String]("LIVING_COUNTRY")
        def firstLanguage = column[String]("FIRST_LANGUAGE")
        def secondLanguage = column[String]("SECOND_LANGUAGE")
        
        
        def * = (profileID, userId, firstName, middleName, lastName, birthdate, livingCountry, firstLanguage, secondLanguage).mapTo[Profile]
        def userId_fk = foreignKey("user_id_fk", userId, userRepo.users)(_.id, onDelete = ForeignKeyAction.Cascade, onUpdate = ForeignKeyAction.Cascade)
    }

    val profiles = TableQuery[Profiles]

    def createProfileSchema = {
      // println(profiles.schema.createStatements.mkString)
      // println(profiles.schema.createStatements.mkString)
      db.run(profiles.schema.create)
    }

    override def addProfile(profile: Profile): Future[Int] = db.run(profiles += profile)
    override def updateProfile(id: UUID, livingCountry: String, firstLanguage: String, secondLanguage: String): Future[Int] = db.run(profiles.filter(_.userId === id).map(x => (x.livingCountry, x.firstLanguage, x.secondLanguage)).update((livingCountry, firstLanguage, secondLanguage)))
    override def getProfiles: Future[Seq[Profile]] = db.run(profiles.result)

    def getNumRowsProfile:Future[Int] = db.run(profiles.length.result)

    def profilesPerUser = db.run(profiles.groupBy(_.userId).map{
      case (senderId, user) => senderId -> user.length }.result)

    
    def usersAndProfiles: Future[Seq[(User, Profile)]] = db.run(userRepo.users.join(profiles).on(_.id === _.userId).result)
    def leftJoinUsersAndProfiles = db.run(userRepo.users.join(profiles).on(_.id === _.userId).result)
}
