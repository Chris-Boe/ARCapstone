package plu.capstone;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by cboe1 on 4/19/2017.
 */

public class EventsViewFragment extends Fragment{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String LOCPARAM = "param1";

    // TODO: Rename and change types of parameters
    private String locParam;

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    private ArrayList<String> listHeaders;
    private HashMap<String, ArrayList<String>> listChildren;
    private HashMap<String, Event> eventsMap;
    //private DatabaseEvents dbe;
    private DatabaseReference mDatabase;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_events_view, container, false);
        expListView = (ExpandableListView) view.findViewById(R.id.lvExpEvents);
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                //Toast.makeText(getContext(), listHeaders.get(groupPosition) + ":"
                  //      + listChildren.get(listHeaders.get(groupPosition)).get(childPosition),
                    //    Toast.LENGTH_SHORT).show();
                if(childPosition == 2){
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(listChildren.get(listHeaders.get(groupPosition)).get(2)));
                    Toast.makeText(getContext(), "Opening link in Browser...", Toast.LENGTH_SHORT);
                    startActivity(browserIntent);
                }
                return false;
            }
        });
        listHeaders = new ArrayList<>();
        listChildren = new HashMap<>();
        eventsMap = new HashMap<>();
        final Context con = this.getContext();

        //gets instance/reference of database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        Query eventQuery;
        //queries each event
        if(locParam!=null)
            eventQuery = mDatabase.child("Pacific Lutheran University/Events").orderByChild("loc").equalTo(locParam);
        else
            eventQuery = mDatabase.child("Pacific Lutheran University/Events");

        //listener for onDataChange
        eventQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            //reads in data whenever changed (maybe find a more appropriate callback)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String eList;
                Event singleEvent;
                String title;
                int count = 0;
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    //eventsMap.put(singleSnapshot.getKey(),singleSnapshot.getValue(Event.class));
                    Log.d("Count: ", count+"");
                    count++;
                    title = singleSnapshot.getKey();
                    singleEvent = singleSnapshot.getValue(Event.class);
                    addToEventsMap(title, singleEvent);
                    addToHeaders(title);
                    addToEventChildren(title, singleEvent);
                }
                eList = listHeaders.toString();
                Log.d("Sizes H:", listHeaders.size() + " C: " + listChildren.size());
                //Toast toast = Toast.makeText(con, eList, Toast.LENGTH_SHORT);
                //toast.show();
                Log.d("list", "LH" + eventsMap.size() + " LC: "+listChildren.size());
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
            locParam = getArguments().getString(LOCPARAM);
        }
    }

    public static EventsViewFragment newInstance(String text, String location){
        EventsViewFragment evf = new EventsViewFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);
        b.putString(LOCPARAM, location);
        evf.setArguments(b);
        return evf;
    }
    public void addToEventsMap(String key, Event obj){
        eventsMap.put(key, obj);
    }
    public HashMap<String, Event> getEventsMap(){
        return eventsMap;
    }
    public void addToHeaders(String title){
        listHeaders.add(title);
    }
    public ArrayList<String> getEventHeaders(){
        Log.d("EH SIZE", listHeaders.size() + "");
        return listHeaders;
    }
    public void addToEventChildren(String s, Event e){
        ArrayList<String> details = new ArrayList<>();
        details.add(e.getCategory() + "\nClick to add to calendar!");
        details.add(e.getDescription());
        details.add(e.getLink());
        details.add(e.getLoc());
        listChildren.put(s, details);
    }
    public HashMap<String, ArrayList<String>> getEventChildren(){
        Log.d("EC SIZE", listChildren.size() + "");
        return listChildren;
    }
}
