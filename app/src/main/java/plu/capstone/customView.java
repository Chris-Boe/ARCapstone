package plu.capstone;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.Query;

/**
 * Created by playt on 4/4/2017.
 */

public class customView extends View {

    private String accelData, compassData, gyroData, bearing, gps, ori;
    private float[] orientation;
    private float curBearing;
    private CameraManager manager;
    private String cameraId;
    private CameraCharacteristics fcc;
    private String info;


    public customView(Context context) {
        super(context);

        accelData = compassData = gyroData = bearing = gps = ori = "";
        orientation = new float[]{0.0f, 0.0f, 0.0f};
        curBearing = 0;
        manager = null;
        cameraId = "0";
    }

    public void setOptions(String aData, String cData, String gData, String b,
                           String g, String o, float[] or, float cb, CameraCharacteristics c){

        accelData = aData;
        compassData = cData;
        gyroData = gData;
        bearing = b;
        gps = g;
        ori = o;
        orientation = or;
        curBearing = cb;
        fcc =c;
        this.invalidate();
    }

    //TODO:make this json
    public void updateInfo(String q){
        info = q;
    }


    @Override

    public void onDraw(Canvas canvas){




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


            if(fcc!=null) {
                /*float hFOV = (float) (2 * Math.atan(
                        (fcc.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE).getWidth() /
                                (2 * fcc.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)[0])
                        )));
                float vFOV = (float) (2 * Math.atan(
                        (fcc.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE).getHeight() /
                                (2 * fcc.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)[0])
                        )));
*/

                canvas.rotate((float) (0.0f - Math.toDegrees(orientation[2])));
                //float dx = (float) ((canvas.getWidth() / hFOV) * (Math.toDegrees(orientation[0]) - curBearing));
                //float dy = (float) ((canvas.getHeight() / vFOV) * Math.toDegrees(orientation[1]));
                Log.d("ORI[0]/BEARING:", orientation[0] + "/" + curBearing);
                Log.d("o/d", (orientation[0] - curBearing) + "");
               // float testx = dx / -100;
                //Log.d("DX/TestX:", dx + "/" + testx);

                // wait to translate the dx so the horizon doesn't get pushed off
                // canvas.translate(0.0f, 0.0f - dy);

                // make our line big enough to draw regardless of rotation and translation
                canvas.drawLine(0f - canvas.getHeight(), canvas.getHeight() / 2, canvas.getWidth() + canvas.getHeight(), canvas.getHeight() / 2, targetPaint);


                // now translate the dx
                //canvas.translate(0.0f - dx, 0.0f);

                // draw our point -- we've rotated and translated this to the right spot already
                //Log.d("w/h: ", canvas.getWidth() + "/" + canvas.getHeight());

               // canvas.drawCircle(testx, canvas.getHeight() / 2, 100, targetPaint);

            }


        }
    }
}
