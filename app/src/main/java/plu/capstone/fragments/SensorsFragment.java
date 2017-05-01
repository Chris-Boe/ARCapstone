package plu.capstone.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.sql.Time;
import java.util.ArrayList;

import plu.capstone.Models.Buildings;
import plu.capstone.Models.PointOfInterest;
import plu.capstone.R;
import plu.capstone.activities.AR;
import plu.capstone.deprecated.DataBaseBuildings;

import static java.lang.System.currentTimeMillis;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SensorsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SensorsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SensorsFragment extends Fragment implements SensorEventListener, com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    // private static final String ARG_PARAM1 = "man";
    // private static final String ARG_PARAM2 = "cId";

    // TODO: Rename and change types of parameters
    // private CameraManager man;
    //private String cId;

    public static final String TAG = "OverlayView Log";
    String accelData = "Accelerometer Data";
    String compassData = "Compass Data";
    String gyroData = "Gyro Data";
    String bearing = "Bearing";
    String gps = "GPS:";
    String ori = "ORI:";
    private Location lastLocation = null;
    private Context superContext;
    private float[] lastAccelerometer;
    private float[] lastCompass;
    private CameraCharacteristics cc;
    private final static Location testLoc = new Location("manual");

    static {
        testLoc.setLatitude(47.1486260);
        testLoc.setLongitude(-122.4504670);
        testLoc.setAltitude(0);
    }

    private final static Location testLoc2 = new Location("manual");

    static {
        testLoc2.setLatitude(47.143121);
        testLoc2.setLongitude(-122.454317);
        testLoc2.setAltitude(0);
    }

    private float vFOV, hFOV;
    private float orientation[] = null;
    private float curBearing;
    private Time lastTime;
    private float[] smoothedAccel, smoothedCompass;
    private int TWENTYSEC = 1000 * 2;

    private String dName;
    private DataBaseBuildings dbb;
    private CameraManager man;
    private String cId;

    private OnFragmentInteractionListener mListener;
    private ArrayList<Buildings> buildingList;

    public SensorsFragment() {
        // Required empty public constructor
    }

    private static final String CC_KEY = "camchar_key";
    private CameraCharacteristics camchar;
    private DatabaseReference mDatabase;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private boolean isRequestingLocation = false;
    private float distance;
    private float lastRotation[];
    private float smoothRotation[];
    private boolean isRotationAvailable,isAccelAvailable,isCompassAvailable,isGyroAvailable,isGravityAvailable;
    private boolean accMag;
    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SensorsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SensorsFragment newInstance() {
        SensorsFragment fragment = new SensorsFragment();
        // Bundle args = new Bundle();
        // args.putString(ARG_PARAM2, cid);
        // args.putSerializable(ARG_PARAM2, c);
        // fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //  mParam1 = getArguments().getString(ARG_PARAM1);
            //  mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        man = ((AR) getActivity()).getCameraManager();
        cId = ((AR) getActivity()).getCameraId();
        Log.d("CAMINFO (v)", man + "/" + cId + "?");

        /**
         * GENERATE BUILDINGS
         * todo:SORT THIS BY LOCATION SOMEHOW
         */
        //get this moved later
        buildingList = new ArrayList<Buildings>();

        mDatabase = FirebaseDatabase.getInstance().getReference();
        Query testQuery = mDatabase.child("Pacific Lutheran University/Buildings");
        //probably use minheap instead
        testQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            //reads in data whenever changed (maybe find a more appropriate callback)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    buildingList.add(singleSnapshot.getValue(Buildings.class));
                    //Log.d("NAME",singleSnapshot.getValue(Buildings.class).Name);
                }
            }

            //error
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ERR", "onCancelled", databaseError.toException());
            }
        });

        Log.d("BLIST:", buildingList.toString());


        SensorManager sensors = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        Sensor accelSensor = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor compassSensor = sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor gyroSensor = sensors.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor rotationSensor = sensors.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        Sensor gravitySensor = sensors.getDefaultSensor(Sensor.TYPE_GRAVITY);

        isAccelAvailable = sensors.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_UI);
        isCompassAvailable = sensors.registerListener(this, compassSensor, SensorManager.SENSOR_DELAY_UI);
        isGyroAvailable = sensors.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_UI);
        isGravityAvailable = sensors.registerListener(this,gravitySensor,SensorManager.SENSOR_DELAY_UI);
        isRotationAvailable = sensors.registerListener(this,rotationSensor,SensorManager.SENSOR_DELAY_UI);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();


        //GET FOV
        cc = null;
        //Log.d("TEST", cId +" ?");
        try {
            cc = man.getCameraCharacteristics(cId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        //Log.d("cc return:", cc + " ?");

        // dbb = new DataBaseBuildings(getContext());
        // dName = "";

        return inflater.inflate(R.layout.fragment_sensors, container, false);
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void invalidate(String accelData,
                           String compassData,
                           String gyroData,
                           String bearing,
                           String gps,
                           String ori,
                           ArrayList<PointOfInterest> poiList) {
        if (mListener != null) {
            mListener.invalidate(accelData,
                    compassData,
                    gyroData,
                    bearing,
                    gps,
                    ori,
                    poiList);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        StringBuilder msg = new StringBuilder(event.sensor.getName()).append(" ");

        int smoothing = 15;
        Time time = new Time(currentTimeMillis());

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ROTATION_VECTOR:
               // Log.d("using","rotationvec");
/*
                lastRotation = new float[16];
                SensorManager.getRotationMatrixFromVector(lastRotation,event.values);

                //Log.d("just to make usre",lastRotation.toString()+"");
                accMag = false;*/
                break;

            case Sensor.TYPE_GRAVITY:
                lastAccelerometer = new float[event.values.length];
                lastAccelerometer = event.values;
                smoothedAccel = exponentialSmoothing(lastAccelerometer, smoothedAccel, (float) 0.1);
                accMag = true;
                break;
           case Sensor.TYPE_ACCELEROMETER:
               if(isGravityAvailable)
                   //gravity > accelerometer
                   break;
             /*  if(isRotationAvailable)
                   break;*/
               else {
                  // Log.d("using","accel");
                   lastAccelerometer = new float[event.values.length];
                   lastAccelerometer = event.values;
                   smoothedAccel = exponentialSmoothing(lastAccelerometer, smoothedAccel, (float) 0.1);
                   accMag = true;
               }
                /*
                float temp[] = null;
                smoothedAccel = lastAccelerometer;
                if (smoothedAccel == null)
                    for (int i = 0; i < event.values.length; i++) {
                        smoothedAccel = new float[event.values.length];
                        smoothedAccel[i] = 0;
                    }
                lastAccelerometer = event.values;
                float elapsed[] = new float[3];
                for (int i = 0; i < event.values.length; i++) {
                    smoothedAccel[i] += elapsed[i] * (lastAccelerometer[i] - smoothedAccel[i]) / smoothing;
                    lastTime = time;
                }
                for (float value : smoothedAccel) {
                    msg.append("[").append(value).append("]");
                }
                accelData = msg.toString();
                */
                break;
          /*  case Sensor.TYPE_GYROSCOPE:
                gyroData = msg.toString();
                break;*/
            case Sensor.TYPE_MAGNETIC_FIELD:
                /*if(isRotationAvailable)
                    break;
                else {*/
                   // Log.d("using:", "magfield");
                    lastCompass = new float[event.values.length];
                    lastCompass = event.values;
                    smoothedCompass = exponentialSmoothing(lastCompass, smoothedCompass, (float) .8);
                    accMag = true;
                //}
               /*
                smoothedCompass = lastCompass;
                if (smoothedCompass == null)
                    for (int i = 0; i < event.values.length; i++) {
                        smoothedCompass = new float[event.values.length];
                        smoothedCompass[i] = 0;
                    }
                lastCompass = event.values;
                float elapsedC[] = new float[3];
                for (int i = 0; i < event.values.length; i++) {
                    if (smoothedAccel == null)
                        smoothedAccel = new float[3];
                    smoothedCompass[i] += elapsedC[i] * (lastCompass[i] - smoothedCompass[i]) / smoothing;
                    lastTime = time;
                }
                for (float value : smoothedCompass) {
                    msg.append("[").append(value).append("]");
                }
                compassData = msg.toString();*/
               break;
        }


        //  invalidate(accelData, compassData, gyroData, bearing, gps, ori, orientation, curBearing);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        int SensorType = sensor.getType();
        switch(SensorType) {
            case Sensor.TYPE_ACCELEROMETER: Log.d("ACCELEROMETER CHANGED",accuracy+""); break;
            case Sensor.TYPE_MAGNETIC_FIELD: Log.d("MAGFIELD CHANGED",accuracy+""); break;
            case Sensor.TYPE_GRAVITY: Log.d("GRAVITY CHANGED",accuracy+""); break;
            case Sensor.TYPE_ROTATION_VECTOR: Log.d("ROTATION CHANGED",accuracy+"");break;
        }


    }

    @Override
    public void onLocationChanged(Location location) {
        //  Log.d("BLIST2", buildingList.toString());
        if (buildingList.size() > 0) {
            //Log.d("BUILDING:", buildingList.get(0).Name);

            ArrayList<PointOfInterest> poiList = new ArrayList<PointOfInterest>();
            if (isBetterLocation(location, lastLocation)) {
                lastLocation = location;
            }
            Log.d("prov", lastLocation.getProvider() + "");

            //TODO:this should be buildinglist.size()
            //buildinglist.size
          //  for (int i = 0; i < buildingList.size(); i++) {
            for(int i=0;i<4;i++){
                Location loc = new Location("manual");
                loc.setLatitude(buildingList.get(i).getLatitude());
                loc.setLongitude(buildingList.get(i).getLongitude());
                loc.setAltitude(0);

                String printLoc = "Lat: " + location.getLatitude() + " Long: " + location.getLongitude();
                //Toast toast = Toast.makeText(superContext.getApplicationContext(), printLoc, Toast.LENGTH_SHORT);
                // toast.show();
                gps = "GPS: " + printLoc;
                curBearing = lastLocation.bearingTo(loc);
                // Log.d("CURBEARING: ", curBearing + "");
                bearing = "bearing: " + curBearing;
                distance = lastLocation.distanceTo(loc);
                //  Log.d("CURBEARING:", curBearing + "?");

                //CHECK DISTANCE
                if(distance > 150) {
                    //using accel
                    if(accMag==true) {
                        float rotation[] = new float[9];
                        float identity[] = new float[9];

                        if (smoothedAccel == null)
                            Log.d(":c", "like really");


                        boolean gotRotation = SensorManager.getRotationMatrix(rotation, identity, smoothedAccel, smoothedCompass);


                        if (gotRotation) {
                            float cameraRotation[] = new float[9];
                            //remap so camera points straight down y axis
                            SensorManager.remapCoordinateSystem(rotation, SensorManager.AXIS_X, SensorManager.AXIS_Z, cameraRotation);
                            //orientation vec
                            orientation = new float[3];
                            SensorManager.getOrientation(cameraRotation, orientation);
                        /*if (gotRotation) {
                           cameraRotation = new float[9];
                            //remap so camera points positive
                           SensorManager.remapCoordinateSystem(rotation, SensorManager.AXIS_X, SensorManager.AXIS_Z, cameraRotation);

                            orientation = new float[3];
                            SensorManager.getOrientation(cameraRotation,orientation);
                        }*/
                        }
                    }
                    else {
                        //else using matrix
                        float[] smoothedRotation = new float[16];
                        orientation = new float[3];
                        SensorManager.remapCoordinateSystem(lastRotation,SensorManager.AXIS_X, SensorManager.AXIS_Z, smoothedRotation);
                        SensorManager.getOrientation(lastRotation, smoothedRotation);
                        Log.d("Really using matrix", "yup");

                        SensorManager.getOrientation(smoothedRotation,orientation);
                    }

                    double lon1 = lastLocation.getLongitude();
                    double lon2 = loc.getLongitude();
                    double lat1 = lastLocation.getLatitude();
                    double lat2 = lastLocation.getLongitude();



                    double phi1 = Math.toRadians(lat1);
                    double phi2 = Math.toRadians(lat2);
                    double lam1 = Math.toRadians(lon1);
                    double lam2 = Math.toRadians(lon2);

                    double mathBearing = Math.atan2(Math.sin(lam2-lam1)*Math.cos(phi2),
                            Math.cos(phi1)*Math.sin(phi2) - Math.sin(phi1)*Math.cos(phi2)*Math.cos(lam2-lam1)
                    ) * 180/Math.PI;


                    double latDelta = (lat2 - lat1);
                    double lonDelta = (lon2 - lon1);
                    double y = Math.sin(lonDelta)  * Math.cos(lat2);
                    double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2)* Math.cos(lonDelta);
                    double angle = Math.atan2(y, x); //not finished here yet
                   double headingDeg = orientation[0];
                    double angleDeg = angle * 180/Math.PI;
                    double heading = headingDeg*Math.PI/180;
                    angle = ((angleDeg + 360) % 360) * Math.PI/180; //normalize to 0 to 360 (instead of -180 to 180), then convert back to radians
                    angleDeg = angle * 180/Math.PI;




                    //Log.d("ORI:",ori+"");
                    ori = "ORI: " + orientation[0] + " " + orientation[1] + " " + orientation[2];


                        PointOfInterest poi = new PointOfInterest(orientation, curBearing, buildingList.get(i), distance);
                        poiList.add(poi);
                }

                invalidate(accelData, compassData, gyroData, bearing, gps, ori, poiList);

            }


           /* Location auc = new Location("manual");
            auc.setLatitude(buildingList.get(0).Latitude);
            auc.setLongitude(buildingList.get(0).Longitude);
            auc.setAltitude(0);


            //lastLocation = location;
            String printLoc = "Lat: " + location.getLatitude() + " Long: " + location.getLongitude();
            //Toast toast = Toast.makeText(superContext.getApplicationContext(), printLoc, Toast.LENGTH_SHORT);
            // toast.show();
            gps = "GPS: " + printLoc;

            curBearing = lastLocation.bearingTo(auc);
            Log.d("CURBEARING: ", curBearing + "");
            bearing = "bearing: " + curBearing;
            Log.d("CURBEARING:", curBearing + "?");


            float rotation[] = new float[9];
            float identity[] = new float[9];

            if (smoothedAccel == null)
                Log.d(":c", "like really");

            boolean gotRotation = SensorManager.getRotationMatrix(rotation, identity, smoothedAccel, smoothedCompass);

            if (gotRotation) {
                float cameraRotation[] = new float[9];
                //remap so camera points straight down y axis
                SensorManager.remapCoordinateSystem(rotation, SensorManager.AXIS_X, SensorManager.AXIS_Z, cameraRotation);
                //orientation vec
                orientation = new float[3];
                SensorManager.getOrientation(cameraRotation, orientation);
                if (gotRotation) {
                    cameraRotation = new float[9];
                    //remap so camera points positive
                    SensorManager.remapCoordinateSystem(rotation, SensorManager.AXIS_X, SensorManager.AXIS_Z, cameraRotation);

                    orientation = new float[3];
                    SensorManager.getOrientation(cameraRotation, orientation);
                }
            }
            //Log.d("ORI:",ori+"");
            ori = "ORI: " + orientation[0] + " " + orientation[1] + " " + orientation[2];

            if(distance < 100) {
                PointOfInterest poi = new PointOfInterest(orientation, curBearing, buildingList.get(0), distance);
                poiList.add(poi);
            }

            invalidate(accelData, compassData, gyroData, bearing, gps, ori, poiList);
            */
        }
    }

    //From Google Location Strategies
    //Smooth location results
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWENTYSEC;
        boolean isSignificantlyOlder = timeDelta < -TWENTYSEC;
        boolean isNewer = timeDelta > 0;

        // If it's been more than twenty seconds since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than twenty seconds older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
    /*
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }*/

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            requestPermissions(new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.INTERNET
            }, 10);
            return;
        }
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        isRequestingLocation = true;
        mLocationRequest = LocationRequest.create()
                .setInterval(500)
              .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            requestPermissions(new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.INTERNET
            }, 10);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    private float[] exponentialSmoothing( float[] input, float[] output, float alpha ) {
        if ( output == null )
            return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + alpha * (input[i] - output[i]);
        }
        return output;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void pauseLoc(){

        Log.d("pauseloc","location is pausing");
        stopLocationUpdates();
    }

    public void resumeLoc(){

        Log.d("resumeLoc", "locations are resuming");
        startLocationUpdates();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void invalidate(String accelData,
                    String compassData,
                    String gyroData,
                    String bearing,
                    String gps,
                    String ori,
                    ArrayList<PointOfInterest> poiList);
    }
    @Override
    public void onPause(){
        super.onPause();
        stopLocationUpdates();

    }
    @Override
    public void onResume(){
        super.onResume();
        if(mGoogleApiClient.isConnected() && !isRequestingLocation){
            startLocationUpdates();
        }
    }
    protected void stopLocationUpdates(){
        isRequestingLocation = false;
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

}
