package plu.capstone.fragments;

import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.GeomagneticField;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.api.services.calendar.model.Events;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import plu.capstone.Models.CustomEvent;
import plu.capstone.adapters.ExpandableListAdapter;
import plu.capstone.R;
import plu.capstone.dialogs.EventsSearchDialog;
import plu.capstone.dialogs.KeywordSearchDialog;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.GET_ACCOUNTS;
import static android.app.Activity.RESULT_OK;

/**
 * Created by cboe1 on 4/19/2017.
 */

public class EventsViewFragment extends Fragment implements EasyPermissions.PermissionCallbacks {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String PARAM_VALUE = "param1";
    private static final String PARAM_KEY = "param2";
    private static final String PARAM_PARENT = "param3";

    // TODO: Rename and change types of parameters
    private String paramValue;
    private String paramKey;
    private String paramParent;

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    private ArrayList<String> listHeaders;
    private HashMap<String, ArrayList<String>> listChildren;
    private HashMap<String, CustomEvent> eventsMap;
    //private DatabaseEvents dbe;
    private DatabaseReference mDatabase;
    private TextView textView;
    private TextView tipsTextView;
    GoogleAccountCredential mCredential;
    private Button mCallApiButton;
    ProgressDialog mProgress;
    static final int REQUEST_ACCOUNT_PICKER = 1000;
    static final int REQUEST_AUTHORIZATION = 1001;
    static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    private static final String BUTTON_TEXT = "Connect to GCal";
    private static final String PREF_ACCOUNT_NAME = "accountName";
    private static final String[] SCOPES = { CalendarScopes.CALENDAR};
    ArrayList<Event> savedEvents;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    private ActionBar actionBar;
    boolean hasCalendar;
    private EventsSearchDialog.EventsSearchListener eListener;

    private ArrayList<ArrayList<String>> eventsList; //name, date, desc, link, loc

    /*
    OnEventAddedListener mCallback;
    public interface OnEventAddedListener{
        public void onEventAdded(Event e);
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try{
            mCallback = (OnEventAddedListener) context;
        }catch(ClassCastException b){
            throw new ClassCastException(context.toString() + " must implement OnEventAddedListener");
        }
    }*/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_events_view, container, false);
        tipsTextView = (TextView) view.findViewById(R.id.tipsTextView);
        textView = (TextView) view.findViewById(R.id.apiTextView);
        prefs = getActivity().getPreferences(0);
        editor = prefs.edit();
        hasCalendar = false;
        if(prefs.contains("CalID"))
            hasCalendar = true;
        final EventReminder[] reminderOverrides = new EventReminder[]{
                new EventReminder().setMethod("popup").setMinutes(30)
        };
        final Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(Arrays.asList(reminderOverrides));
        mCredential = GoogleAccountCredential.usingOAuth2(
                getContext().getApplicationContext(), Arrays.asList(SCOPES))
                .setBackOff(new ExponentialBackOff());
        /*mCallApiButton = (Button)view.findViewById(R.id.connectToGCal);
        mCallApiButton.setText(BUTTON_TEXT);
        mCallApiButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                mCallApiButton.setEnabled(false);
                textView.setText("");
                getResultsFromApi();
                mCallApiButton.setEnabled(true);
            }
        });*/
        mProgress = new ProgressDialog(getContext());
        mProgress.setMessage("Connecting to Google Calendar...");

        listHeaders = new ArrayList<>();
        listChildren = new HashMap<>();
        eventsList = new ArrayList<ArrayList<String>>();

        eventsMap = new HashMap<>();
        final Context con = this.getContext();
        savedEvents = new ArrayList<Event>();
        expListView = (ExpandableListView) view.findViewById(R.id.lvExpEvents);
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if(childPosition == 0) {
                    String date = listChildren.get(listHeaders.get(groupPosition)).get(0);
                    DateTime startDateTime;
                    DateTime endDateTime;
                    if(date.length() > 36){ //if time was found, length will be 40
                        date = date.replaceAll("Date: ", "");
                        String startTime = date.substring(date.indexOf("Time: "));
                        int dash = startTime.indexOf("-");
                        String endTime = startTime.substring(dash);
                        startTime = startTime.substring(0, dash);
                        date = date.replaceAll("/", "-");
                        date = date.substring(0, 10);
                        if (startTime.contains("-"))
                            startTime = startTime.replaceAll("-", "");
                        if (endTime.contains("-"))
                            endTime = endTime.replaceAll("-", "");
                        date = date + "T" + startTime + "-07:00";
                        date = date.replaceAll("Time: ", "");
                        date = date.replaceAll(" ", "");
                        startDateTime = new DateTime(date);
                        date = date.substring(0, 10);
                        date = date + "T" + endTime + "-07:00";
                        date = date.replaceAll("Time: ", "");
                        date = date.replaceAll(" ", "");
                        endDateTime = new DateTime(date);
                    }else{ //add fake time since nothing was found in RSS Reader description
                        date = date.replaceAll("Date: ", "");
                        date = date.replaceAll("/", "-");
                        date = date.replaceAll(" ", "");
                        date = date.substring(0, 10);
                        date = date + "T08:00:00-07:00";
                        startDateTime = new DateTime(date);
                        date = date.substring(0, 10);
                        date = date + "T09:00:00-07:00";
                        endDateTime = new DateTime(date);
                    }
                    Log.d("DateFinal", date);
                    EventDateTime start = new EventDateTime()
                            .setDateTime(startDateTime)
                            .setTimeZone("America/Los_Angeles");
                    EventDateTime end = new EventDateTime()
                            .setDateTime(endDateTime)
                            .setTimeZone("America/Los_Angeles");
                    if(!prefs.contains(listHeaders.get(groupPosition))) {
                        savedEvents.add(new Event()
                                .setDescription(listChildren.get(listHeaders.get(groupPosition)).get(1))
                                .setSummary(listHeaders.get(groupPosition))
                                .setReminders(reminders)
                                .setLocation(listChildren.get(listHeaders.get(groupPosition)).get(3))
                                .setStart(start)
                                .setEnd(end));
                        Log.d("HIOO", savedEvents.toString());
                    }else{
                        Toast toast = Toast.makeText(getContext(), "Event already in Calendar", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    getResultsFromApi();


                }
                if(childPosition == 2){
                    String url = listChildren.get(listHeaders.get(groupPosition)).get(2);
                    url = url.replaceAll("Link: ","");
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    Toast.makeText(getContext(), "Opening link in Browser...", Toast.LENGTH_SHORT);
                    startActivity(browserIntent);
                }
                return false;
            }
        });
        //gets instance/reference of database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Query eventQuery;
        //queries each event
        if(paramKey!=null && paramValue != null)
            eventQuery = mDatabase.child("Pacific Lutheran University/Events").orderByChild(paramKey).equalTo(paramValue);
        else
            eventQuery = mDatabase.child("Pacific Lutheran University/Events");

        //listener for onDataChange
        eventQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            //reads in data whenever changed (maybe find a more appropriate callback)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CustomEvent singleEvent;
                String title;
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    title = singleSnapshot.getKey();
                    addToHeaders(title);

                    singleEvent = singleSnapshot.getValue(CustomEvent.class);
                    //addToEventsMap(title, singleEvent);
                    addToEventChildren(title, singleEvent);
                    ArrayList<String> holderList = new ArrayList<String>();

                    holderList.add(title);
                    holderList.add(singleEvent.getCategory());
                    holderList.add(singleEvent.getDescription());
                    holderList.add(singleEvent.getLink());
                    holderList.add(singleEvent.getLoc());
                    holderList.add(singleEvent.getStartTime());
                    holderList.add(singleEvent.getEndTime());

                    eventsList.add(holderList);
                }
                updateList(listHeaders,listChildren);

            }

            //error
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ERR", "onCancelled", databaseError.toException());
            }
        });
        return view;
    }




    private void updateList(ArrayList list,HashMap map){
        if(list.isEmpty()){
            tipsTextView.setText("There are no events listed.");
        }else{
            tipsTextView.setText("Click an Event's date to add it to your calendar.\nClick an Event's link to open it in a browser.");
        }
        listAdapter = new ExpandableListAdapter(getContext(), list, map);
        expListView.setAdapter(listAdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            paramValue = getArguments().getString(PARAM_VALUE);
            paramKey = getArguments().getString(PARAM_KEY);
            paramParent = getArguments().getString(PARAM_PARENT);
        }
        actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        Log.d("ACTION BAR", "[ " + actionBar+" ]????");
        if(actionBar !=null){
            setHasOptionsMenu(true);
            actionBar.setTitle("Events");
        }

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(R.id.event_menu_buttons, R.id.queryBuilding, 0, "Building").setIcon(R.drawable.ic_search_black_24dp)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(R.id.event_menu_buttons,R.id.sortby,0,"Sort").setIcon(R.drawable.ic_sort_black_24dp).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final Fragment fragment = this;
        switch (item.getItemId()) {
            case R.id.sortby:

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                final String sortby[] = getResources().getStringArray(R.array.sort_options);

                builder.setTitle("What do you want to sort by?")
                        .setItems(sortby, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                ArrayList<String> heading;
                                HashMap<String, ArrayList<String>> eMap;
                                switch(which) {
                                    case 0:
                                        Log.d("0","Date");
                                        sortBy(eventsList,"date");

                                        heading = new ArrayList<String>();
                                        eMap = new HashMap<String, ArrayList<String>>();

                                        for(int i=0;i<eventsList.size();i++) {
                                            heading.add(eventsList.get(i).get(0));
                                            eMap.put(eventsList.get(i).get(0),eventsList.get(i));
                                        }

                                        Log.d("MAP",eMap.toString());

                                        updateList(heading,eMap);

                                        return;
                                    case 1:
                                        Log.d("1","Loc");
                                        sortBy(eventsList,"loc");

                                        heading = new ArrayList<String>();
                                        eMap = new HashMap<String, ArrayList<String>>();

                                        for(int i=0;i<eventsList.size();i++) {
                                            heading.add(eventsList.get(i).get(0));
                                            eMap.put(eventsList.get(i).get(0),eventsList.get(i));
                                        }

                                        Log.d("MAP",eMap.toString());

                                        updateList(heading,eMap);
                                        return;
                                    case 2:
                                        Log.d("2","Name");
                                        sortBy(eventsList,"name");

                                        heading = new ArrayList<String>();
                                        eMap = new HashMap<String, ArrayList<String>>();

                                        for(int i=0;i<eventsList.size();i++) {
                                            heading.add(eventsList.get(i).get(0));
                                            eMap.put(eventsList.get(i).get(0),eventsList.get(i));
                                        }

                                        Log.d("MAP",eMap.toString());

                                        updateList(heading,eMap);
                                        return;
                                }

                            }
                        });

                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

                //set other properties

// Create the AlertDialog
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            case R.id.queryBuilding:
                builder = new AlertDialog.Builder(getActivity());
                final String[] searchOptions = {"Building","Keyword"};

                builder.setTitle("Search by").setItems(searchOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which==0){
                            EventsSearchDialog eDialog = new EventsSearchDialog();
                            eDialog.show(getActivity().getSupportFragmentManager(), "search_building");
                        }
                        if(which==1){
                            Log.d("searchDialog","yup");
                            KeywordSearchDialog kDialog = new KeywordSearchDialog();
                            kDialog.show(getActivity().getSupportFragmentManager(),"search_words");
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //exit
                    }
                }).setPositiveButton("Show all", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //refreshing
                        paramKey = null;
                        paramValue = null;

                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .detach(fragment)
                                .attach(fragment)
                                .commit();
                    }
                });

                AlertDialog dialog2 = builder.create();
                dialog2.show();

                /*
                Log.d("HI","The button works");

                builder = new AlertDialog.Builder(getActivity());

                final String building[] = getResources().getStringArray(R.array.plu_building_list);

                builder.setTitle("Please choose a building")
                        .setItems(building, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                for(int i=0;i<building.length;i++){

                                    //building found
                                    if(i==which){
                                        Log.d("building:", building[i]);
                                        //modify list
                                        paramKey = "loc";
                                        paramValue = building[i];

                                        getActivity().getSupportFragmentManager()
                                                .beginTransaction()
                                                .detach(fragment)
                                                .attach(fragment)
                                                .commit();


                                    }
                                }
                            }
                        });

                builder.setPositiveButton("Show all", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id) {
                        //refreshing
                        paramKey = null;
                        paramValue = null;

                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .detach(fragment)
                                .attach(fragment)
                                .commit();

                    }
                });

                builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

                //set other properties

// Create the AlertDialog
                AlertDialog dialog2 = builder.create();
                dialog2.show();*/
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    public static EventsViewFragment newInstance(String text, String k, String val, String par){
        EventsViewFragment evf = new EventsViewFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);
        b.putString(PARAM_VALUE, val);
        b.putString(PARAM_KEY,k);
        b.putString(PARAM_PARENT,par);
        evf.setArguments(b);
        return evf;
    }

    /*public void addToEventsMap(String key, CustomEvent obj){
        eventsMap.put(key, obj);
    }
    public HashMap<String, CustomEvent> getEventsMap(){
        return eventsMap;
    }*/
    public void addToHeaders(String title){
        listHeaders.add(title);
    }
    public ArrayList<String> getEventHeaders(){
        Log.d("EH SIZE", listHeaders.size() + "");
        return listHeaders;
    }
    public void addToEventChildren(String s, CustomEvent e){
        ArrayList<String> details = new ArrayList<>();
        details.add("Date: " + e.getCategory() + "Time: "+e.getStartTime()+" - "+e.getEndTime());
        details.add(e.getDescription());
        details.add("Link: " + e.getLink());
        details.add("Where: " + e.getLoc());
        listChildren.put(s, details);
    }
    public HashMap<String, ArrayList<String>> getEventChildren(){
        Log.d("EC SIZE", listChildren.size() + "");
        return listChildren;
    }


    private void addEventsToCal(ArrayList<Event> events) {
        if(prefs != null){
            Iterator<Event> listIterator = events.iterator();
            while(listIterator.hasNext()) {
                Event e = listIterator.next();
                DateTime start = e.getStart().getDateTime();
                if (start == null) {
                    // All-day events don't have start times, so just use
                    // the start date.
                    start = e.getStart().getDate();
                }
                String eventDetails = String.format("%s (%s)", e.getLocation(), start);
                editor.putString(e.getSummary(), eventDetails);
            }
            editor.commit();
        }else{
            Log.d("PrefsError", " Prefs is null");
        }
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    private void getResultsFromApi() {
        if (! isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (! isDeviceOnline()) {
            textView.setText("No network connection available.");
        } else {
            new EventsViewFragment.MakeRequestTask(mCredential).execute();
        }
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private void chooseAccount() {
        if (EasyPermissions.hasPermissions(
                getContext(), GET_ACCOUNTS)) {
            String accountName = getActivity().getPreferences(Context.MODE_PRIVATE)
                    .getString(PREF_ACCOUNT_NAME, null);
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                    this,
                    "This app needs to access your Google account (via Contacts).",
                    REQUEST_PERMISSION_GET_ACCOUNTS,
                    GET_ACCOUNTS);
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    public void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    textView.setText(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");
                } else {
                    getResultsFromApi();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null &&
                        data.getExtras() != null) {
                    String accountName =
                            data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings =
                                getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    private void sortBy(ArrayList<ArrayList<String>> list, String sort){
        switch(sort){
                case "date":
                    Collections.sort(list, new Comparator<List<String>>() {
                        @Override
                        public int compare(List<String> o1, List<String> o2) {
                            String date1 = o1.get(1);
                            String date2 = o2.get(1);

                            String year1 = date1.substring(0,4);
                            String year2 = date2.substring(0,4);

                            String month1 = date1.substring(5,7);
                            String month2 = date2.substring(5,7);

                            String day1 = date1.substring(8,10);
                            String day2 = date2.substring(8,10);

                            if(year1.compareTo(year2)==0) {
                                Log.d("same year","yay");
                                if (month1.compareTo(month2) == 0) {
                                    Log.d("same month,", "yay");
                                    if(day1.compareTo(day2)==0)
                                        Log.d("???","wtf");
                                    return day1.compareTo(day2);
                                }
                                else {
                                    return month1.compareTo(month2);
                                }
                            }
                            else return year1.compareTo(year2);
                        }

                    });
                    Log.d("newlist",list.toString());
                    return;
                case "loc":
                    Collections.sort(list, new Comparator<List<String>>() {
                        @Override
                        public int compare(List<String> o1, List<String> o2) {
                            String loc1 = o1.get(4);
                            String loc2 = o2.get(4);

                            return loc1.compareTo(loc2);
                        }

                    });
                    Log.d("newlist",list.toString());
                    return;
                case "name":
                    Collections.sort(list, new Comparator<List<String>>() {
                        @Override
                        public int compare(List<String> o1, List<String> o2) {
                            String name1 = o1.get(0);
                            String name2 = o2.get(0);

                            return name1.compareTo(name2);
                        }

                    });
                    Log.d("newlist",list.toString());
                    return;
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
    //Make sure it's connected to the internet
    private boolean isDeviceOnline(){
        ConnectivityManager conMan = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conMan.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
    //Check to make sure google play services are available since Google
    //calendar uses these
    private boolean isGooglePlayServicesAvailable(){
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connecctionStatus = apiAvailability.isGooglePlayServicesAvailable(getContext());
        return connecctionStatus == ConnectionResult.SUCCESS;
    }

    private void acquireGooglePlayServices(){
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        final int connecctionStatus = apiAvailability.isGooglePlayServicesAvailable(getContext());
        if(apiAvailability.isUserResolvableError(connecctionStatus)){
            showGooglePlayServicesAvailabilityErrorDialog(connecctionStatus);
        }
    }
    //show a dialog saying the google play servicecs are out of date or missing
    //the 1002 is the specific error code
    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionCode){
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(getActivity(), connectionCode, REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    public void onSelect(String key, String val) {
        paramKey = key;
        paramValue = val;

        Log.d("SELECT?",paramKey+"/"+paramValue);

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .detach(this)
                .attach(this)
                .commit();

    }

    public void onSelectWords(String[] words) {
        Log.d("sup","pass3");
        HashMap<String, ArrayList<String>> newMap = new HashMap<String, ArrayList<String>>();
        ArrayList<String> newHeaders = new ArrayList<String>();

        //loop through to find keywords in eventsmap
        for(String key : listChildren.keySet()){
            for(String s : words)
                if(listChildren.get(key).get(1).toLowerCase().contains(s.toLowerCase()) || key.toLowerCase().contains(s.toLowerCase())){
                    newMap.put(key,listChildren.get(key));
                    newHeaders.add(key);
                }
        }

        updateList(newHeaders,newMap);
    }


    //Use an asynchronous task to handle Google API call and keep UI responsive
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Capstone")
                    .build();
        }

        /**
         * Background task to call Google Calendar API.
         *
         * @param params no parameters needed for this task.
         */
        @Override
        protected List<String> doInBackground(Void... params) {
            try {
                return getDataFromApi();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }
        /**
         * Fetch a list of the next 10 events from the PLU Events calendar.
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            Calendar pluEvents;
            String id = "";
            Log.d("Bookmark", "Starting getDataFromAPI");
            if(!hasCalendar){
                Log.d("Bookmark", "Creating new Calendar");
                try {
                    Calendar calendar = new Calendar();
                    calendar.setSummary("PLU Events");
                    calendar.setTimeZone("America/Los_Angeles");
                    pluEvents = mService.calendars().insert(calendar).execute();
                    id = pluEvents.getId();
                }catch(Exception e){
                    Log.d("Exception: ", e.toString());
                }
                if(editor != null){
                    editor.putString("CalID", id);
                }
                Log.d("HEYO", id);
            }else{
                Log.d("HEYO", "found calendar");
                id = prefs.getAll().get("CalID").toString();
            }
            Log.d("Bookmark", "Got ID: " +id);
            // List the next 10 events from the PLU Events calendar.
            Log.d("HIHI", savedEvents.size()+"");
            DateTime now = new DateTime(System.currentTimeMillis());
            List<String> eventStrings = new ArrayList<String>();
            Events events = mService.events().list(id)
                    .setMaxResults(30)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();
            Event added;
            String check = "";
            String target = "";
            Log.d("Prefs:", prefs.getAll().toString());
            Log.d("Events", events.size()+"");
            for (Event Event : items) {
                check = Event.getSummary();
                Log.d("HEREEE", check + "||"+target);
                if(prefs != null && editor != null && prefs.contains("Remove")) {
                    target = prefs.getAll().get("Remove").toString();
                    Log.d("REMOVE THIS ONE", check + "||" + target + " || " + target.equals(check));
                    if (target.equals(check)) {
                        Log.d("Attempting to Delete", check);
                        mService.events().delete(id, Event.getId()).execute();
                        editor.remove("Remove");
                        editor.commit();
                    }
                }
                DateTime start = Event.getStart().getDateTime();
                if (start == null) {
                    // All-day events don't have start times, so just use
                    // the start date.
                    start = Event.getStart().getDate();
                }
                eventStrings.add(
                        String.format("%s (%s)", Event.getSummary(), start));
            }
            if(!savedEvents.isEmpty() && savedEvents != null){
                Iterator<Event> listIterator = savedEvents.iterator();
                while(listIterator.hasNext()){
                    Event e = listIterator.next();
                    try{
                        added = mService.events().insert(id, e).execute();
                        Log.d("Added to Cal", added.getHtmlLink());
                    }catch(IOException ioe){
                        Log.d("IOE", ioe.toString());
                    }
                }
                addEventsToCal(savedEvents);
                Log.d("Added", savedEvents.size() +" events");
                savedEvents.clear();
            }
            return eventStrings;
        }


        @Override
        protected void onPreExecute() {
            textView.setText("");
            mProgress.show();
        }

        @Override
        protected void onPostExecute(List<String> output) {
            mProgress.hide();
            if (output == null || output.size() == 0) {
                //textView.setText("No results returned.");
            } else {
                //output.add(0, "Data retrieved using the Google Calendar API:");
                //textView.setText(TextUtils.join("\n", output));
                Toast toast = Toast.makeText(getContext(),
                        "Connected to "+mCredential.getSelectedAccountName(), Toast.LENGTH_SHORT);
                toast.show();
                textView.setText("");
            }
        }

        @Override
        protected void onCancelled() {
            mProgress.hide();
            if (mLastError != null) {
                if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                            ((GooglePlayServicesAvailabilityIOException) mLastError)
                                    .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            REQUEST_AUTHORIZATION);
                } else {
                    textView.setText("The following error occurred:\n"
                            + mLastError.getMessage());
                }
            } else {
                textView.setText("Request cancelled.");
            }
        }

    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {

    }
}
