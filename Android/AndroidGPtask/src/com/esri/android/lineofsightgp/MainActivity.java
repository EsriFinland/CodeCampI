package com.esri.android.lineofsightgp;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.geometry.Point;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol.STYLE;
import com.esri.core.tasks.ags.geoprocessing.GPFeatureRecordSetLayer;
import com.esri.core.tasks.ags.geoprocessing.GPJobResource;
import com.esri.core.tasks.ags.geoprocessing.GPJobResource.JobStatus;
import com.esri.core.tasks.ags.geoprocessing.GPMessage;
import com.esri.core.tasks.ags.geoprocessing.GPParameter;
import com.esri.core.tasks.ags.geoprocessing.GPString;
import com.esri.core.tasks.ags.geoprocessing.Geoprocessor;


public class MainActivity extends Activity {
	
    private static final String TAG = "MainActivity";
    
	MapView map;
	GraphicsLayer graphicsLayer;
    Geoprocessor geoprocessor;
    Handler handler;
    Context context;
    
    // Symbols for GP result line.
    private final SimpleLineSymbol SYM_ZONE_BORDER = new SimpleLineSymbol(Color.RED, 3);
    private final SimpleLineSymbol SYM_ZONE_BORDER_2 = new SimpleLineSymbol(Color.BLACK, 3);
	
    // URL to remote geoprocessing service 
    private final String URL_GEOPROCESSING_SERVICE = "http://174.129.236.68/ArcGIS/rest/services/LineOfSight/Async2/GPServer/LineOfSight2";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        context = this;
        
        // Initialize map and set touch listener
        map = (MapView) findViewById(R.id.map);
        map.setOnTouchListener(new MyTouchListener(this, map));
        
        // Add basemap layer
        ArcGISTiledMapServiceLayer basemapStreet = new ArcGISTiledMapServiceLayer("http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer");
        map.addLayer(basemapStreet);        
        
        // Add empty graphics layer for GP result
        graphicsLayer = new GraphicsLayer();
        map.addLayer(graphicsLayer);
        
        // Set button listeners
        Button executeGPButton = (Button) findViewById(R.id.executeGPButton);

        executeGPButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (graphicsLayer.getNumberOfGraphics() == 2) {
                    Toast.makeText(context, "Running GP...", Toast.LENGTH_SHORT).show();
                    executeGP();
                } else {
                    Toast.makeText(context, "Two points needed to run GP model", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Button clearMapButton = (Button) findViewById(R.id.clearMapButton);

        clearMapButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                graphicsLayer.removeAll();
            }
        });   
    }

    
    /*
     * MapView's touch listener
     */
    class MyTouchListener extends MapOnTouchListener {

        public MyTouchListener(Context context, MapView view) {
            super(context, view);
        }

        // Invoked when user single taps on the map view.
        public boolean onSingleTap(MotionEvent e) {
            
            // Our GP model takes two point as a parameter so allow only two point at time.
            if (graphicsLayer.getNumberOfGraphics() > 1)
                graphicsLayer.removeAll(); 

            // Create point to position where user has tap.
            Point point = map.toMapPoint(new Point(e.getX(), e.getY()));
            Graphic graphic = new Graphic(point, new SimpleMarkerSymbol(Color.RED, 25, STYLE.CIRCLE));

            // Add point symbol to map.
            graphicsLayer.addGraphic(graphic);

            return true;
        }
    };
    

    /**
     * Run GP Task.
     */
    private void executeGP() {
       
        // Set GP model address to geoprocessor task.
        geoprocessor = new Geoprocessor(URL_GEOPROCESSING_SERVICE);

        List<GPParameter> gpInputParams = new ArrayList<GPParameter>();

        // Set GP parameters.
        GPString katselukorkeusParam = new GPString("Katselukorkeus");
        katselukorkeusParam.setValue("10");

        GPString kohteenkorkeusParam = new GPString("Kohteen_korkeus");
        kohteenkorkeusParam.setValue("30");
        
        GPFeatureRecordSetLayer katselupisteParam = new GPFeatureRecordSetLayer("Katselupiste");
        katselupisteParam.addGraphic(graphicsLayer.getGraphic(graphicsLayer.getGraphicIDs()[0]));
        
        GPFeatureRecordSetLayer kohdeposteParam = new GPFeatureRecordSetLayer("Kohdepiste");
        kohdeposteParam.addGraphic(graphicsLayer.getGraphic(graphicsLayer.getGraphicIDs()[1]));

        gpInputParams.add(katselukorkeusParam);
        gpInputParams.add(kohteenkorkeusParam);
        gpInputParams.add(katselupisteParam);
        gpInputParams.add(kohdeposteParam);

        handler = new Handler();
        
        // Run GP task
        submitJobAndPolling(geoprocessor, gpInputParams);
    }
    
    /**
     * Execute GP job.
     * @param gp
     * @param params
     */
    void submitJobAndPolling(final Geoprocessor gp, List<GPParameter> params) {
        try {
            GPJobResource gpjr1 = gp.submitJob(params);
            JobStatus jobstatus = gpjr1.getJobStatus();
            final String jobid = gpjr1.getJobID();
            Log.d("Test", "jobid " + jobid);
            Log.d("Test", "jobstatus " + jobstatus);

            if (jobstatus != JobStatus.SUCCEEDED) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            GPJobResource gpjr2 = gp.checkJobStatus(jobid);
                            GPMessage[] messages = gpjr2.getMessages();
                            if (messages != null && messages.length > 0) {
                                for (int i = 0; i < messages.length; i++) {
                                    Log.d("Test", "Message: " + messages[i].getDescription());
                                }
                            }
                            Log.d("Test", "Polling thread is: " + Thread.currentThread().getName());

                            JobStatus status = gpjr2.getJobStatus();
                            boolean jobcomplete = false;

                            if (status == JobStatus.CANCELLED || status == JobStatus.DELETED
                                    || status == JobStatus.FAILED || status == JobStatus.SUCCEEDED
                                    || status == JobStatus.TIME_OUT) {
                                jobcomplete = true;
                            }
                            if (jobcomplete) {
                                if (status == JobStatus.SUCCEEDED) {

                                    GPParameter outputParameter = gp.getResultData(jobid,
                                            "LineOfSightVisibilitySinglep");
                                    processGPResult(outputParameter);

                                    Toast.makeText(getApplicationContext(), "GP succeeded",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "GP failed",
                                            Toast.LENGTH_SHORT).show();
                                    Log.d("Test", "GP failed");
                                }

                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "GP status: " + status.toString(), Toast.LENGTH_SHORT)
                                        .show();
                                handler.postDelayed(this, 5000);
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }, 4000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Process GP result.
     * @param result
     */
    private void processGPResult(GPParameter result) {
        if (result instanceof GPFeatureRecordSetLayer) { 
            GPFeatureRecordSetLayer gpLayer = (GPFeatureRecordSetLayer) result; 

            if (gpLayer.getParamName().equals("LineOfSightVisibilitySinglep")) {
                for (Graphic graphic : gpLayer.getGraphics()) { 
                    
                    Graphic theGraphic = null;
                    
                    if (graphic.getAttributes().get("VisCode").equals(1)) {
                        theGraphic = new Graphic(graphic.getGeometry(), SYM_ZONE_BORDER);
                    } else {
                        theGraphic = new Graphic(graphic.getGeometry(), SYM_ZONE_BORDER_2);
                    }

                    graphicsLayer.addGraphic(theGraphic); 
                } 
            }
        } 
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        map.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        map.unpause();
    }
}