/***
 * Copyright (c) 2010 readyState Software Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package de.tud.android.locpairs;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import de.tud.android.mapbiq.R;

/**
 * A view representing a MapView marker information balloon.
 * <p>
 * This class has a number of Android resource dependencies:
 * <ul>
 * <li>drawable/balloon_overlay_bg_selector.xml</li>
 * <li>drawable/balloon_overlay_close.png</li>
 * <li>drawable/balloon_overlay_focused.9.png</li>
 * <li>drawable/balloon_overlay_unfocused.9.png</li>
 * <li>layout/balloon_map_overlay.xml</li>
 * </ul>
 * </p>
 * 
 * @author Jeff Gilfelt
 *
 */
public class BalloonOverlayView extends FrameLayout {

	/** The Constant OPENABLE. */
	public static final boolean OPENABLE = false;
	
	/** The Constant UNOPENABLE. */
	public static final boolean UNOPENABLE = true;
	
	/** The state. */
	private boolean state;
	
	/** The iv_bmp. */
	private ImageView iv_bmp;
	
	/** The layout. */
	private LinearLayout layout;

	/**
	 * Create a new BalloonOverlayView.
	 * 
	 * @param context - The activity context.
	 * @param balloonBottomOffset - The bottom padding (in pixels) to be applied
	 * when rendering this view.
	 */
	public BalloonOverlayView(Context context, int balloonBottomOffset) {

		super(context);

		setPadding(10, 0, 10, balloonBottomOffset);
		layout = new LinearLayout(context);
		layout.setVisibility(VISIBLE);
		
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.balloon_overlay, layout);
		
		iv_bmp = (ImageView) v.findViewById(R.id.balloon_item_bmp);

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.NO_GRAVITY;

		addView(layout, params);

	}
	
	/**
	 * Sets the view data from a given overlay item.
	 * 
	 * @param item - The overlay item containing the relevant view data 
	 * (title and snippet). 
	 */
	public void setData(CardMarkerOverlay item) {
		layout.setVisibility(VISIBLE);
		if (item.getBitmap() != null) {
			iv_bmp.setVisibility(VISIBLE);
			iv_bmp.setImageBitmap(item.getBitmap());
		} else {
			iv_bmp.setVisibility(GONE);
		}		
	}
	
	/**
	 * Checks if is state.
	 * 
	 * @return true, if is state
	 */
	public boolean isState() {
		return state;
	}

	/**
	 * Sets the state.
	 * 
	 * @param state
	 *            the new state
	 */
	public void setState(boolean state) {
		this.state = state;
	}

}
