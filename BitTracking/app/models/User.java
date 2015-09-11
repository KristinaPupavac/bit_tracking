package models;


import play.data.validation.Constraints;
import play.db.ebean.Model;

import javax.persistence.*;
import java.util.List;

@Entity
public class User extends Model {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long id;
    @Column(length = 50)
    public String firstName;
    @Column(length = 50)
    public String lastName;
    @Column(length = 50)
    @Constraints.MinLength(6)
    @Constraints.MaxLength(28)
    public String password;
    @Column(length = 50)
    public String email;
    @Column
    @Enumerated(EnumType.STRING)
    public UserType typeOfUser;
    @ManyToOne(cascade = CascadeType.ALL)
    public PostOffice postOffice;

    @OneToOne(mappedBy="profilePhoto", cascade=CascadeType.ALL)
    public ImagePath imagePath;


    public User() {
    }

    /**
     * Constructor for registered user
     * @param firstName - represents his first name
     * @param lastName - last name
     * @param password - password
     * @param email - email address
     */
    public User(String firstName, String lastName, String password, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.email = email;
        this.typeOfUser=UserType.REGISTERED_USER;
    }

    /**
     * Constructor that creates office worker
     * @param firstName
     * @param lastName
     * @param password
     * @param email
     * @param postOffice - office where he works
     */
    public User(String firstName, String lastName, String password, String email, PostOffice postOffice) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.email = email;
        this.typeOfUser=UserType.OFFICE_WORKER;
        this.postOffice=postOffice;

    }


    public static Finder<String, User> find = new Finder<String, User>(String.class, User.class);

    /**
     * Method that checks if user exists in database
     *
     * @param email    - email address from inserted user
     * @param password - inserted user password
     * @return - user if it finds him, otherwise null
     */
    public static User findEmailAndPassword(String email, String password) {

        List<User> list = find.where().eq("email", email).eq("password", password).findList();
        if (list.size() == 0) {
            return null;
        }
        return (User) (list.get(0));
    }


    /**
     * This method checks if the entered email exists in the database
     *
     * @param email email from user input
     * @return null if email doesnt exist in database, otherwise 1
     */
    public static User checkEmail(String email) {
        List<User> listEmail = find.where().eq("email", email).findList();
        if (listEmail.size() == 0) {
            return null;
        }
        return (User) (listEmail.get(0));
    }

    public static boolean checkName(String name) {


    for (int i = 0; i < name.length(); i++) {
        if ((name.charAt(i) > 64 && name.charAt(i) < 91) || (name.charAt(i) > 96 && name.charAt(i) < 124)){
            return true;
        } else if (name.charAt(i)==262 || name.charAt(i)==263 || name.charAt(i)==268 || name.charAt(i)==269 || name.charAt(i)==272 || name.charAt(i)==273 || name.charAt(i)==352 || name.charAt(i)==353 || name.charAt(i)==381 || name.charAt(i)==382) {
            return true;
        }
    }
    return false;
}

    public static boolean checkPassword(String password) {
        int letters = 0;
        int numbers = 0;
        if (password.length() < 6) {
            return false;
        }
        for (int i = 0; i < password.length(); i++) {
            if (password.charAt(i) > 47 && password.charAt(i) < 58) {
                numbers++;
            } else if ((password.charAt(i) > 64 && password.charAt(i) < 91) || (password.charAt(i) > 96 && password.charAt(i) < 123)) {
                letters++;
            }
        }
        if (letters == 0 || numbers == 0) {
            return false;
        }
        return true;
    }

    public static User findById (Long longId) {
        String id = longId.toString();
        User user = find.byId(id);
        if (user != null) {
            return user;
        }
        return null;
    }

    public static List<User> findOfficeWorkers(){
         List<User> officeworkers = find.where().eq("type_of_user", UserType.OFFICE_WORKER).findList();

        if (officeworkers.size() == 0) {
            return null;
        }

        return officeworkers;
    }


    @Override
    public String toString() {
        return "id=" + id +
                ", firstName=" + firstName +
                ", lastName=" + lastName +
                ", password=" + password +
                ", email=" + email;
    }
}