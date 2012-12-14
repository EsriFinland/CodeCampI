package com.esri.android.offlinemaps;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import com.esri.android.map.LocationService;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISLocalTiledLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;


public class MainActivity extends FragmentActivity implements SensorEventListener  {
	
	private MapView map;
    private SensorManager mSensorManager;
    private Sensor mOrientation;
    private boolean compassEnabled = false;
    private static boolean trackLocation = false;
    private static boolean gpsFixAvailable = false;
    private LocationService ls;
    private ArcGISLocalTiledLayer local2;
    private ArcGISLocalTiledLayer local3;
    private ArcGISLocalTiledLayer local4;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        gpsFixAvailable = false;
        trackLocation = false;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.main);
        map = (MapView) findViewById(R.id.map);

        map.setMaxResolution(500);
        map.setMinResolution(2); 
        
        String path = Environment.getExternalStorageDirectory().getPath() + "/MapTiles";
        
        local4 = new ArcGISLocalTiledLayer("file:///" + path + "/YleiskarttaSuomi.tpk");
        local4.setName("Yleiskartta");
        map.addLayer(local4);
        local4.setVisible(true);  
        
        local2 = new ArcGISLocalTiledLayer("file:///" + path + "/Helsinki_peruskartta_level16.tpk");
        local2.setName("Helsinki peruskartta");
        map.addLayer(local2);
        local2.setVisible(false);

        local3 = new ArcGISLocalTiledLayer("file:///" + path + "/Helsinki_ilmakuva_16_JPG.tpk");
        local3.setName("Helsinki aerial");
        map.addLayer(local3);
        local3.setVisible(false);  
        
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        
        // After map is initialized --> enable GPS.
        map.setOnStatusChangedListener(new OnStatusChangedListener() {
            private static final long serialVersionUID = 1L;

            public void onStatusChanged(Object source, STATUS status) {
                if (source == map && status == STATUS.INITIALIZED) {
                    ls = map.getLocationService();
                    ls.setAutoPan(false);

                    // Center to the current location
                    ls.setLocationListener(new LocationListener() {
                        
                        public void onLocationChanged(Location loc) {
                            gpsFixAvailable = true;

                            if (trackLocation) {
                                double locy = loc.getLatitude();
                                double locx = loc.getLongitude();
                                Point wgspoint = new Point(locx, locy);
                                Point mapPoint = (Point) GeometryEngine.project(wgspoint, 
                                        SpatialReference.create(4326), map.getSpatialReference());

                                map.centerAt(mapPoint, false);
                            }
                        }

                        public void onProviderDisabled(String arg0) {
                            Log.d("OfflineMaps","onProviderDisabled " + arg0);   
                        }

                        public void onProviderEnabled(String arg0) {
                        }

                        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
                        }
                    });
                    ls.start();
                }
            }
        });
    }

    
    public void setLayerVisibility(int layerID, boolean visible) {
        map.getLayer(layerID).setVisible(visible);
    } 
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }   
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
                
        switch (item.getItemId()) {
            case R.id.menuitem_toc:
                LayerTocFragment layerTOC = new LayerTocFragment();
                layerTOC.setLayerList(map.getLayers());
                layerTOC.show(getSupportFragmentManager(), "layertoc");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    /**
     * Enable/disable compass based map rotation.
     * @param view
     */
    public void compass_click(View view) {        
        compassEnabled = !compassEnabled;
        
        if (!compassEnabled) 
            map.setRotationAngle(0);
    }
    
    @Override
    public void onSensorChanged(SensorEvent event) {
        float azimuth_angle = event.values[0];

        if (compassEnabled) {
            map.setRotationAngle(azimuth_angle);
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     * Switch location tracking on and off if gpsFix is available. When enabled
     * map is following gps location.
     * 
     * @param view
     */
    public void trackLocation_onClick(View view) {

        if (gpsFixAvailable) {
            setTrackLocation(!trackLocation);
        } else {
            Toast.makeText(getApplicationContext(), "Odottaa paikannusta...", Toast.LENGTH_SHORT).show();
        }
    }

    private void setTrackLocation(Boolean trackval) {
        ImageButton trackLocBut = (ImageButton) findViewById(R.id.tracklocation_button);

        trackLocation = trackval;

        if (trackLocation) {
            trackLocBut.setImageResource(R.drawable.ic_device_access_location_found);
        } else {
            trackLocBut.setImageResource(R.drawable.ic_device_access_location_searching);
        }
    }
    
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onStop() {
        super.onStop();

        if (ls != null) {
            ls.stop();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        map.unpause();

        if (ls != null) {
            ls.start();
        }

        mSensorManager.registerListener(this, mOrientation, SensorManager.SENSOR_DELAY_UI);
    }
}