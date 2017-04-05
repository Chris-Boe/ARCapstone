package plu.capstone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BuildingOverlay.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BuildingOverlay#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BuildingOverlay extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Query bQuery;
    private customView tempView;
    private RelativeLayout arViewPane;

    private  String accelData, aData, compassData, cData, gyroData, gData, bearing, b, gps, g, ori, o;
    private float[] orientation;
    private float curBearing;
    private DatabaseReference mDatabase;
    private FrameLayout buildingInfo;

    private Button buildingButton;
    private View buttonView;

    private OnFragmentInteractionListener mListener;


    public BuildingOverlay() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment BuildingOverlay.
     */
    // TODO: Rename and change types and number of parameters
    public static BuildingOverlay newInstance(String param1, String param2) {
        BuildingOverlay fragment = new BuildingOverlay();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        //instantiate database
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //TODO: query database for possible locations
        //queries each building (should probably test for more buildings)



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View in = inflater.inflate(R.layout.fragment_building_overlay, container, false);
        arViewPane = (RelativeLayout)in.findViewById(R.id.arviewpane);
        Canvas temp = new Canvas();
        tempView = new customView(getContext());
        arViewPane.addView(tempView);
        onReady();
        return in;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onReady() {
        if (mListener != null) {
            mListener.onReady();
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


    public void update(String aData, String cData, String gData, String b,
                       String g, String o, float[] or, float cb, CameraManager manager, String cameraId){

        CameraCharacteristics cc = null;

        try {
            cc = manager.getCameraCharacteristics(cameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        CameraCharacteristics fcc = cc;
        tempView.setOptions(aData, cData, gData, b, g, o, or, cb, cc);

        //TODO:calculate query
        Query testQuery = mDatabase.child("Pacific Lutheran University/Buildings");

        float hFOV = (float) (2 * Math.atan(
                (fcc.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE).getWidth() /
                        (2 * fcc.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)[0])
                )));
        float vFOV = (float) (2 * Math.atan(
                (fcc.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE).getHeight() /
                        (2 * fcc.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)[0])
                )));



        if(or!=null) {
            final float dx = (float) ((getView().getWidth() / hFOV) * (Math.toDegrees(or[0]) - cb));

            final float testx = dx / -100;
            Log.d("TESTX",dx+"?");


            //listener for onDataChange
            testQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                //reads in data whenever changed (maybe find a more appropriate callback)
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    //send info to various views


                    if(buildingButton!=null)
                        arViewPane.removeView(buildingButton);

                    buildingButton = new Button(getContext());
                    buildingButton.setTag("Building");
                    buildingButton.setText("TEMPORARY");
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    params.leftMargin = getView().getWidth()/2;
                    params.topMargin = getView().getHeight()/2;


                    arViewPane.addView(buildingButton,params);

                    // String bList = "";

                   /* for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                        bList += singleSnapshot.getValue(DataBaseBuildings.Buildings.class).Name + " ";
                    }
                    Log.d("TEST", bList);
                    //send query to customView
                    //Toast toast = Toast.makeText(getContext().getApplicationContext(), bList, Toast.LENGTH_SHORT);
                    //toast.show();*/
                }

                //error
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e("ERR", "onCancelled", databaseError.toException());
                }
            });
        }

       /* CameraCharacteristics cc = null;
        try {
            cc = manager.getCameraCharacteristics(cameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        final CameraCharacteristics fcc = cc; */

        //if(tempView != null)
          //  arViewPane.removeView(tempView);



        /*
        tempView = new View(getContext()){
            private int canvasWidth, canvasHeight;

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


                    float hFOV = (float) (2 * Math.atan(
                            (fcc.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE).getWidth() /
                                    (2 * fcc.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)[0])
                            )));
                    float vFOV = (float) (2 * Math.atan(
                            (fcc.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE).getHeight() /
                                    (2 * fcc.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)[0])
                            )));


                    canvas.rotate((float) (0.0f - Math.toDegrees(orientation[2])));
                    float dx = (float) ((canvas.getWidth() / hFOV) * (Math.toDegrees(orientation[0]) - curBearing));
                    float dy = (float) ((canvas.getHeight() / vFOV) * Math.toDegrees(orientation[1]));
                    // Log.d("ORI[0]/BEARING:", orientation[0]+"/"+curBearing);
                    // Log.d("o/d", (orientation[0]-curBearing)+"");
                    Log.d("DX/DY:", dx + "/" + dy);
                    float testx = dx / -100;

                    // wait to translate the dx so the horizon doesn't get pushed off
                    // canvas.translate(0.0f, 0.0f - dy);

                    // make our line big enough to draw regardless of rotation and translation
                    canvas.drawLine(0f - canvas.getHeight(), canvas.getHeight() / 2, canvas.getWidth() + canvas.getHeight(), canvas.getHeight() / 2, targetPaint);


                    // now translate the dx
                    //canvas.translate(0.0f - dx, 0.0f);

                    // draw our point -- we've rotated and translated this to the right spot already
                    Log.d("w/h: ", canvas.getWidth() + "/" + canvas.getHeight());

                    canvas.drawCircle(testx, canvas.getHeight() / 2, 100, targetPaint);


                }
            }

            @Override
            protected void onSizeChanged(int w, int h, int oldw, int oldh) {
                super.onSizeChanged(w, h, oldw, oldh);
                canvasWidth = w;
                canvasHeight = h;
            }
        };
*/


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
        boolean onReady();
    }
}