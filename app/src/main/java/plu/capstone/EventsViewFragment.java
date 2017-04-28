package plu.capstone;

import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.GET_ACCOUNTS;
import static android.app.Activity.RESULT_OK;

/**
 * Created by cboe1 on 4/19/2017.
 */

public class EventsViewFragment extends Fragment implements EasyPermissions.PermissionCallbacks{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String PARAM_VALUE = "param1";
    private static final String PARAM_KEY = "param2";

    // TODO: Rename and change types of parameters
    private String paramValue;
    private String paramKey;

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
        //Toolbar myToolbar = (Toolbar) view.findViewById(R.id.myToolbar);
        //((AppCompatActivity)getActivity()).setSupportActionBar(myToolbar);
        //setHasOptionsMenu(true);
        tipsTextView = (TextView)view.findViewById(R.id.eventsTextView);
        textView = (TextView) view.findViewById(R.id.apiTextView);
        prefs = getActivity().getPreferences(0);
        editor = prefs.edit();
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
        tipsTextView.setText("Click an Event's date to add it to your calendar.\nClick an Event's link to open it in a browser.");
        mProgress = new ProgressDialog(getContext());
        mProgress.setMessage("Connecting to Google Calendar...");

        listHeaders = new ArrayList<>();
        listChildren = new HashMap<>();
        eventsMap = new HashMap<>();
        final Context con = this.getContext();
        savedEvents = new ArrayList<Event>();
        expListView = (ExpandableListView) view.findViewById(R.id.lvExpEvents);
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if(childPosition == 0){
                    String date = listChildren.get(listHeaders.get(groupPosition)).get(0);
                    date = date.replaceAll("/", "-");
                    date = date.substring(0, 10);
                    Log.d("Date", date);
                    date = date+"T08:00:00-07:00";
                    DateTime startDateTime = new DateTime(date);
                    date = date.substring(0, 10);
                    date = date+"T09:00:00-07:00";
                    DateTime endDateTime = new DateTime(date);
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
                        getResultsFromApi();
                    }else{
                        Toast toast = Toast.makeText(getContext(), "Event already in Calendar", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                }
                if(childPosition == 2){
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(listChildren.get(listHeaders.get(groupPosition)).get(2)));
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
                    addToEventsMap(title, singleEvent);
                    addToEventChildren(title, singleEvent);
                }
                listAdapter = new ExpandableListAdapter(con, listHeaders, listChildren);
                expListView.setAdapter(listAdapter);
            }

            //error
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ERR", "onCancelled", databaseError.toException());
            }
        });
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            paramValue = getArguments().getString(PARAM_VALUE);
            paramKey = getArguments().getString(PARAM_KEY);
        }
    }

    public static EventsViewFragment newInstance(String text, String k, String val){
        EventsViewFragment evf = new EventsViewFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);
        b.putString(PARAM_VALUE, val);
        b.putString(PARAM_KEY,k);
        evf.setArguments(b);
        return evf;
    }
    /*@Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_events_view, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.connectToGCal:
                getResultsFromApi();
                return true;
            case R.id.search:
                addEventsToCal();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }*/

    public void addToEventsMap(String key, CustomEvent obj){
        eventsMap.put(key, obj);
    }
    public HashMap<String, CustomEvent> getEventsMap(){
        return eventsMap;
    }
    public void addToHeaders(String title){
        listHeaders.add(title);
    }
    public ArrayList<String> getEventHeaders(){
        Log.d("EH SIZE", listHeaders.size() + "");
        return listHeaders;
    }
    public void addToEventChildren(String s, CustomEvent e){
        ArrayList<String> details = new ArrayList<>();
        details.add(e.getCategory());
        details.add(e.getDescription());
        details.add(e.getLink());
        details.add(e.getLoc());
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
    //Use an asynchronous task to handle Google API call and keep UI responsive
    private class MakeRequestTask extends AsyncTask<Void, Void, List<String>> {
        private com.google.api.services.calendar.Calendar mService = null;
        private Exception mLastError = null;

        MakeRequestTask(GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
            mService = new com.google.api.services.calendar.Calendar.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName("Google Calendar API Android Quickstart")
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
         * Fetch a list of the next 10 events from the primary calendar.
         * @return List of Strings describing returned events.
         * @throws IOException
         */
        private List<String> getDataFromApi() throws IOException {
            
            // List the next 10 events from the primary calendar.
            Log.d("HIHI", savedEvents.size()+"");
            DateTime now = new DateTime(System.currentTimeMillis());
            List<String> eventStrings = new ArrayList<String>();
            Events events = mService.events().list("primary")
                    .setMaxResults(50)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute();
            List<Event> items = events.getItems();
            Event added;
            String check = "";
            String target = "";
            Log.d("Prefs:", prefs.getAll().toString());
            for (Event Event : items) {
                /*check = Event.getSummary();
                target = prefs.getString("Remove")
                Log.d("HEREEE", check + " "+prefs.getString("Remove", check));
                if(prefs != null && editor != null) {
                    Log.d("REMOVE THIS ONE", check + "||" + (prefs.getString("Remove", check)).equals(check) + "");
                    if ((prefs.getString("Remove", check)).equals(check)) {
                        Log.d("Attempting to Delete", check);
                        //mService.events().delete("primary", Event.getId()).execute();
                        //editor.remove("Remove");
                        //editor.commit();
                    }
                }*/
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
                        added = mService.events().insert("primary", e).execute();
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
                textView.setText("No results returned.");
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
