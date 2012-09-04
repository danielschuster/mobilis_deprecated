package de.tudresden.inf.rn.mobilis.context;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class LocationActivity extends MapActivity {
	
	private MapView mapView;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.location);
        
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.setSatellite(false);
        
        initComponents();
    }

    private void initComponents() {
		
		Button btn_publish = (Button) findViewById(R.id.location_button_publish);
		btn_publish.setOnClickListener(new OnClickListener() {        	
        	@Override
            public void onClick(View v) {        		        		        		
        		XMPPManager.getInstance().sendUserLocationInfoSet(
        				mapView.getMapCenter().getLongitudeE6()/1E6f,
        				mapView.getMapCenter().getLatitudeE6()/1E6f,
        				String.valueOf(System.currentTimeMillis()/1000));
            }
        });
		
	}
    
	@Override
	protected boolean isRouteDisplayed() {		
		return false;
	}
}