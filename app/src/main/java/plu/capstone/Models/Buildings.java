package plu.capstone.Models;

/**
 * Created by Micaela and Chris on 4/14/2017.
 * A building model for pulling json info from the database
 * fields are public following mobile best practices, be careful
 */

public class Buildings {

    public String Description;  //information about building
    public float Latitude;      //lat coordinates
    public float Longitude;     //long coordinates
    public String Name;         //name
    public String icon;         //icon for ar

    public Buildings(){

    }

    public Buildings(String description, float latitude, float longitude, String name) {
        this.Description = description;
        this.Latitude = latitude;
        this.Longitude = longitude;
        this.Name = name;
    }

}
