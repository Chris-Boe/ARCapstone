package plu.capstone.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import plu.capstone.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class ARDialog extends android.support.v4.app.DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final String msg0 = "Hold your device in an upright position and shift it left and right to explore the campus!";

        final String msg1 = "If the buildings don't seem accurate you might need to recallibrate your gyroscope: Move your device in a figure-8 movement as shown below";

        final String msg2 = "If you want to learn more about a building, click on the button displayed over it! When you're within proximity of a building it will appear on the bottom of your screen.";

        LinearLayout layout = new LinearLayout(getContext());
        layout.setPadding(20,20,20,20);
        layout.setOrientation(LinearLayout.VERTICAL);
        final TextView words = new TextView(getContext());
        words.setText(msg0);
        layout.addView(words);



        Button nextButton = new Button(getContext());
        nextButton.setText("next");
        nextButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(words.getText().equals(msg0)){
                    words.setText(msg1);
                    words.setCompoundDrawablesWithIntrinsicBounds(0,0,0,R.drawable.calibration);

                }
                else
                    words.setText(msg2);
            }
        });

        if(words.getText().equals(msg2))
            layout.removeView(nextButton);

        layout.addView(nextButton);

        builder.setView(layout);

        builder.setTitle("Explore the Campus")
                .setNegativeButton("exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
