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

/**
 * Created by cboe1 on 4/18/2017.
 */

public class InfoViewPager extends FragmentActivity{

    InfoPagerAdapter infoPagerAdapter;
    ViewPager vP;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_view_pager);
        infoPagerAdapter = new InfoPagerAdapter(getSupportFragmentManager());
        vP = (ViewPager) findViewById(R.id.pager);
        vP.setAdapter(infoPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(vP);
    }


    private class InfoPagerAdapter extends FragmentStatePagerAdapter {
        //private FragmentManager fm;
        public InfoPagerAdapter(FragmentManager fragMan){
            super(fragMan);
            //this.fm = fragMan;
        }
        @Override
        public Fragment getItem(int position) {
            //Fragment fragment = fm.findFragmentByTag("android:switcher:" + )
            switch(position){
                case 0: return EventsViewFragment.newInstance("EventsViewFragment",null);
                case 1: return CalendarViewFragment.newInstance("CalendarViewFragment");
                case 2: return BuildingsViewFragment.newInstance("BuildingsViewFragment");
                default: return EventsViewFragment.newInstance("Default: EventsViewFragment",null);
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
