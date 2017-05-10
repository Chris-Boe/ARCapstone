package plu.capstone.Models;

import plu.capstone.Models.Buildings;

/**
 * Created by Micaela Pierce and Chris boe
 * a bundle to pass from Sensors to AR to BuildingOverlay
 * fields are public following mobile best practices, be careful
 * */

public class PointOfInterest {

    /*    though typically best practice to use private fields for classes, in android development its strongly encouraged to access fields directly as its up to 3x faster
     */
    public float[] orientation;     //array holding azimuth, pitch, roll
    public float curBearing;        //bearing of the poi
    public Buildings building;      //building of the poi
    public float distance;          //distance from user to poi (not needed)
    public double azdeg;            //azimuth in degrees

    public float dx;

    public PointOfInterest(){

    }

    public PointOfInterest(float[] orientation, float curBearing, Buildings building, float dis, double azdeg, float x) {

        this.orientation = orientation;
        this.curBearing = curBearing;
        this.building = building;
        this.distance = dis;
        this.azdeg = azdeg;
        this.dx = x;
    }
}
