package plu.capstone;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    //private HashMap<String, ArrayList<String>> map;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton camButton = (FloatingActionButton) findViewById(R.id.camButton);
        camButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                goToCamView();
            }
        });
        Button calButton = (Button) findViewById(R.id.calButton);
        calButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                goToCal();
            }
        });
        Button myEventsButton = (Button) findViewById(R.id.myEventsButton);
        myEventsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                goToMyEvents();
            }
        });

        //HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
        //RSSReader reader = new RSSReader("https://25livepub.collegenet.com/calendars/all.rss");
        //map = reader.getMap();
    }
    private void goToCamView(){
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }
    private void goToCal(){
        Intent intent = new Intent(this, CalendarActivity.class);
        startActivity(intent);
    }
    private void goToMyEvents(){
        Intent intent = new Intent(this, MyEvents.class);
        startActivity(intent);
    }
    //public HashMap getMap(){
    //    return map;
    //}



}
