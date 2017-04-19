package plu.capstone;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ExpandableListView;

/**
 * Created by cboe1 on 4/19/2017.
 */

public class CalendarViewFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_calendar_view, container, false);
        CalendarView calendarView = (CalendarView)view.findViewById(R.id.calendarView);

        return view;
    }

    public static CalendarViewFragment newInstance(String text){
        CalendarViewFragment cvf = new CalendarViewFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);

        cvf.setArguments(b);
        return cvf;
    }
}
