package plu.capstone.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.goka.blurredgridmenu.GridMenu;
import com.goka.blurredgridmenu.GridMenuFragment;

import java.util.ArrayList;

import plu.capstone.R;
import plu.capstone.util.RSSReader;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    private GridMenuFragment mGridMenuFragment;
    int pic;
    boolean hasQueried = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RelativeLayout rel = (RelativeLayout)findViewById(R.id.mainBackgroundImg);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        int pic = getRandomPic();
        //grid fragment
        rel.setBackgroundResource(pic);
        mGridMenuFragment = GridMenuFragment.newInstance(pic);

        setupGridMenu();

        //TODO: place chosen school into SharedPreferences, scale entire app to reflect choice
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
                //updateDB();
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
    }

    private void setupGridMenu() {
        ArrayList<GridMenu> menus = new ArrayList<>();
        menus.add(new GridMenu("Explore", R.drawable.ic_explore_black_24dp));
        menus.add(new GridMenu("Events", R.drawable.ic_event_available_black_24dp));
        menus.add(new GridMenu("Map", R.drawable.ic_map_black_24dp));
        menus.add(new GridMenu("Settings", R.drawable.ic_settings_black_24dp));
        menus.add(new GridMenu("Create an Event", R.drawable.ic_event_note_black_24dp));
        menus.add(new GridMenu("About", R.drawable.ic_info_black_24dp));

        mGridMenuFragment.setupMenu(menus);
    }
    private int getRandomPic(){
        //randomize homepage background image
        int num = (int)(Math.random()*3);
        Log.d("Rand", num+"");
        switch(num){
            case 0:
                pic = R.drawable.sun_khp;
                break;
            case 1:
                pic = R.drawable.morken_pic;
                break;
            case 2:
                pic = R.drawable.clocktower;
                break;
            default:
                pic = R.drawable.sun_khp;
        }
        Log.d("Rand", pic+"");
        return pic;
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
        hasQueried = true;
    }
    private void goToMyEvents(){
        Intent intent = new Intent(this, InfoViewPager.class);
        startActivity(intent);
    }
    private void goToMap(){
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
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
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    //public HashMap getMap(){
    //    return map;
    //}



}
