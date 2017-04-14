package plu.capstone;

/**
 * Created by playt on 4/14/2017.
 */

public class PointOfInterest {
    private float[] orientation;
    private float curBearing;
    private Buildings building;

    public PointOfInterest(){

    }

    public PointOfInterest(float[] orientation, float curBearing, Buildings building) {

        this.orientation = orientation;
        this.curBearing = curBearing;
        this.building = building;
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

}
