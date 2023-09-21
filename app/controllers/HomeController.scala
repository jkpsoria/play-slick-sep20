package controllers

import javax.inject._
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}
import play.api.data._
import play.api.data.Forms._
import models.domain.User
import models.domain.Profile
import models.repo.UserRepo
import models.repo.ProfileRepo
import java.util.UUID

@Singleton
class HomeController @Inject()(
  val controllerComponents: ControllerComponents,
  implicit val ec: ExecutionContext,
  userRepo: UserRepo,
  profileRepo: ProfileRepo
) extends BaseController {

  val createUserForm: Form[User] = Form(
    mapping(
      "id" -> ignored(UUID.randomUUID()),
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "email" -> text
    )(User.apply)(User.unapply)
  )
  val createProfileForm: Form[Profile] = Form(
    mapping(
      "profileID" -> ignored(0L),
      "userid" -> ignored(UUID.randomUUID()),
      "firstname" -> nonEmptyText,
      "middlename" -> optional(text),
      "lastname" -> nonEmptyText,
      "birthdate" -> nonEmptyText,
      "livingcountry" -> nonEmptyText,
      "firstlanguage" -> nonEmptyText,
      "secondlanguage" -> nonEmptyText
    )(Profile.apply)(Profile.unapply)
  )

  def index() = Action.async { implicit request =>
    
    userRepo.createUserSchema.flatMap{ _ =>
      profileRepo.createProfileSchema
    }.map(_ => Ok)
  }
  def getUsers() = Action.async{ implicit request =>
    userRepo.getUsers.map(users => Ok(users.mkString("\n")))  
  }
  def getNumberUsers() = Action.async{ implicit request =>
    userRepo.numberUsers.map(users => Ok(users.toString))  
  }
  def getProfiles() = Action.async{ implicit request =>
    profileRepo.getProfiles.map(profiles => Ok(profiles.mkString("\n")))  
  }
  def createUser() = Action.async { implicit request =>
    createUserForm.bindFromRequest().fold(
      formsWithErrors => Future.successful(BadRequest),
      user => userRepo.addUser(user.copy(id = UUID.randomUUID())).map(_ => Ok)
    )
  }
  def createProfile(id: UUID) = Action.async { implicit request =>
    createProfileForm.bindFromRequest().fold(
      formsWithErrors => Future.successful(BadRequest),
      profile =>
        profileRepo.addProfile(profile.copy(userId = id)).map(_ => Ok)
    )
  }
  def updateUser(id: UUID): Action[AnyContent] = Action.async { implicit request =>
    createUserForm.bindFromRequest().fold(
      formsWithErrors => Future.successful(BadRequest),
      user =>
        userRepo.updateUser(id, user.username, user.email).map(_ => Ok)
    )
  }
  def updateProfile(id: UUID): Action[AnyContent] = Action.async { implicit request =>
    createProfileForm.bindFromRequest().fold(
      formsWithErrors => Future.successful(BadRequest),
      profile =>
        profileRepo.updateProfile(id, profile.livingCountry, profile.firstLanguage, profile.secondLanguage).map(_ => Ok)
    )
  }
  def deleteUser(id: UUID) = Action.async { implicit request =>
    userRepo.deleteUser(id).map(_ => Ok)
  }



  

  def getNumRowsProfile() = Action.async { implicit request => 
    profileRepo.getNumRowsProfile.map(profile => Ok(profile.toString))  
  }

  def getNumDiffUsers() = Action.async { implicit request =>
    userRepo.numDifferentUsers.map(users => Ok(users.toString))
  }

  // def getFirstUser(id: UUID) = Action.async { implicit request =>
  //   userRepo.firstUser.map(user => Ok)
  // }

  def getProfilesPerUser() = Action.async { implicit request =>
    profileRepo.profilesPerUser.map(profiles => Ok(profiles.mkString("\n")))

  }

  def getUsersAndProfiles() = Action.async { implicit request =>
    profileRepo.usersAndProfiles.map(usersAndProfiles => Ok(usersAndProfiles.mkString("\n")))
  }


  def getUsernamesOnly() = Action.async { implicit request =>
    userRepo.getUsernamesOnly.map(usernames => Ok(usernames.mkString("\n")))
  }


  def leftJoinUsersAndProfiles() = Action.async { implicit request => 
    profileRepo.leftJoinUsersAndProfiles.map(usersAndProfiles => Ok(usersAndProfiles.mkString("\n")))
  }
}