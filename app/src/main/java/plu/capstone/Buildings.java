package plu.capstone;

/**
 * Created by playt on 4/14/2017.
 */

public class Buildings {
    public String Name;
    public float Latitude;
    public float Longitude;
    public String Description;

    public Buildings(){

    }

    public Buildings(String name, float latitude, float longitude, String description) {
        Name = name;
        Latitude = latitude;
        Longitude = longitude;
        Description = description;
    }
}
