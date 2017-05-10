package plu.capstone.activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;

import plu.capstone.R;
import plu.capstone.dialogs.EventsSearchDialog;
import plu.capstone.dialogs.KeywordSearchDialog;
import plu.capstone.fragments.BuildingsViewFragment;
import plu.capstone.fragments.CalendarViewFragment;
import plu.capstone.fragments.EventsViewFragment;

/**
 * Created by cboe1 on 4/18/2017.
 */

public class InfoViewPager extends AppCompatActivity implements EventsSearchDialog.EventsSearchListener, KeywordSearchDialog.KeywordListenr{

    InfoPagerAdapter infoPagerAdapter;
    ViewPager vP;
    private EventsViewFragment eFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_info_view_pager);
        infoPagerAdapter = new InfoPagerAdapter(getSupportFragmentManager());
        vP = (ViewPager) findViewById(R.id.pager);
        vP.setAdapter(infoPagerAdapter);
    }

    @Override
    public void onSelect(String key, String val) {
        Log.d("pass2",key+"/"+val);
        if(eFragment!=null)
            eFragment.onSelect(key,val);
    }

    @Override
    public void selectWord(String[] words) {
        Log.d("pass2",words.toString());
        if(eFragment!=null)
            eFragment.onSelectWords(words);
    }
    private class InfoPagerAdapter extends FragmentStatePagerAdapter {
        public InfoPagerAdapter(FragmentManager fragMan){
            super(fragMan);

        }
        @Override
        public Fragment getItem(int position) {
            switch(position){
                case 0:
                    eFragment = EventsViewFragment.newInstance("EventsViewFragment",null,null,"event");
                    return  eFragment;
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
