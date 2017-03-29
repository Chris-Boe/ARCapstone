package plu.capstone;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by playt on 3/28/2017.
 */

public class DataBaseBuildings {

    /*
     * This superimposes the json from the database (child buildings) into the program (I think)
     */
    public static class Buildings {


        public String Name;

        public Buildings(){

        }

        public Buildings(String n) {
            Name = n;
        }

    }

    private DatabaseReference mDatabase;

    DataBaseBuildings(Context context){
        //used for toast
        final Context con = context;

        //gets instance/reference of database
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //queries each building (should probably test for more buildings)
        Query testQuery = mDatabase.child("Pacific Lutheran University/Buildings");

        //listener for onDataChange
        testQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            //reads in data whenever changed (maybe find a more appropriate callback)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String bList = "";

                for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    bList += singleSnapshot.getValue(Buildings.class).Name + " ";
                }
                Log.d("TEST", bList);
                Toast toast = Toast.makeText(con.getApplicationContext(), bList, Toast.LENGTH_SHORT);
                toast.show();;
            }

            //error
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("ERR", "onCancelled", databaseError.toException());
            }
        });

    }

}