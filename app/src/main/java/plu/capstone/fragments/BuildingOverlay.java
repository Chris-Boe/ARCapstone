package plu.capstone.fragments;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.GeomagneticField;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;

import plu.capstone.UI.BuildingButton;
import plu.capstone.Models.Buildings;
import plu.capstone.Models.PointOfInterest;
import plu.capstone.R;
import plu.capstone.deprecated.customView;


/**
 * A Fragment to display where the buildings are on the screen and information about said building
 */
public class BuildingOverlay extends Fragment {
 


    private customView tempView;
    private RelativeLayout arViewPane, buttonsView;
    private BuildingButton buildingButton,compass;
    private OnFragmentInteractionListener mListener;
    private ViewGroup buildingView;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    public BuildingOverlay() {
        // Required empty public constructor
    }

    /**
     * instantiates a new BuildingOverlay, no parameter needed
     */
    public static BuildingOverlay newInstance() {
        BuildingOverlay fragment = new BuildingOverlay();

        return fragment;
    }

    /**
     * Tells ar (and then sensorsfragment) to pause location
     */
    public void pauseLoc() {
        if (mListener != null) {
            mListener.pauseLoc();
        }
    }

    /**
     * Tells ar (and then sensorsfragment) to resume location
     */
    public void resumeLoc(){
        if(mListener != null) {
            mListener.resumeLoc();
        }
    }

   
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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


    public void update(ArrayList<PointOfInterest> poiList){


        CameraCharacteristics cc = null;
        Log.d("hi", "NEW LOCATION\n");

        /*
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
*//*
        float vFOV = (float) (2 * Math.atan(
                (fcc.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE).getHeight() /
                        (2 * (fcc.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)[0] - 40))
                )));
*/

        if(buttonsView!=null){
            buttonsView.removeAllViews();
            //arViewPane.removeView(buttonsView);
        }
        buttonsView = (RelativeLayout)getView().findViewById(R.id.bView);

        //TODO:make better check
        if(poiList.size()>=1) {
            if(compass!=null){
                arViewPane.removeView(compass);
            }
            compass = new BuildingButton(getContext());
            compass.setVisibility(View.INVISIBLE);
            arViewPane.addView(compass);
            compass.setBackgroundColor(Color.TRANSPARENT);
            compass.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.compass,0,0);
            compass.setText("");
            compass.setTranslationX(getView().getWidth()-(getView().getWidth()/10));
            compass.setTranslationY(getView().getHeight()-getView().getHeight()/16);

            float rotDeg = (float)Math.toDegrees(poiList.get(0).orientation[0]);

                 //   if(rotDeg<0)
                   //     rotDeg = 180 - rotDeg;

            compass.setRotation(rotDeg);

            PointOfInterest closestBuilding = poiList.get(0);
            for(PointOfInterest poi : poiList){
                if(poi.distance<closestBuilding.distance)
                    closestBuilding = poi;
            }
           Log.d("closest",closestBuilding.building.Name+","+closestBuilding.distance);

            for(int i=0;i<poiList.size();i++) {

                PointOfInterest poiB = poiList.get(i);

                buildingButton = new BuildingButton(getContext());

                buildingButton.setTag(i);
                String idString = poiB.building.icon;
                if(idString==null) {
                    idString = "building";
                    Log.d("uhuh","null");
                }
                int imgId = getResources().getIdentifier(idString , "drawable", getActivity().getPackageName());
                if(imgId==0){
                    Log.d("oops","need to fix icon name "+poiList.get(i).building.Name);
                    imgId = getResources().getIdentifier("building" , "drawable", getActivity().getPackageName());
                }

                buildingButton.setCompoundDrawablesWithIntrinsicBounds(0,imgId,0,0);
                //buildingButton.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.home_icon_silhouette,0,0);
                buildingButton.setCompoundDrawablePadding(15);
                buildingButton.setAllCaps(false);
                buildingButton.setVisibility(View.INVISIBLE);

                //IF USER IS AT A BUILDING:
                if(poiList.get(i).distance == closestBuilding.distance && poiList.get(i).distance<55){

                    buildingButton.setPadding(25,25,25,25);
                    Spannable str = new SpannableStringBuilder("   You are at: "+poiList.get(i).building.Name+"   ");
                    str.setSpan(new BackgroundColorSpan(Color.parseColor("#eeeeee")), 0,
                                (str.length()), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    buildingButton.setText(str);

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    //params.leftMargin = getView().getWidth()/2;
                    //params.topMargin = getView().getHeight() / 2;
                    buildingButton.setVisibility(View.VISIBLE);
                    buttonsView.addView(buildingButton, params);


                }

                else {


                    //Calculate fov
                    float hFOV = (float) (2 * Math.atan(40 /
                            ( 2 * (poiB.distance - 20))
                    ));

                    double bearingTo = poiB.curBearing;
                    //convert az to (0,360 d]
                    double azDeg = poiB.azdeg;  //Math.toDegrees(poiB.orientation[0]);
                    Log.d("Azimutttttth Before", azDeg+"");

                    //Log.d("Azimutttttth After", azDeg+"");
                   /* if(azDeg<0)
                        azDeg = 180 - azDeg;
                    if(bearingTo<0)
                        bearingTo = 180 - bearingTo;
                    */
                    double degreeDifference = Math.abs(bearingTo-azDeg);
                    Log.d("DegreeDif", degreeDifference+"");
                    //normalize about the fov
                    float dx = (float) ((getView().getWidth()/Math.toDegrees(hFOV)) * degreeDifference);

                    /*
                    float dy = (float) ((getView().getWidth() / Math.toDegrees(hFOV)) * Math.abs((poiB.curBearing - -1*Math.toDegrees(poiB.orientation[1]))));
*/

                    //Log.d("width", getView().getWidth() + "");

                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    //params.leftMargin = getView().getWidth()/2;
                    params.topMargin = getView().getHeight() / 2;

                    buttonsView.addView(buildingButton, params);

                    //rotate around y axis
                    buildingButton.setRotation((float) (0.0f - Math.toDegrees(poiB.orientation[2])));


                    Spannable str = new SpannableStringBuilder("   "+poiB.building.Name+"   ");

                    //buildingButton.setTranslationX(getView().getWidth()/2);
                    if(bearingTo>azDeg)
                        buildingButton.setTranslationX(dx);
                    else
                        buildingButton.setTranslationX(getView().getWidth()- -1*dx);

                    str.setSpan(new BackgroundColorSpan(Color.parseColor("#eeeeee")), 0,
                            (str.length()), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    buildingButton.setText(str);

                   // buildingButton.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    //  if(poiList.get(i).getBuilding().getName().equals("Rieke Science Center")) {
                    Log.d("dx/xpos:", dx+"/"+buildingButton.getX());
                    Log.d("oriBearing:",Math.toDegrees(poiB.orientation[0])+"");
                    Log.d("curBearing:", bearingTo + "");
                    Log.d("azimuth:", azDeg + "");
                    Log.d("difference:", degreeDifference + "");
                    //Log.d("directions:", "NORTH: " + 0 + " EAST: " + Math.toDegrees(Math.PI / 2) + " WEST: " + Math.toDegrees(Math.PI+Math.PI/2) + " SOUTH: " + Math.toDegrees(Math.PI));
                    //Log.d("HFOV", buildingButton.getText() + ":" + Math.toDegrees(hFOV));
                    // Log.d("dx/xtran/bearing/azi", poiList.get(i).getBuilding().getName() + ": " + dx + "/" + buildingButton.getX() + "/" + poiList.get(i).getCurBearing()+"/"+Math.toDegrees(poiList.get(i).getOrientation()[0]));
                    //}*/
                    buildingButton.setVisibility(View.VISIBLE);
                }

            }

            final ArrayList<PointOfInterest> poi2List = poiList;
            new Thread(new Runnable() {
                public void run() {
                     buttonsView.post(new Runnable() {
                        public void run() {
                            for(int i=0;i<buttonsView.getChildCount();i++){
                                try {
                                    final Buildings poi = poi2List.get((int) buttonsView.getChildAt(i).getTag()).building;
                                    final BuildingButton bu = (BuildingButton) buttonsView.getChildAt(i);
                                    buttonsView.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                                        @RequiresApi(api = Build.VERSION_CODES.M)
                                        public void onClick(View v) {
                                            generateBuildingInfo(bu, poi);
                                        }
                                    });
                                }catch(IndexOutOfBoundsException ind){
                                    break;
                                }
                            }
                        }
                    });
                }
            }).start();



        }
    }

    /**
     * concurrently generate buttons
     */
    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * helper method to create buildinginfoview
     * TODO:make this a class!
     * @param bu
     * @param poi (find a way to only pass that building)
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void generateBuildingInfo(BuildingButton bu, Buildings poi){
        buttonsView.removeAllViews();
        pauseLoc();

        if(compass!=null){
            arViewPane.removeView(compass);
        }

        if(buildingView!=null){
            buildingView.removeAllViews();
            //arViewPane.removeView(buttonsView);
        }

        //layouts
        LinearLayout parentLayout = (LinearLayout) getView().findViewById(R.id.layoutparent);
        final FrameLayout tabLayout = (FrameLayout) getView().findViewById(R.id.tablayout);
        final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        final EventsViewFragment event = new EventsViewFragment().newInstance("EventsViewFragment","loc",poi.Name,"ar");


        generateTabs(poi,ft,tabLayout, event);

        generateBuildingName(poi, event,tabLayout, ft);


    }

    /**
     * Generate the tablayout for building information and events
     * @param poi building information
     * @param ft fragmenttransaction
     * @param tabLayout holder for the tablayout
     * @param event holder for the eventsviewfragment
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void generateTabs(Buildings poi, final FragmentTransaction ft, final FrameLayout tabLayout, final EventsViewFragment event){
        final LinearLayout tabCenter = new LinearLayout(getContext());

        tabCenter.setOrientation(LinearLayout.VERTICAL);
        tabCenter.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams tabParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        final ScrollView scrollView = new ScrollView(getContext());
        scrollView.setBackgroundColor(Color.parseColor("#eeeeee"));

        final TextView buildingInfo = new TextView(getContext());
        buildingInfo.setText(poi.Description);
        scrollView.addView(buildingInfo);
        scrollView.setId(R.id.tab1);


        final ScrollView.LayoutParams scrollParams =
                new ScrollView.LayoutParams(getView().getWidth() - getView().getWidth()/3,getView().getWidth() - getView().getWidth()/4);


        LinearLayout tabButtons = new LinearLayout(getContext());

        final RelativeLayout eView = new RelativeLayout(getContext());;
        final Buildings fPoi = poi;
        tabButtons.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(getView().getWidth() - getView().getWidth()/3, LinearLayout.LayoutParams.WRAP_CONTENT);
        //building info
        final Button tab1 = new Button(getContext());
        tab1.setBackgroundColor(getResources().getColor(R.color.colorPrimary,null));
        tab1.setText("Building Information");
        tab1.setTextColor(getResources().getColor(R.color.colorBaseWhite,null));
        tab1.setHeight(25);
        tab1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        tab1.setGravity(Gravity.CENTER);
        Button tab2 = new Button(getContext());
        tab2.setText("Building Events");
        tab2.setHeight(25);
        tab2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        tab2.setGravity(Gravity.CENTER);
        tab2.setTextColor(getResources().getColor(R.color.colorBaseWhite,null));
        tab2.setBackgroundColor(getResources().getColor(R.color.colorPrimary,null));

        tab1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(!ft.isEmpty()) {
                    try {
                        tabCenter.removeView(eView);
                    } catch (NullPointerException e) {

                    }
                    buildingInfo.setText(fPoi.Description);
                    if(tabCenter.findViewById(R.id.tab1)==null)
                        scrollView.addView(buildingInfo);

                    if(tabCenter.findViewById(R.id.tab1)==null)
                        tabCenter.addView(scrollView,scrollParams);
                }
            }
        });

        //events list

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

        tabButtons.addView(tab2, new LinearLayout.LayoutParams((getView().getWidth() - getView().getWidth()/3)/2,LinearLayout.LayoutParams.WRAP_CONTENT));
        tabButtons.addView(tab1, new LinearLayout.LayoutParams((getView().getWidth() - getView().getWidth()/3)/2,LinearLayout.LayoutParams.WRAP_CONTENT));
        tabButtons.setGravity(Gravity.CENTER_HORIZONTAL);
        tabCenter.addView(tabButtons);
        if(tabCenter.findViewById(scrollView.getId())==null)
            tabCenter.addView(scrollView,scrollParams);
        tabLayout.addView(tabCenter,tabParams);
    }

    /**
     * generates building names
     * @param poi building info
     * @param event eventsfragment
     * @param tabLayout layout for tabs
     * @param ft fragmenttransaction
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void generateBuildingName(Buildings poi, final EventsViewFragment event, final FrameLayout tabLayout, final FragmentTransaction ft){
        final LinearLayout barLayout = (LinearLayout) getView().findViewById(R.id.barlayout);
        barLayout.setBackgroundColor(getResources().getColor(R.color.colorAccent,null));

        //Generate building name

        LinearLayout nameBar = new LinearLayout(getContext());

        TextView name = new TextView(getContext());
        name.setText(poi.Name);
        name.setPadding(25,25,35,25);
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        name.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        barLayout.addView(name,params);

        //generate exit button
        Button exitB = new Button(getContext());
        exitB.setText("return");
        exitB.setBackgroundColor(getResources().getColor(R.color.lightAccent,null));
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

        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity=Gravity.RIGHT;
        params.setMargins(0,0,35,25);
        barLayout.addView(exitB,params);

    }




    /**
     * Interface to interact with activity
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name

        boolean onReady();

        /**
         * pauses location services
         */
        void pauseLoc();

        /**
         * resumes location services
         */
        void resumeLoc();
    }
}
