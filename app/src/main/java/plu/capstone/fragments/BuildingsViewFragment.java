package plu.capstone.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import plu.capstone.Models.Buildings;
import plu.capstone.adapters.ExpandableListAdapter;
import plu.capstone.R;

/**
 * Created by cboe1 on 4/19/2017.
 */

public class BuildingsViewFragment extends Fragment {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    private ArrayList<String> listHeaders;
    private HashMap<String, ArrayList<String>> listChildren;
    private HashMap<String, Buildings> buildingsMap;
    private DatabaseReference mDatabase;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        final View view = inflater.inflate(R.layout.fragment_events_view, container, false);
        listHeaders = new ArrayList<>();
        listChildren = new HashMap<>();
        buildingsMap = new HashMap<>();
        final Context con = this.getContext();
        setHasOptionsMenu(false);

        //gets instance/reference of database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //queries each event
        Query buildingQuery = mDatabase.child("Pacific Lutheran University/Buildings");

        //listener for onDataChange
        buildingQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            //reads in data whenever changed (maybe find a more appropriate callback)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Buildings singleBuilding;
                String name;
                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    //buildingsMap.put(singleSnapshot.getKey(),singleSnapshot.getValue(Buildings.class));
                    name = singleSnapshot.getKey();
                    singleBuilding = singleSnapshot.getValue(Buildings.class);
                    addToBuildingsMap(name, singleBuilding);
                    addToHeaders(name);
                    addToBuildingChildren(name, singleBuilding);
                }
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

    public static BuildingsViewFragment newInstance(String text){
        BuildingsViewFragment bvf = new BuildingsViewFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);

        bvf.setArguments(b);
        return bvf;
    }

    public void addToBuildingsMap(String key, Buildings obj){
        buildingsMap.put(key, obj);
    }
    public HashMap<String, Buildings> getEventsMap(){
        return buildingsMap;
    }
    public void addToHeaders(String name){
        listHeaders.add(name);
    }
    public ArrayList<String> getEventHeaders(){
        Log.d("EH SIZE", listHeaders.size() + "");
        return listHeaders;
    }
    public void addToBuildingChildren(String s, Buildings b){
        ArrayList<String> details = new ArrayList<>();
        details.add(b.Description);
        listChildren.put(s, details);
    }
    public HashMap<String, ArrayList<String>> getEventChildren(){
        Log.d("EC SIZE", listChildren.size() + "");
        return listChildren;
    }
}
