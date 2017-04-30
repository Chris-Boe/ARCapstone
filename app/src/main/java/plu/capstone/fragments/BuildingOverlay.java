package plu.capstone.fragments;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.database.Query;

import java.util.ArrayList;

import plu.capstone.UI.BuildingButton;
import plu.capstone.Models.Buildings;
import plu.capstone.Models.PointOfInterest;
import plu.capstone.R;
import plu.capstone.deprecated.customView;


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
    private RelativeLayout arViewPane, buttonsView;
    private GridLayout buildingView;

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
        Log.d("hi", "NEW LOCATION\n");

        try {
            cc = manager.getCameraCharacteristics(cameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        //Log.d("poiListSize",poiList.size()+"?");

        //calculate fov with hardware
        CameraCharacteristics fcc = cc;
        //tempView.setOptions(aData, cData, gData, b, g, o, poiList.get(0).getOrientation(),
        //      poiList.get(0).getCurBearing(), cc);


       /* float hFOV = (float) (2 * Math.atan(
                (fcc.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE).getWidth() /
                        (2 * fcc.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)[0])
                )));
*/
        float vFOV = (float) (2 * Math.atan(
                (fcc.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE).getHeight() /
                        (2 * (fcc.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)[0] - 40))
                )));


        if(buttonsView!=null){
            buttonsView.removeAllViews();
            //arViewPane.removeView(buttonsView);
        }
        buttonsView = (RelativeLayout)getView().findViewById(R.id.bView);

        //TODO:make better check
        if(poiList.size()>=1) {
            PointOfInterest closestBuilding = poiList.get(0);
            for(int i=0;i<poiList.size();i++){
                if(poiList.get(i).getDistance()<closestBuilding.getDistance())
                    closestBuilding = poiList.get(i);
            }
           Log.d("closest",closestBuilding.getBuilding().getName()+","+closestBuilding.getDistance());

            for(int i=0;i<poiList.size();i++) {

                //IF USER IS AT A BUILDING:
                if(poiList.get(i).getDistance() == closestBuilding.getDistance() && poiList.get(i).getDistance()<55){
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
                            ( 2 * (poiList.get(i).getDistance() - 30))
                    ));


                    double bearingTo = poiList.get(i).getCurBearing();
                    //convert az to (0,360 d]
                    double azDeg = Math.toDegrees(poiList.get(i).getOrientation()[0]);

                    if(azDeg<0)
                        azDeg = 180 - azDeg;
                    if(bearingTo<0)
                        bearingTo = 180 - bearingTo;

                    double degreeDifference = bearingTo-azDeg;

                    //normalize about the fov
                   float dx = (float) ((getView().getWidth()/Math.toDegrees(hFOV)) * degreeDifference);
                //    float dx = (float) ( (getView().getWidth()/ hFOV) * (Math.toDegrees(poiList.get(i).getOrientation()[0])- poiList.get(i).getCurBearing()));

                    //float dx = (float) ((getView().getWidth() / Math.toDegrees(hFOV)) * Math.abs((poiList.get(i).getCurBearing() - -1*Math.toDegrees(poiList.get(i).getOrientation()[0]))));
                   // float dx = (float) ((getView().getWidth() / Math.toDegrees(hFOV)) * (Math.toDegrees(poiList.get(i).getOrientation()[0]) - poiList.get(i).getCurBearing()));
                 // float dx = (float) ((getView().getWidth() / Math.toDegrees(hFOV)) * (-1*Math.toDegrees(poiList.get(i).getOrientation()[0]) - poiList.get(i).getCurBearing()));//((Math.toDegrees(poiList.get(i).getOrientation()[0]) - poiList.get(i).getCurBearing());
                   // float dy = (float) ((getView().getHeight() / Math.toDegrees(vFOV)) * Math.toDegrees(poiList.get(i).getOrientation()[1]));
                    float dy = (float) ((getView().getWidth() / Math.toDegrees(hFOV)) * Math.abs((poiList.get(i).getCurBearing() - -1*Math.toDegrees(poiList.get(i).getOrientation()[1]))));

                    //Log.d("width", getView().getWidth() + "");


                    buildingButton = new BuildingButton(getContext(), getView().getWidth(), getView().getHeight(), poiList.get(i).getOrientation());

                    buildingButton.setX(0);
                    buildingButton.setY(0);
                    buildingButton.setTag(i);
                    buildingButton.setText(poiList.get(i).getBuilding().getName() + "\nb" + bearingTo + "\na" + azDeg);

                    //rotate around y axis
                    buildingButton.setRotation((float) (0.0f - Math.toDegrees(poiList.get(i).getOrientation()[2])));

                    //translate around z axis
                    //if building is to left
                    if(degreeDifference>=0)
                        buildingButton.setTranslationX(dx);
                    else
                        buildingButton.setTranslationX(getView().getWidth()- -1*dx);
                   // buildingButton.setTranslationY(testy);

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    //params.leftMargin = getView().getWidth()/2;
                    params.topMargin = getView().getHeight() / 2;

                    buttonsView.addView(buildingButton, params);

                  //  if(poiList.get(i).getBuilding().getName().equals("Rieke Science Center")) {
                        Log.d("dx/xpos:", dx+"/"+buildingButton.getX());
                        Log.d("oriBearing:",poiList.get(i).getCurBearing()+"");
                        Log.d("curBearing:", bearingTo + "");
                        Log.d("azimuth:", azDeg + "");
                        Log.d("difference:", degreeDifference + "");
                        //Log.d("directions:", "NORTH: " + 0 + " EAST: " + Math.toDegrees(Math.PI / 2) + " WEST: " + Math.toDegrees(Math.PI+Math.PI/2) + " SOUTH: " + Math.toDegrees(Math.PI));
                        Log.d("HFOV", buildingButton.getText() + ":" + Math.toDegrees(hFOV));
                        // Log.d("dx/xtran/bearing/azi", poiList.get(i).getBuilding().getName() + ": " + dx + "/" + buildingButton.getX() + "/" + poiList.get(i).getCurBearing()+"/"+Math.toDegrees(poiList.get(i).getOrientation()[0]));
                    //}

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
        buttonsView.removeAllViews();
        pauseLoc();

        if(buildingView!=null){
            buildingView.removeAllViews();
            //arViewPane.removeView(buttonsView);
        }

        //layouts
        LinearLayout parentLayout = (LinearLayout) getView().findViewById(R.id.layoutparent);
        final FrameLayout tabLayout = (FrameLayout) getView().findViewById(R.id.tablayout);
        final RelativeLayout barLayout = (RelativeLayout) getView().findViewById(R.id.barlayout);
        barLayout.setBackgroundColor(Color.parseColor("#f7c738"));




        //Generate building info tabs
        final LinearLayout tabCenter = new LinearLayout(getContext());
        tabCenter.setOrientation(LinearLayout.VERTICAL);
        tabCenter.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams tabParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            final ScrollView scrollView = new ScrollView(getContext());
            scrollView.setBackgroundColor(Color.parseColor("#eeeeee"));

                final TextView buildingInfo = new TextView(getContext());
                buildingInfo.setText(poi.getDescription());
                scrollView.addView(buildingInfo);
                scrollView.setId(R.id.tab1);


            final ScrollView.LayoutParams scrollParams =
                new ScrollView.LayoutParams(getView().getWidth() - getView().getWidth()/3,getView().getWidth() - getView().getWidth()/4);


            LinearLayout tabButtons = new LinearLayout(getContext());

        final RelativeLayout eView = new RelativeLayout(getContext());;
        final EventsViewFragment event = new EventsViewFragment().newInstance("EventsViewFragment","loc",poi.getName(),"ar");
        final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            final Buildings fPoi = poi;
            tabButtons.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(getView().getWidth() - getView().getWidth()/3, LinearLayout.LayoutParams.WRAP_CONTENT);
                //building info
                final Button tab1 = new Button(getContext());
                tab1.setText("Building Information");
                tab1.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if(!ft.isEmpty()) {
                            try {
                                tabCenter.removeView(eView);
                            } catch (NullPointerException e) {

                            }
                            buildingInfo.setText(fPoi.getDescription());
                            if(tabCenter.findViewById(R.id.tab1)==null)
                                scrollView.addView(buildingInfo);

                            if(tabCenter.findViewById(R.id.tab1)==null)
                                tabCenter.addView(scrollView,scrollParams);
                        }
                    }
                });

                //events list
                Button tab2 = new Button(getContext());
                tab2.setText("Building Events");
        final RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(getView().getWidth() - getView().getWidth() / 3, getView().getWidth() - getView().getWidth() / 4);
                tab2.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if(scrollView.getChildCount()!=0)
                            scrollView.removeAllViews();

                        if(ft.isEmpty()) {



                            eView.setId(R.id.fragment_events_view);
                            eView.setBackgroundColor(Color.parseColor("#eeeeee"));

                            tabCenter.removeView(scrollView);
                            tabCenter.addView(eView, param);

                            ft.replace(eView.getId(), event, "events");
                            ft.commit();

                        }
                        else {
                            tabCenter.removeView(scrollView);
                            if(tabCenter.findViewById(R.id.fragment_events_view)==null)
                            tabCenter.addView(eView, param);
                        }


                    }
                });

                tabButtons.addView(tab1, new LinearLayout.LayoutParams((getView().getWidth() - getView().getWidth()/3)/2,LinearLayout.LayoutParams.WRAP_CONTENT));
                tabButtons.addView(tab2, new LinearLayout.LayoutParams((getView().getWidth() - getView().getWidth()/3)/2,LinearLayout.LayoutParams.WRAP_CONTENT));
                tabButtons.setGravity(Gravity.CENTER_HORIZONTAL);
            tabCenter.addView(tabButtons);
            if(tabCenter.findViewById(scrollView.getId())==null)
                tabCenter.addView(scrollView,scrollParams);
        tabLayout.addView(tabCenter,tabParams);





        //Generate building name
        TextView name = new TextView(getContext());
        name.setText(poi.getName());
        name.setPadding(25,25,35,25);
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        name.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);

        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        barLayout.addView(name,params);

        //generate exit button
        Button exitB = new Button(getContext());
        exitB.setText("return");
        exitB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                Log.d("HI","hi from tempbutton");
                if(tabLayout.getChildCount()!=0) {
                    tabLayout.removeAllViews();
                }
                if(barLayout.getChildCount()!=0)
                    barLayout.removeAllViews();

                if(!ft.isEmpty())
                    ft.remove(event);

                resumeLoc();
            }
        });

        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(25,25,25,25);
        barLayout.addView(exitB,params);




        /*

        TextView buildingName = new TextView(getContext());
        buildingName.setBackgroundColor(Color.parseColor("#efbb37"));
        buildingName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        buildingName.setHeight(100);

        buildingName.setText(poi.getName());
        Log.d("???",poi.getName());

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 0;
        params.topMargin = 0;


        buildingView.addView(buildingName,params);

        //EVENT BUTTON
        RelativeLayout layout = (RelativeLayout)getView().findViewById(R.id.eView);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) layout.getLayoutParams();
        layoutParams.leftMargin = getView().getWidth() / 6;
        layoutParams.topMargin = getView().getHeight() / 3;
        layoutParams.height = 800;
        layoutParams.width = 500;


        layout.setLayoutParams(layoutParams);

        final Button eventButton = new Button(getContext());
        final EventsViewFragment event = new EventsViewFragment();
        final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

        eventButton.setText("EVENTS!");
        eventButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ft.replace(R.id.eView, event, "events");
                ft.commit();
                RelativeLayout eView = (RelativeLayout) getView().findViewById(R.id.eView);


                Button exit = new Button(getContext());
                exit.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        ft.detach(event);

                    }
                });
                eView.addView(exit);

            }
        });

        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getView().getWidth()/8;
        params.topMargin = getView().getHeight()/6;

        buildingView.addView(eventButton,params);


        //REMOVE BUTTON
        Button tempButton = new Button(getContext());
        tempButton.setText("return");

        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getView().getWidth()/8;
        params.topMargin = getView().getHeight()/8;

        buildingView.addView(tempButton,params);
        tempButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v){
                Log.d("HI","hi from tempbutton");
                buildingView.removeAllViews();

                if(!ft.isEmpty()) {
                    Log.d("srsly","Srsly");
                    ft.remove(event);
                }

                resumeLoc();
            }
        });

        //BUILDING DESC
        TextView buildingDesc = new TextView(getContext());
        buildingDesc.setText(poi.getDescription());
        buildingDesc.setBackgroundColor(Color.parseColor("#ededed"));

        params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.leftMargin = getView().getWidth()/8;
        params.topMargin = getView().getHeight() - getView().getHeight()/3;

        buildingDesc.setHeight(getView().getHeight()/6);
        buildingDesc.setWidth(getView().getWidth()/2);

        buildingView.addView(buildingDesc,params);

**/

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
