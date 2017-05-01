package plu.capstone.activities;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.goka.blurredgridmenu.GridMenu;
import com.goka.blurredgridmenu.GridMenuFragment;

import java.util.ArrayList;

import plu.capstone.R;
import plu.capstone.util.RSSReader;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private Button mapButton;
    private GridMenuFragment mGridMenuFragment;
    int item;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //grid fragment
        mGridMenuFragment = GridMenuFragment.newInstance(R.drawable.sun_khp);

        setupGridMenu();

        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.schools, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        Button show_menu = (Button)findViewById(R.id.show_menu_button);
        show_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDB();
                FragmentTransaction tx = getSupportFragmentManager().beginTransaction();
                tx.replace(R.id.main_frame, mGridMenuFragment);
                tx.addToBackStack(null);
                tx.commit();
            }
        });
        mGridMenuFragment.setOnClickMenuListener(new GridMenuFragment.OnClickMenuListener(){
            @Override
            public void onClickMenu(GridMenu gridMenu, int position){
                switch(position) {
                    case 0:
                        goToAR();
                        break;
                    case 1:
                        goToMyEvents();
                        break;
                    case 2:
                        goToMap();
                        break;
                    case 3:
                        //TODO: settings?
                        break;
                    case 4:
                        goto25Live();
                        break;
                    case 5:
                        openAbout();
                        break;
                }            }
        });
        /*FloatingActionButton camButton = (FloatingActionButton) findViewById(R.id.camButton);
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
        });*/



    }

    private void setupGridMenu() {
        ArrayList<GridMenu> menus = new ArrayList<>();
        menus.add(new GridMenu("Explore", R.drawable.ic_explore_black_24dp));
        menus.add(new GridMenu("Events", R.drawable.ic_event_available_black_24dp));
        menus.add(new GridMenu("Map", R.drawable.ic_map_black_24dp));
        menus.add(new GridMenu("Settings", R.drawable.ic_settings_black_24dp));
        menus.add(new GridMenu("25Live", R.drawable.ic_event_note_black_24dp));
        menus.add(new GridMenu("About", R.drawable.ic_info_black_24dp));

        mGridMenuFragment.setupMenu(menus);
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
        Intent intent = new Intent(this, InfoViewPager.class);
        startActivity(intent);
    }
    private void goToMap(){
        //Intent intent = new Intent(this, MapsActivity.class);
        //startActivity(intent);
    }
    private void goToAR(){
        Intent intent = new Intent(this, AR.class);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d("School: ", parent.getItemAtPosition(position).toString());

        }

    private void goto25Live() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://r25web.plu.edu/25live/#home_my25live[0]"));
        Toast.makeText(this, "Opening link in Browser...", Toast.LENGTH_SHORT);
        startActivity(browserIntent);
    }
    private void openAbout() {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    //public HashMap getMap(){
    //    return map;
    //}



}
