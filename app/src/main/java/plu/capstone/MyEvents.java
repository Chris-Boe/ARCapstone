package plu.capstone;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
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

public class MyEvents extends AppCompatActivity {
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    private ArrayList<String> listHeaders;
    private HashMap<String, ArrayList<String>> listChildren;
    private HashMap<String, CustomEvent> eventsMap;
    //private DatabaseEvents dbe;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton calButton = (FloatingActionButton) findViewById(R.id.fab);
        calButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToCal();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        listHeaders = new ArrayList<>();
        listChildren = new HashMap<>();
        eventsMap = new HashMap<>();
        final Context con = this.getApplicationContext();

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
                CustomEvent singleCustomEvent;
                String title;
                int count = 0;
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    eventsMap.put(singleSnapshot.getKey(),singleSnapshot.getValue(CustomEvent.class));
                    Log.d("Count: ", count+"");
                    count++;
                    title = singleSnapshot.getKey();
                    singleCustomEvent = singleSnapshot.getValue(CustomEvent.class);
                    addToEventsMap(title, singleCustomEvent);
                    addToHeaders(title);
                    addToEventChildren(title, singleCustomEvent);
                }
                eList = listHeaders.toString();
                Log.d("Sizes H:", listHeaders.size() + " C: " + listChildren.size());
                Toast toast = Toast.makeText(con, eList, Toast.LENGTH_SHORT);
                toast.show();
                Log.d("list", "LH" + eventsMap.size() + " LC: "+listChildren.size());
                expListView = (ExpandableListView) findViewById(R.id.lvExp);
                listAdapter = new ExpandableListAdapter(con, listHeaders, listChildren);
                expListView.setAdapter(listAdapter);

            }

            //error
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ERR", "onCancelled", databaseError.toException());
            }
        });
    }

    private void goToCal(){
        Intent intent = new Intent(this, CalendarActivity.class);
        startActivity(intent);
    }
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
}
