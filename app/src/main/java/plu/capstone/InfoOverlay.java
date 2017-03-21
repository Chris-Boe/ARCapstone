package plu.capstone;

import java.lang.Math.*;
import java.sql.Time;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import static android.R.attr.value;
import static java.lang.System.currentTimeMillis;

/**
 * Created by playt on 3/15/2017.
 */

public class InfoOverlay extends View implements SensorEventListener, LocationListener {
    public static final String DEBUG_TAG = "OverlayView Log";
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



    public InfoOverlay(Context context, CameraDevice cam, CameraManager man, String cId) {
        super(context);
        superContext = context;

        SensorManager sensors = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor accelSensor = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor compassSensor = sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor gyroSensor = sensors.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        boolean isAccelAvailable = sensors.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL);
        boolean isCompassAvailable = sensors.registerListener(this, compassSensor, SensorManager.SENSOR_DELAY_NORMAL);
        boolean isGyroAvailable = sensors.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_NORMAL);

        //location
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);

        String best = locationManager.getBestProvider(criteria, true);

        Log.v(DEBUG_TAG, "best provider: " + best);
        /*
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(best, 2000, 0, this);
*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(superContext, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(superContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) !=
                            PackageManager.PERMISSION_GRANTED) {
             /*   requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.INTERNET
                }, 10);*/
                return;
            }
        }
        locationManager.requestLocationUpdates(best, 500, 0, this);

        //GET FOV
        cc = null;
        //Log.d("TEST", cId +" ?");
        try {
            cc = man.getCameraCharacteristics(cId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        //Log.d("cc return:", cc + " ?");

    }




    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint contentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Paint targetPaint = new Paint(Color.CYAN);
        contentPaint.setTextAlign(Paint.Align.CENTER);
        contentPaint.setTextSize(20);
        contentPaint.setColor(Color.RED);
        canvas.drawText(accelData, canvas.getWidth()/2, canvas.getHeight()/8, contentPaint);
        canvas.drawText(compassData, canvas.getWidth()/2, canvas.getHeight()*2/8, contentPaint);
        canvas.drawText(gyroData, canvas.getWidth()/2, (canvas.getHeight())*3/8, contentPaint);
        canvas.drawText(bearing,canvas.getWidth()/2, (canvas.getHeight())*4/8, contentPaint);
        canvas.drawText(gps,canvas.getWidth()/2, (canvas.getHeight())*5/8, contentPaint);
        canvas.drawText(ori,canvas.getWidth()/2, (canvas.getHeight())*6/8, contentPaint);

        //Log.d("CC VAL", cc + " ?");

        float lVFOV, lHFOV;

        if(orientation!=null) {



            hFOV = (float)(2 * Math.atan(
                (cc.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE).getWidth() /
                        (2 * cc.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)[0])
                )));
            vFOV = (float)(2 * Math.atan(
                (cc.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE).getHeight() /
                        (2 * cc.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)[0])
                )));




            canvas.rotate((float) (0.0f - Math.toDegrees(orientation[2])));
            float dx = (float) ((canvas.getWidth() / hFOV) * (Math.toDegrees(orientation[0]) - curBearing));
            float dy = (float) ((canvas.getHeight() / vFOV) * Math.toDegrees(orientation[1]));



            // wait to translate the dx so the horizon doesn't get pushed off
            canvas.translate(0.0f, 0.0f - dy);

            // make our line big enough to draw regardless of rotation and translation
            canvas.drawLine(0f - canvas.getHeight(), canvas.getHeight() / 2, canvas.getWidth() + canvas.getHeight(), canvas.getHeight() / 2, targetPaint);


            // now translate the dx
           // canvas.translate(0.0f - dx, 0.0f);

            // draw our point -- we've rotated and translated this to the right spot already
           // Log.d("w/h: ", canvas.getWidth() / 2 + "/" + canvas.getHeight() / 2);
            canvas.drawCircle(canvas.getWidth() / 2, canvas.getHeight()*6/8, 100, targetPaint);
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event){
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


        this.invalidate();

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
        Log.d("bearing: ",curBearing+"");
        bearing = "bearing: " + curBearing;
        Log.d("loc:",printLoc);


        float rotation[] = new float[9];
        float identity[] = new float[9];

        boolean gotRotation = SensorManager.getRotationMatrix(rotation,identity,smoothedAccel,smoothedCompass);

        float cameraRotation[];
        if(gotRotation){
            cameraRotation = new float[9];
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
        Log.d("ORI:",ori+"");
        ori = "ORI: " + orientation[0] + " " + orientation[1] + " " + orientation[2];
        this.invalidate();
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
}
