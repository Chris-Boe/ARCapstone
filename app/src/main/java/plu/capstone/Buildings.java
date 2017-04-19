package plu.capstone;

/**
 * Created by playt on 4/14/2017.
 */

public class Buildings {

    private String description;
    private float latitude;
    private float longitude;
    private String name;

    public Buildings(){

    }

    public Buildings(String description, float latitude, float longitude, String name) {
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }
}
