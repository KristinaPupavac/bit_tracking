package controllers;

import com.avaje.ebean.Ebean;
import helpers.SessionHelper;
import models.ImagePath;
import models.PostOffice;
import models.User;
import models.UserType;
import org.apache.commons.io.FileUtils;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Result;
import views.html.*;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by mladen.teofilovic on 04/09/15.
 * This class controls user registration and login functions.
 */

public class UserController extends Controller {

    static Form<User> newUser = new Form<User>(User.class);
    private static final Form<User> userForm =
            Form.form(User.class);

    /**
     * This method is used for checking if user inserted data is valid.
     *
     * @return - positive message if true, else negative message
     */
    public Result check() {

        User u1 = SessionHelper.getCurrentUser(ctx());


        String password = newUser.bindFromRequest().field("password").value();
        String email = newUser.bindFromRequest().field("email").value();
        String newPassword = getEncriptedPasswordMD5(password);

        User u = User.findEmailAndPassword(email, newPassword);

        if (u != null) {
            session("email", email);
            return redirect(routes.Application.index());
        }
        return ok(login.render("Wrong email or password!"));

    }

    /**
     * This method get user input from registration form and if every input is valid
     * saves it to database.
     *
     * @return redirect user to subpage login if everything is ok, otherwise ?????
     */
    public Result registrationCheck() {
        User u1 = SessionHelper.getCurrentUser(ctx());
        Form<User> boundForm = userForm.bindFromRequest();
        String firstName = boundForm.bindFromRequest().field("firstName").value();
        String lastName = boundForm.bindFromRequest().field("lastName").value();
        String password = boundForm.bindFromRequest().field("password").value();
        String repassword = boundForm.bindFromRequest().field("repassword").value();
        String email = boundForm.bindFromRequest().field("email").value();
        User u = User.checkEmail(email);
        if (u == null) {
            u = new User(firstName, lastName, password, email);

            if (firstName.length() == 0 || lastName.length() == 0 || password.length() == 0 || repassword.length() == 0 || email.length() == 0) {
                flash("errorEmptyName", "Please fill all fields!");
            }

            if (u.checkName(firstName) && u.checkName(lastName)) {

                if (u.checkPassword(password) && password.equals(repassword)) {
                    String newPassword = getEncriptedPasswordMD5(password);
                    u = new User(firstName, lastName, newPassword, email);

                    Ebean.save(u);
                    flash("registered", "Welcome, " + u.firstName + "!");
                    return redirect(routes.Application.login());
                } else {
                    flash("errorPassword", "Couldn't accept password. Your password should contain at least 6 characters, one number, and one sign, or you entered different passwords");
                    return ok(register.render());
                }
            } else {
                flash("errorName", "Your name or last name should have only letters.");
                return (badRequest(register.render()));
                //return ok(register.render());
            }
        } else {
            flash("errorEmail", "E-mail address already exists!");
            return ok(register.render());
        }
    }

    /**
     * This method is used for password encryption.
     *
     * @param password - that would be inserted into database
     * @return - encrypted password
     */
    public static String getEncriptedPasswordMD5(String password) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(password.getBytes(), 0, password.length());
            String result = new BigInteger(1, md5.digest()).toString(16);
            md5.reset();
            return result;
        } catch (NoSuchAlgorithmException e) {
            // TODO add to logger
        }
        return "INVALID PASSWORD";
    }

    public Result editProfile(Long id) {
        User u1 = SessionHelper.getCurrentUser(ctx());
        User user = User.findById(id);

        ImagePath image = ImagePath.findImage.where().eq("profile_photo_id", user.id).findUnique();
       // System.out.println(image.image_url);

        if (user == null || u1.id != user.id) {
            return redirect(routes.Application.index());
        }
        return ok(editprofile.render(user, image));
    }

    /**
     * Method that updates user firstname, lastname and password
     *
     * @param id - user on that id
     * @return
     */
    public Result updateUser(Long id) {
        User u1 = SessionHelper.getCurrentUser(ctx());
        User user = User.findById(id);

        MultipartFormData body = request().body().asMultipartFormData();
        FilePart picture = body.getFile("picture");

        if(picture != null) {
            String fileName = picture.getFilename();
            File file = picture.getFile();
            String path = "\\public\\images\\" + fileName;
            try {
                FileUtils.moveFile(file, new File(path));

                ImagePath image = new ImagePath(path, u1);
                Ebean.save(image);
            } catch (IOException e) {
                flash("Could not move file.");
            }
        }

        Form<User> filledForm = newUser.fill(user);

        user.firstName = filledForm.bindFromRequest().field("firstName").value();
        user.lastName = filledForm.bindFromRequest().field("lastName").value();
        user.password = filledForm.bindFromRequest().field("password").value();

        String repassword = filledForm.bindFromRequest().field("repassword").value();

        if (User.checkName(user.firstName) && User.checkName(user.lastName)) {
            if (user.password.equals(repassword)) {
                user.password = getEncriptedPasswordMD5(user.password);
                Ebean.update(user);
                if(user.typeOfUser != UserType.ADMIN) {
                    return redirect(routes.Application.index());
                }
                return redirect(routes.Application.adminPanel());
            }
        }

        return redirect(routes.Application.login());
    }

    public Result deleteUser(Long id) {
        User user = User.findById(id);
        Ebean.delete(user);
        return redirect(routes.Application.adminTables());
    }

    public Result adminPreferences(Long id){
        User u1 = SessionHelper.getCurrentUser(ctx());
        User user = User.findById(id);
        if(user == null || u1.id != user.id){
            return redirect(routes.Application.adminPanel());
        }

        return ok(adminpreferences.render(user));
    }


    public Result userProfile(Long id) {
        User u1 = SessionHelper.getCurrentUser(ctx());
        User user = User.findById(id);

        if(user == null){
            return redirect(routes.Application.index());
        }

        if (u1.id != user.id && u1.typeOfUser != UserType.ADMIN) {
            return redirect(routes.Application.index());
        }

        return ok(userprofile.render(user));
    }

    public Result updateUserType(Long id) {
        User u1 = SessionHelper.getCurrentUser(ctx());
        User user = User.findById(id);

        Form<User> boundForm = userForm.bindFromRequest();
        String userType = boundForm.bindFromRequest().field("userType").value();
        if (userType.equals("Admin")) {
            user.typeOfUser = UserType.ADMIN;
        } else if (userType.equals("Office Worker")) {
            user.typeOfUser = UserType.OFFICE_WORKER;
        } else {
            user.typeOfUser = UserType.REGISTERED_USER;
        }
        Ebean.update(user);

        return TODO;

    }

    public Result addOfficeWorker(){
        User u1 = SessionHelper.getCurrentUser(ctx());
        Form<User> boundForm = userForm.bindFromRequest();
        String firstName = boundForm.bindFromRequest().field("firstName").value();
        String lastName = boundForm.bindFromRequest().field("lastName").value();
        String password = boundForm.bindFromRequest().field("password").value();
        String repassword = boundForm.bindFromRequest().field("repassword").value();
        String email = boundForm.bindFromRequest().field("email").value();
        String postOffice = boundForm.bindFromRequest().field("postOffice").value();

        PostOffice wantedPostOffice = PostOffice.findOffice.where().eq("name", postOffice).findUnique();
        System.out.println(postOffice + " " + wantedPostOffice.name);
        User u = User.checkEmail(email);
        if (u == null) {
            u = new User(firstName, lastName, password, email, wantedPostOffice);

            if (firstName.length() == 0 || lastName.length() == 0 || password.length() == 0 || repassword.length() == 0 || email.length() == 0) {
                flash("errorEmptyName", "Please fill all fields!");
            }

            if (u.checkName(firstName) && u.checkName(lastName)) {

                if (u.checkPassword(password) && password.equals(repassword)) {
                    String newPassword = getEncriptedPasswordMD5(password);
                    u = new User(firstName, lastName, newPassword, email, wantedPostOffice);

                    Ebean.save(u);
                    flash("registered", "Welcome, " + u.firstName + "!");
                    return redirect(routes.Application.officeWorkersList());
                } else {
                    flash("errorPassword", "Couldn't accept password. Your password should contain at least 6 characters, one number, and one sign, or you entered different passwords");
                    return ok(register.render());
                }
            } else {
                flash("errorName", "Your name or last name should have only letters.");
                return (badRequest(register.render()));
                //return ok(register.render());
            }
        } else {
            flash("errorEmail", "E-mail address already exists!");
            return ok(register.render());
        }

    }

}
