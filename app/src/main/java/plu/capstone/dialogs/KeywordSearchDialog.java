package plu.capstone.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

/**
 * Created by playt on 5/1/2017.
 */

public class KeywordSearchDialog extends android.support.v4.app.DialogFragment {

    private KeywordListenr kListener;


    public KeywordSearchDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof KeywordListenr) {
            kListener = (KeywordListenr) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnEventsListener");
        }
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Search by keywords, seperated by spaces");

        final EditText eText = new EditText(getContext());

        builder.setView(eText);

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(eText.getText()!=null){
                    String words = eText.getText().toString();
                    if(kListener!=null)
                        kListener.selectWord(words.split(" "));
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //cancel
            }
        });

        return builder.create();
    }



        public interface KeywordListenr {
        public void selectWord(String[] words);
    }

}
