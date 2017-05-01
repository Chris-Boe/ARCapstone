package plu.capstone.Models;

/**
 * Created by playt on 4/14/2017.
 */

public class Buildings {

    private String Description;
    private float Latitude;
    private float Longitude;
    private String Name;
    private String icon;

    public Buildings(){

    }

    public Buildings(String description, float latitude, float longitude, String name) {
        this.Description = description;
        this.Latitude = latitude;
        this.Longitude = longitude;
        this.Name = name;
    }

    public String getDescription() {
        return Description;
    }

    public float getLatitude() {
        return Latitude;
    }

    public float getLongitude() {
        return Longitude;
    }

    public String getName() {
        return Name;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public void setLatitude(float latitude) {
        Latitude = latitude;
    }

    public void setLongitude(float longitude) {
        Longitude = longitude;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
