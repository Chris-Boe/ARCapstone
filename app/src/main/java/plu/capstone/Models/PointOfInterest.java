package plu.capstone.Models;

import plu.capstone.Models.Buildings;

/**
 * Created by playt on 4/14/2017.
 */

public class PointOfInterest {

    /*
    though typically bestpractice to use private fields for classes, in android development its strongly encouraged to access fields directly as its up to 3x faster
     */
    public float[] orientation;
    public float curBearing;
    public Buildings building;
    public float distance;
    public double azdeg;

    public PointOfInterest(){

    }

    public PointOfInterest(float[] orientation, float curBearing, Buildings building, float dis, double azdeg) {

        this.orientation = orientation;
        this.curBearing = curBearing;
        this.building = building;
        this.distance = dis;
        this.azdeg = azdeg;
    }

    /*
    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public float[] getOrientation() {
        return orientation;
    }

    public void setOrientation(float[] orientation) {
        this.orientation = orientation;
    }

    public float getCurBearing() {
        return curBearing;
    }

    public void setCurBearing(float curBearing) {
        this.curBearing = curBearing;
    }

    public Buildings getBuilding() {
        return building;
    }

    public void setBuilding(Buildings building) {
        this.building = building;
    }
*/
}
