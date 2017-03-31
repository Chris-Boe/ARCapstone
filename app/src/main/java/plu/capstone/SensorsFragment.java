package plu.capstone;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.sql.Time;

import static java.lang.System.currentTimeMillis;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SensorsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SensorsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SensorsFragment extends Fragment implements SensorEventListener, LocationListener {
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
    private float vFOV, hFOV;
    private float orientation[]=null;
    private float curBearing;
    private Time lastTime;
    private float[] smoothedAccel, smoothedCompass;

    private String dName;
    private DataBaseBuildings dbb;
    private CameraManager man;
    private String cId;

    private OnFragmentInteractionListener mListener;

    public SensorsFragment() {
        // Required empty public constructor
    }

    private static final String CC_KEY = "camchar_key";
    private CameraCharacteristics camchar;

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
        man = ((AR)getActivity()).getCameraManager();
        cId = ((AR)getActivity()).getCameraId();
        Log.d("CAMINFO (v)", man + "/" + cId + "?");

        SensorManager sensors = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        Sensor accelSensor = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor compassSensor = sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor gyroSensor = sensors.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        boolean isAccelAvailable = sensors.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
        boolean isCompassAvailable = sensors.registerListener(this, compassSensor, SensorManager.SENSOR_DELAY_NORMAL);
        boolean isGyroAvailable = sensors.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);

        //location
        LocationManager locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

        String best = locationManager.getBestProvider(criteria, true);

        Log.v(TAG, "best provider: " + best);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.INTERNET
                }, 10);
                //request permission
            } else {
                locationManager.requestLocationUpdates(best, 500, 0, this);
            }
        }

        //GET FOV
        cc = null;
        //Log.d("TEST", cId +" ?");
        try {
            cc = man.getCameraCharacteristics(cId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

       //Log.d("cc return:", cc + " ?");

        //dbb = new DataBaseBuildings(getContext());
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
                       float orientation[], float curBearing) {
        if (mListener != null) {
            mListener.invalidate(accelData,
                    compassData,
                    gyroData,
                    bearing,
                    gps,
                    ori,
                    orientation, curBearing);
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

        switch(event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                float temp[] = null;
                smoothedAccel = lastAccelerometer;
                if(smoothedAccel==null)
                    for(int i=0;i<event.values.length;i++) {
                        smoothedAccel = new float[event.values.length];
                        smoothedAccel[i] = 0;
                    }
                lastAccelerometer = event.values;
                float elapsed[] = new float[3];
                for(int i=0;i<event.values.length;i++){
                    smoothedAccel[i] += elapsed[i] * (lastAccelerometer[i] - smoothedAccel[i])/smoothing;         lastTime = time;
                }
                for(float value: smoothedAccel) {
                    msg.append("[").append(value).append("]");
                }
                accelData = msg.toString();
                break;
          /*  case Sensor.TYPE_GYROSCOPE:
                gyroData = msg.toString();
                break;*/
            case Sensor.TYPE_MAGNETIC_FIELD:

                smoothedCompass = lastCompass;
                if(smoothedCompass==null)
                    for(int i=0;i<event.values.length;i++) {
                        smoothedCompass = new float[event.values.length];
                        smoothedCompass[i] = 0;
                    }
                lastCompass = event.values;
                float elapsedC[] = new float[3];
                for(int i=0;i<event.values.length;i++){
                    smoothedAccel[i] += elapsedC[i] * (lastCompass[i] - smoothedCompass[i])/smoothing;         lastTime = time;
                }
                for(float value: smoothedCompass) {
                    msg.append("[").append(value).append("]");
                }
                compassData = msg.toString();
                break;
        }


        invalidate(accelData, compassData, gyroData, bearing, gps, ori, orientation, curBearing);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;
        String printLoc = "Lat: " + location.getLatitude() + " Long: " + location.getLongitude();
        //Toast toast = Toast.makeText(superContext.getApplicationContext(), printLoc, Toast.LENGTH_SHORT);
        // toast.show();
        gps = "GPS: " + printLoc;

        curBearing = lastLocation.bearingTo(testLoc);
        Log.d("CURBEARING: ",curBearing+"");
        bearing = "bearing: " + curBearing;
        Log.d("CURBEARING:",curBearing+"?");


        float rotation[] = new float[9];
        float identity[] = new float[9];

        if(smoothedAccel==null)
            Log.d(":c","like really");

        boolean gotRotation = SensorManager.getRotationMatrix(rotation,identity,smoothedAccel,smoothedCompass);

        if(gotRotation){
            float cameraRotation[] = new float[9];
            //remap so camera points straight down y axis
            SensorManager.remapCoordinateSystem(rotation,SensorManager.AXIS_X,SensorManager.AXIS_Z,cameraRotation);
            //orientation vec
            orientation = new float[3];
            SensorManager.getOrientation(cameraRotation,orientation);
            if (gotRotation) {
                cameraRotation = new float[9];
                //remap so camera points positive
                SensorManager.remapCoordinateSystem(rotation,SensorManager.AXIS_X,SensorManager.AXIS_Z,cameraRotation);

                orientation = new float[3];
                SensorManager.getOrientation(cameraRotation,orientation);
            }
        }
        //Log.d("ORI:",ori+"");
        ori = "ORI: " + orientation[0] + " " + orientation[1] + " " + orientation[2];
        invalidate(accelData, compassData, gyroData, bearing, gps, ori, orientation, curBearing);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

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
                    float orientation[], float curBearing);
    }
}
