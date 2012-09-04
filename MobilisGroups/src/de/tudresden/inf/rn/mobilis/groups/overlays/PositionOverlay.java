package de.tudresden.inf.rn.mobilis.groups.overlays;

import java.util.ArrayList;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

import de.tudresden.inf.rn.mobilis.groups.R;
import de.tudresden.inf.rn.mobilis.groups.activities.MainActivity;

public class PositionOverlay extends ItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	
	public PositionOverlay(MainActivity mainActivity) {
		super(boundCenter(mainActivity.getResources().getDrawable(R.drawable.gps_position)));
	}
	
	public void addOverlay(OverlayItem overlay) {
		mOverlays.clear();
	    mOverlays.add(overlay);
	    populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

}
