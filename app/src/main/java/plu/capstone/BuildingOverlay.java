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
import android.util.SizeF;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


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
    private RelativeLayout arViewPane, buttonsView, buildingView;

    private  String accelData, aData, compassData, cData, gyroData, gData, bearing, b, gps, g, ori, o;
    // private DatabaseReference mDatabase;
    private FrameLayout buildingInfo;

    private Button buildingButton;
    private int count = 1;

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

    public void pauseLoc() {
        if (mListener != null) {
            mListener.pauseLoc();
        }
    }

    public void resumeLoc(){
        if(mListener != null) {
            mListener.resumeLoc();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        //instantiate database
        // mDatabase = FirebaseDatabase.getInstance().getReference();

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

    /*
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
     */

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public void update(String aData, String cData, String gData, String b,
                       String g, String o, ArrayList<PointOfInterest> poiList, CameraManager manager, String cameraId){

        CameraCharacteristics cc = null;

        try {
            cc = manager.getCameraCharacteristics(cameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        //Log.d("poiListSize",poiList.size()+"?");

        CameraCharacteristics fcc = cc;
        //tempView.setOptions(aData, cData, gData, b, g, o, poiList.get(0).getOrientation(),
        //      poiList.get(0).getCurBearing(), cc);

        //TODO:calculate query
        //     Query testQuery = mDatabase.child("Pacific Lutheran University/Buildings");

       /* float hFOV = (float) (2 * Math.atan(
                (fcc.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE).getWidth() /
                        (2 * fcc.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)[0])
                )));
*/
        float vFOV = (float) (2 * Math.atan(
                (fcc.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE).getHeight() /
                        (2 * fcc.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)[0])
                )));


        if(buttonsView!=null){
            buttonsView.removeAllViews();
            //arViewPane.removeView(buttonsView);
        }
        buttonsView = (RelativeLayout)getView().findViewById(R.id.bView);

        //TODO:make better check
        if(poiList.size()>=0) {
            String temp = "";
            for(int i=0;i<poiList.size();i++){
                temp += i + ": " + poiList.get(i).getBuilding().getName() + "\n";
            }
            Log.d("BuildingList",temp);


            for(int i=0;i<poiList.size();i++) {

                //IF USER IS AT A BUILDING:
                if(poiList.get(i).getDistance()<40){
                    buildingButton = new BuildingButton(getContext(), getView().getWidth(), getView().getHeight(), poiList.get(i).getOrientation());
                    buildingButton.setX(getView().getWidth()/2);
                    buildingButton.setY(getView().getHeight() - getView().getHeight()/16);
                    buildingButton.setTag(i);
                    buildingButton.setText("You are at \n" + poiList.get(i).getBuilding().getName());

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    //params.leftMargin = getView().getWidth()/2;
                    //params.topMargin = getView().getHeight() / 2;

                    buttonsView.addView(buildingButton, params);


                }

                else {

                    //Calculate fov
                    float hFOV = (float) (2 * Math.atan(40 /
                            2 * poiList.get(i).getDistance()
                    ));


                    //normalize about the fov

                    float dx = (float) ((getView().getWidth() / Math.toDegrees(hFOV)) * (Math.toDegrees(poiList.get(i).getOrientation()[0]) - poiList.get(i).getCurBearing()));
                    //float dx = (float) ((getView().getWidth() / hFOV) * (Math.toDegrees(poiList.get(i).getOrientation()[0]) - poiList.get(i).getCurBearing()));
                    float dy = (float) ((getView().getHeight() / Math.toDegrees(vFOV)) * Math.toDegrees(poiList.get(i).getOrientation()[1]));
                    float testx = dx;
                    float testy = dy;
                    //Log.d("width", getView().getWidth() + "");


                    buildingButton = new BuildingButton(getContext(), getView().getWidth(), getView().getHeight(), poiList.get(i).getOrientation());
                    buildingButton.setX(0);
                    buildingButton.setY(0);
                    buildingButton.setTag(i);
                    buildingButton.setText(poiList.get(i).getBuilding().getName() + "\n" + poiList.get(i).getDistance());
                    buildingButton.setRotation((float) (0.0f - Math.toDegrees(poiList.get(i).getOrientation()[2])));
                    buildingButton.setTranslationX(testx / -1);
                    //buildingButton.setTranslationY(testy);
                    //buildingButton.setTranslationY(testy);

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    //params.leftMargin = getView().getWidth()/2;
                    params.topMargin = getView().getHeight() / 2;

                    buttonsView.addView(buildingButton, params);

                    Log.d("dx/xtran/ytran", buildingButton.getText() + ": " + dx + "/" + buildingButton.getTranslationX() + "/" + buildingButton.getTranslationY());
                    Log.d("HFOV", buildingButton.getText() + ":" + Math.toDegrees(hFOV));

                }

            }

            for(int i=0;i<buttonsView.getChildCount();i++){
                final Buildings poi = poiList.get((int)buttonsView.getChildAt(i).getTag()).getBuilding();
                final BuildingButton bu = (BuildingButton)buttonsView.getChildAt(i);
                buttonsView.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v){
                        Log.d("HI","HIHIHIH");
                        generateBuildingInfo(bu,poi);
                    }
                });
            }
        }
    }

    /**
     * helper method to create buildinginfoview
     * TODO:make this a class!
     * @param bu
     * @param poi (find a way to only pass that building)
     */
    private void generateBuildingInfo(BuildingButton bu, Buildings poi){
        bu.setText("HIHIH");

        if(buildingView!=null){
            buildingView.removeAllViews();
            //arViewPane.removeView(buttonsView);
        }
        buildingView = (RelativeLayout)getView().findViewById(R.id.iView);

        Button tempButton = new Button(getContext());
        tempButton.setText("return");

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getView().getWidth()/8;
        params.topMargin = getView().getHeight()/8;

        buildingView.addView(tempButton,params);
        tempButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                Log.d("HI","hi from tempbutton");
                buildingView.removeAllViews();
                resumeLoc();
            }
        });

        Log.d("BHEIGHT",buttonsView.getHeight()+"");
        /**
         * generate building name
         */
        TextView buildingName = new TextView(getContext());
        buildingName.setBackgroundColor(Color.parseColor("#efbb37"));
        buildingName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        buildingName.setHeight(100);

        buildingName.setText(poi.getName());
        Log.d("???",poi.getName());

        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 0;
        params.topMargin = 100;

        buildingView.addView(buildingName,params);


        /**
         * generate building description
         * TODO:make this a tooltitlething
         */
        TextView buildingDesc = new TextView(getContext());
        buildingDesc.setText(poi.getDescription());
        buildingDesc.setBackgroundColor(Color.parseColor("#ededed"));

        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getView().getWidth()/8;
        params.topMargin = getView().getHeight() - getView().getHeight()/3;

        buildingDesc.setHeight(getView().getHeight()/6);
        buildingDesc.setWidth(getView().getWidth()/2);

        buildingView.addView(buildingDesc,params);



        buttonsView.removeAllViews();


        pauseLoc();

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

        void pauseLoc();

        void resumeLoc();
    }
}
