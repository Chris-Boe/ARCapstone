package plu.capstone.fragments;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import plu.capstone.R;
import plu.capstone.adapters.BasicListAdapter;

/**
 * Created by cboe1 on 4/19/2017.
 */

public class CalendarViewFragment extends Fragment {
    private TextView mOutputText;
    static List<String> eventList;
    ListView listView;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    BasicListAdapter adapter;
    Map<String, ?> events;
    CheckBox checkBox;
    CalendarView calendarView;
    private String date;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_calendar_view, container, false);
        calendarView = (CalendarView)view.findViewById(R.id.calendarView);
        date = "2017/5/4";
        checkBox = (CheckBox)view.findViewById(R.id.viewCheckBox);
        prefs = getActivity().getPreferences(0);
        editor = prefs.edit();
        listView = (ListView)view.findViewById(R.id.calListView);
        mOutputText = (TextView)view.findViewById(R.id.calTextView);
        mOutputText.setText("Click an Event to delete it from your calendar");
        getEvents();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                confirm(listView.getItemAtPosition(position).toString());
            }
        });
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getEvents();
            }
        });
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                month += 1;
                String monthString = month+"";
                String dayOfMonthString = dayOfMonth+"";
                if(month<10)
                    monthString = "0"+monthString;
                if(dayOfMonth<10)
                    dayOfMonthString = "0"+dayOfMonthString;
                date = year + "-"+monthString+"-"+dayOfMonthString;
                Toast toast  = Toast.makeText(getContext(), date, Toast.LENGTH_SHORT);
                toast.show();
                getEvents();
            }
        });
        return view;
    }

    private void getEvents() {
        if(eventList != null)
            eventList.clear();
        events = prefs.getAll();
        if(checkBox.isChecked() && events != null){
            for (Map.Entry<String, ?> entry : events.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                Log.d("events", key + " " + value.toString());
                if(key != null && value != null) {
                    if (value.toString().contains(date)){
                        eventList.add(key);
                    }
                }
            }
            if(eventList.contains("accountName")){
                eventList.remove("accountName");
            }if(eventList.contains("Remove")){
                eventList.remove("Remove");
            }if(eventList.contains("CalID"))
                eventList.remove("CalID");
            adapter = new BasicListAdapter(getContext(), eventList);
            listView.setAdapter(adapter);
        }else{
            eventList = new ArrayList<String>(events.keySet());
            if(eventList.contains("accountName")){
                eventList.remove("accountName");
            }if(eventList.contains("Remove")){
                eventList.remove("Remove");
            }if(eventList.contains("CalID"))
                eventList.remove("CalID");
            adapter = new BasicListAdapter(getContext(), eventList);
            listView.setAdapter(adapter);
        }
        Log.d("list", eventList.toString());
        adapter.notifyDataSetChanged();
    }
    private void confirm(final String key){
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        Log.d("Editor", "Before Confirm: "+key+ " " +prefs.contains(key));
                        editor.remove(key);
                        editor.putString("Remove", key);
                        editor.commit();
                        Log.d("Editor", "After Confirm: "+prefs.contains(key));
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
                getEvents();
            }
        };

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Do you want to remove this event from your calendar?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }
    public static CalendarViewFragment newInstance(String text){
        CalendarViewFragment cvf = new CalendarViewFragment();
        Bundle b = new Bundle();
        b.putString("msg", text);
        cvf.setArguments(b);
        return cvf;
    }

}
