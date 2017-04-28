package plu.capstone;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import com.google.api.services.calendar.model.Event;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cboe1 on 4/18/2017.
 */

public class InfoViewPager extends FragmentActivity {

    InfoPagerAdapter infoPagerAdapter;
    ViewPager vP;
    ArrayList<Event> eventsToCal;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_view_pager);
        infoPagerAdapter = new InfoPagerAdapter(getSupportFragmentManager());
        vP = (ViewPager) findViewById(R.id.pager);
        vP.setAdapter(infoPagerAdapter);
        //Toolbar toolbar = (Toolbar)findViewById(R.id.myToolbar);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(vP);
    }

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
                case 0: return EventsViewFragment.newInstance("EventsViewFragment",null,null);
                case 1: return CalendarViewFragment.newInstance("CalendarViewFragment");
                case 2: return BuildingsViewFragment.newInstance("BuildingsViewFragment");
                default: return EventsViewFragment.newInstance("Default: EventsViewFragment",null,null);
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
