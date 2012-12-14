package com.esri.android.lodgigs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISDynamicMapServiceLayer;
import com.esri.android.map.ags.ArcGISFeatureLayer;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.core.tasks.ags.identify.IdentifyParameters;


public class MainActivity extends FragmentActivity {
	
	MapView map;
	  IdentifyParameters params;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        map = (MapView) findViewById(R.id.map);
        
        ArcGISTiledMapServiceLayer basemapStreet = new ArcGISTiledMapServiceLayer("http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer");
        basemapStreet.setName("Base Street");
        basemapStreet.setVisible(true);
        map.addLayer(basemapStreet);
        
        ArcGISTiledMapServiceLayer basemapSatellite = new ArcGISTiledMapServiceLayer("http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer");
        basemapSatellite.setName("Base Satellite");
        basemapSatellite.setVisible(false);
        map.addLayer(basemapSatellite);
        
        ArcGISTiledMapServiceLayer hyvatuloisetTile = new ArcGISTiledMapServiceLayer("http://tiles.arcgis.com/tiles/4PuGhqdWG1FwH2Yk/arcgis/rest/services/Hyv%C3%A4tuloiset/MapServer");
        hyvatuloisetTile.setVisible(false);
        hyvatuloisetTile.setOpacity(0.5f);
        hyvatuloisetTile.setName("Hyvätuloiset");
        map.addLayer(hyvatuloisetTile);

        ArcGISDynamicMapServiceLayer dyn2 = new ArcGISDynamicMapServiceLayer("http://174.129.236.68/ArcGIS/rest/services/Asuinaluetulokset/MapServer", new int[] { 32,33 });
        dyn2.setName("Indeksi (halpa = hyvä)");
        dyn2.setOpacity(0.5f);
        dyn2.setVisible(false);
        map.addLayer(dyn2);
        
        ArcGISDynamicMapServiceLayer dyn3 = new ArcGISDynamicMapServiceLayer("http://174.129.236.68/ArcGIS/rest/services/Asuinaluetulokset/MapServer", new int[] { 32,35 });
        dyn3.setName("Indeksi (kallis = hyvä)");
        dyn3.setOpacity(0.5f);
        dyn3.setVisible(false);
        map.addLayer(dyn3);
        
        ArcGISDynamicMapServiceLayer dyn1 = new ArcGISDynamicMapServiceLayer("http://174.129.236.68/ArcGIS/rest/services/Asuinaluetulokset/MapServer", new int[] { 32,44 });
        dyn1.setName("Etäisyys metrolle/junalle");
        dyn1.setOpacity(0.5f);
        dyn1.setVisible(false);
        map.addLayer(dyn1);        
        
        ArcGISFeatureLayer vuokratFeatureLayer = new ArcGISFeatureLayer(
                "http://services.arcgis.com/4PuGhqdWG1FwH2Yk/ArcGIS/rest/services/CodeCampVuokrat/FeatureServer/0", ArcGISFeatureLayer.MODE.ONDEMAND);
        map.addLayer(vuokratFeatureLayer);

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
    
	@Override 
	protected void onDestroy() { 
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		map.pause();
	}
	
	@Override 	protected void onResume() {
		super.onResume(); 
		map.unpause();
	}	
}