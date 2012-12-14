package com.esri.android.lodgigs;

import java.util.ArrayList;

import com.esri.android.map.Layer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class LayerTocFragment extends DialogFragment  {
    
    private ArrayList<Integer> mSelectedItems;
    private CharSequence[] _charlayers;
    private boolean[] _checkedLayers;
    public void setLayerList(Layer[] layers) {
        
        CharSequence[] l = new String[layers.length];  
        _checkedLayers = new boolean[layers.length];
        
        for (int i = 0; i < layers.length; i++) {
            l[i] = layers[i].getName();
            
            _checkedLayers[i] = layers[i].isVisible();
        }
        
        _charlayers = l;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mSelectedItems = new ArrayList<Integer>();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Layers")
                .setMultiChoiceItems(_charlayers, _checkedLayers,
                        new DialogInterface.OnMultiChoiceClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked) {
                                    MainActivity main = (MainActivity) getActivity();
                                    main.setLayerVisibility(which, true);
                                } else {
                                    MainActivity main = (MainActivity) getActivity();
                                    main.setLayerVisibility(which, false);
                                }
                            }
                        })
                // Set the action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        return builder.create();
    }
}
