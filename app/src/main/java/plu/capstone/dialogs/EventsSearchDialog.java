package plu.capstone.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import plu.capstone.R;

/**
 * Created by playt on 5/1/2017.
 */

public class EventsSearchDialog extends android.support.v4.app.DialogFragment  implements DialogInterface.OnClickListener {

    private EventsSearchListener eListener;

    public EventsSearchDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof EventsSearchListener) {
            eListener = (EventsSearchListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnEventsListener");
        }
    }


/*
   public void onSelect(String key, String val){
        Log.d("events","first pass");
        if (eListener != null) {
            Log.d("events","second pass");
            eListener.onSelect(key,val);
        }
    }*/



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        int id = R.array.plu_building_list;

        final String building[] = getResources().getStringArray(R.array.plu_building_list);

        builder.setTitle("Please choose a building")
                .setItems(building, this);
        builder.setPositiveButton("Show all", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int id) {
                //refreshing
                //onSelect(null,null);
                //paramKey = null;
                //paramValue = null;
                EventsSearchListener activity = (EventsSearchListener) getActivity();
                activity.onSelect(null,null);

                /*getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .detach(fragment)
                        .attach(fragment)
                        .commit();*/

            }
        });

        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });


        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        String building[] = getResources().getStringArray(R.array.plu_building_list);

        for(int i=0;i<building.length;i++){

            //building found
            if(i==which){
                Log.d("building:", building[i]);
                //modify list
                //if (eListener != null) {
                //    Log.d("events","second pass");
                //    eListener.onSelect("loc",building[i]);
                //}

                EventsSearchListener activity = (EventsSearchListener) getActivity();
                activity.onSelect("loc",building[i]);
//                mListener.onSelect("loc", building[i]);
                //paramKey = "loc";
                //paramValue = building[i];

                            /*    getActivity().getSupportFragmentManager()
                                        .beginTransaction()
                                        .detach(fragment)
                                        .attach(fragment)
                                        .commit(); */


            }
        }
    }

    public interface EventsSearchListener {

        public void onSelect(String key, String val);

    }



}
