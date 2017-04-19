package plu.capstone;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
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

        listHeaders = new ArrayList<>();
        listChildren = new HashMap<>();
        eventsMap = new HashMap<>();
        final Context con = this.getContext();

        //gets instance/reference of database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //queries each event
        Query eventQuery = mDatabase.child("Pacific Lutheran University/Events");

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
                expListView = (ExpandableListView) view.findViewById(R.id.lvExpEvents);
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

    public static EventsViewFragment newInstance(String text){
        EventsViewFragment evf = new EventsViewFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);

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
}
