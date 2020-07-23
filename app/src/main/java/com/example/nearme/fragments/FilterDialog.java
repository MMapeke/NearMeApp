package com.example.nearme.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.example.nearme.R;

public class FilterDialog extends AppCompatDialogFragment {

    public static final String TAG = "FilterDialog";
    private SeekBar barTimeWithin;
    private FilterDialogListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog,null);

        builder.setView(view)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //grab the value and send to to the listener method
                        int time = barTimeWithin.getProgress();
                        Log.i(TAG,"timeWithin Value: " + time);

                        listener.applyFilter(time);
                    }
                });

        barTimeWithin = view.findViewById(R.id.bar_TimeWithin);
        barTimeWithin.setMax(24);

        return builder.create();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof FilterDialog.FilterDialogListener) {
            listener = (FilterDialogListener) context;
        }
    }

    public interface FilterDialogListener{
        void applyFilter(int hours);
    }
}
