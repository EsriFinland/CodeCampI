package com.esri.android.offlinemaps;

import java.util.ArrayList;

import com.esri.android.map.Layer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LayerTocFragment extends DialogFragment  {
    
    private ArrayList<Integer> mSelectedItems;
    private CharSequence[] _charlayers;
    private boolean[] _checkedLayers;
    private Layer[] _layers;

    
    public void setLayerList(Layer[] layers) {
        
        this._layers = layers;
        CharSequence[] l = new String[layers.length];  
        _checkedLayers = new boolean[layers.length];
        
        for (int i = 0; i < layers.length; i++) {
            l[i] = layers[i].getName();
            
            _checkedLayers[i] = layers[i].isVisible();
        }
        
        _charlayers = l;
    }
    
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
////        LinearLayout layout =(LinearLayout)inflater.inflate(R.layout.custom_dialog, null);
////        LinearLayout item = (LinearLayout)layout.findViewById(R.id.display_item);
////        populateItemData(item, inflater);
//        return layout;
//    }
//    
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mSelectedItems = new ArrayList<Integer>(); // Where we track the selected

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Layers")
                .setMultiChoiceItems(_charlayers, _checkedLayers,
                        new DialogInterface.OnMultiChoiceClickListener() {
                    
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked) {
                                    MainActivity main =  (MainActivity)getActivity();
                                    main.setLayerVisibility(which, true);
                                    //_layers[which].setVisible(true);
                                } else {
                                    MainActivity main =  (MainActivity)getActivity();
                                    main.setLayerVisibility(which, false);
                                    //_layers[which].setVisible(false);
                                }
                            }
                        })
                // Set the action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        return builder.create();
    }
}
