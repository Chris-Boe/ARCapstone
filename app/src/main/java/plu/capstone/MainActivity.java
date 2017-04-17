package plu.capstone;

import android.app.IntentService;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    private Button mapButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton camButton = (FloatingActionButton) findViewById(R.id.camButton);
        camButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDB();
            }
        });
        Button arButton = (Button)findViewById(R.id.arButton);
        arButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                goToAR();
            }
        });
        Button calButton = (Button) findViewById(R.id.calButton);
        calButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToCal();
            }
        });
        Button myEventsButton = (Button) findViewById(R.id.myEventsButton);
        myEventsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToMyEvents();
            }
        });
        mapButton = (Button) findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // toastLoc();
                //Toast toast = Toast.makeText(getApplicationContext(), "Test", Toast.LENGTH_SHORT);
                //toast.show();
                // goToMap();
            }
        });



    }
    private void updateDB(){
        //Intent intent = new Intent(this, CameraActivity.class);
        //startActivity(intent);
        Toast toast1 = Toast.makeText(getApplicationContext(), "Starting update", Toast.LENGTH_SHORT);
        toast1.show();
        Intent rssIntent = new Intent(this, RSSReader.class);
        rssIntent.putExtra(RSSReader.urlInMessage, "https://25livepub.collegenet.com/calendars/all.rss");
        startService(rssIntent);
        Toast toast2 = Toast.makeText(getApplicationContext(), "Update Complete", Toast.LENGTH_SHORT);
        toast2.show();
    }
    private void goToCal(){
        Intent intent = new Intent(this, CalendarActivity.class);
        startActivity(intent);
    }
    private void goToMyEvents(){
        Intent intent = new Intent(this, MyEvents.class);
        startActivity(intent);
    }
    private void goToMap(){
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
    private void goToAR(){
        Intent intent = new Intent(this, AR.class);
        startActivity(intent);
    }
    //public HashMap getMap(){
    //    return map;
    //}



}
