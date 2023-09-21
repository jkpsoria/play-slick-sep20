package models.repo

import javax.inject._
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile
import scala.concurrent.{ExecutionContext, Future}
import models.repo.ProfileRepo
import models.domain.{Profile, User}
import java.util.UUID

trait UserRepository {
  def addUser(user: User): Future[Int]
  def updateUser(id: UUID, username: String, email: String): Future[Int]
  def getUsers: Future[Seq[User]]
  def deleteUser(id: UUID): Future[Int]
  def numberUsers: Future[Int]
}

@Singleton
final class UserRepo @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ex: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile]
    with UserRepository {

    import profile.api._
    protected class Users (tag: Tag) extends Table[User](tag, "USERS") {
        def id = column[UUID]("ID", O.PrimaryKey)
        def username = column[String]("USERNAME")
        def password = column[String]("PASSWORD")
        def email = column[String]("EMAIL")

        def * = (id, username, password, email).mapTo[User]
    }

    val users = TableQuery[Users]

    def createUserSchema = {
      db.run(users.schema.createIfNotExists)
      }

    override def addUser(user: User): Future[Int] = db.run(users += user)
    override def updateUser(id: UUID, username: String, email: String): Future[Int] = db.run(users.filter(_.id === id).map(x => (x.username, x.email)).update((username, email)))
    override def getUsers: Future[Seq[User]] = db.run(users.result)
    override def deleteUser(id: UUID): Future[Int] = db.run(users.filter(_.id === id).delete)
    override def numberUsers: Future[Int] = db.run(users.length.result)

    def numDifferentUsers: Future[Int] = db.run(users.map(_.id).distinct.length.result)

    //def firstUser: Future[Option[UUID]] =  db.run(users.map(_.id).min.result)

    //create the function to inner join both tables users and profiles
    
    def getUsernamesOnly = db.run(users.map(x => x.username).result)


    //groupby
    val msgPerUser: DBIO[Seq[(Long, Int)]] =
      messages.groupBy(_.senderId).
      map { case (senderId, msgs) => senderId -> msgs.length }.
    result
  

    //groupby with joins
    val msgsPerUser =
    messages.join(users).on(_.senderId === _.id).
    groupBy { case (msg, user)   => user.name }.
    map     { case (name, group) => name -> group.length }.
    result
}


