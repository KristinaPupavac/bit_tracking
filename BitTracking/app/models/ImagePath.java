package models;


import play.data.validation.Constraints.*;
import play.db.ebean.Model;

import javax.persistence.*;

/**
 * Created by Mladen13 on 6.9.2015.
 */
@Entity
public class ImagePath extends Model {
    @Id
    public Long id;
    @Required
    public String image_url;
    @OneToOne
    public User profilePhoto;

    public ImagePath(){

    }

    public ImagePath(String image_url, User profilePhoto){
        this.image_url = image_url;
        this.profilePhoto = profilePhoto;
    }

    public static Model.Finder<Long, ImagePath> findImage = new Model.Finder<Long, ImagePath>(Long.class, ImagePath.class);
}
