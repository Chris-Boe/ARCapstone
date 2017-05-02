package plu.capstone.activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Window;

import com.google.api.services.calendar.model.Event;

import java.util.ArrayList;

import plu.capstone.fragments.CalendarViewFragment;
import plu.capstone.Models.CustomEvent;
import plu.capstone.R;
import plu.capstone.fragments.BuildingsViewFragment;
import plu.capstone.fragments.EventsViewFragment;

/**
 * Created by cboe1 on 4/18/2017.
 */

public class InfoViewPager extends AppCompatActivity {

    InfoPagerAdapter infoPagerAdapter;
    ViewPager vP;
    ArrayList<Event> eventsToCal;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_info_view_pager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        infoPagerAdapter = new InfoPagerAdapter(getSupportFragmentManager());
        vP = (ViewPager) findViewById(R.id.pager);
        vP.setAdapter(infoPagerAdapter);
    }


   /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(0, 0, 0, "History").setIcon(R.drawable.ic_play_dark)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return true;
    }*/

    /*@Override
    public void onEventAdded(Event e) {
        eventsToCal = new ArrayList<Event>();
        eventsToCal.add(e);
        CalendarViewFragment.newInstance("CalendarViewFragment", eventsToCal);
    }*/


    private class InfoPagerAdapter extends FragmentStatePagerAdapter {
        CustomEvent event;
        //private FragmentManager fm;
        public InfoPagerAdapter(FragmentManager fragMan){
            super(fragMan);

        }
        @Override
        public Fragment getItem(int position) {
            //Fragment fragment = fm.findFragmentByTag("android:switcher:" + )
            switch(position){
                case 0: return EventsViewFragment.newInstance("EventsViewFragment",null,null,"event");
                case 1: return CalendarViewFragment.newInstance("CalendarViewFragment");
                case 2: return BuildingsViewFragment.newInstance("BuildingsViewFragment");
                default: return EventsViewFragment.newInstance("Default: EventsViewFragment",null,null,"event");
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position){
            switch(position){
                case 0: return getString(R.string.title_fragment_events_view);
                case 1: return getString(R.string.title_fragment_calendar_view);
                case 2: return getString(R.string.title_fragment_buildings_view);
                default: return null;
            }
        }
    }
}
