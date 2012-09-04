package de.tudresden.inf.rn.mobilis.android;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.location.Location;
import android.net.Uri;
import android.view.View;
import android.widget.Button;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import de.tudresden.inf.rn.mobilis.android.services.SessionService;
import de.tudresden.inf.rn.mobilis.android.util.Const;

public class PresenceOverlay extends Overlay {

	// fields
	private boolean mClickable = true;
	private MapView locationMap;
	private MapController mc;
	/**
	 * HashMap of informal type <jid, MemberData>
	 */
	private HashMap<String, MemberData> memberContainer;
	private List<MemberData> buddyContainer;

	/**
	 * Holds style information.
	 */
	private Paint mPaint;
	private Bitmap markerRed;
	private Bitmap shadowMarker;
	private Bitmap markerPerson;
	private Bitmap markerBuddyProximity;
	private Bitmap markerBuddyOut;
	private Bitmap markerBuddy;
	private LookupBuddyReceiver lookupBuddyReceiver; 
	
	public PresenceOverlay(MapView locationMap, Context c) {
		this.locationMap = locationMap;
		memberContainer = new HashMap<String, MemberData>();
		buddyContainer = new LinkedList<MemberData>();
		lookupBuddyReceiver = new LookupBuddyReceiver(c);

		// initialize fields
		mPaint = new Paint();
		mPaint.setTextSize(16);
		mPaint.setAntiAlias(true);
		markerRed = BitmapFactory.decodeResource(locationMap.getResources(),
				R.drawable.marker_red);
		shadowMarker = BitmapFactory.decodeResource(locationMap.getResources(),
				R.drawable.shadow_marker);
		markerPerson = BitmapFactory.decodeResource(locationMap.getResources(),
				R.drawable.man9);
		markerBuddyProximity = BitmapFactory.decodeResource(locationMap.getResources(),
				R.drawable.flag_red);
		markerBuddyOut = BitmapFactory.decodeResource(locationMap.getResources(),
				R.drawable.flag_gray);
		markerBuddy = BitmapFactory.decodeResource(locationMap.getResources(),
				R.drawable.flag_yellow);
	}

	/**
	 * Defines whether the overlay is clickable or not.
	 * 
	 * @param clickable
	 *            True if clickable, false if not.
	 */
	public void setClickable(boolean clickable) {
		mClickable = clickable;
	}

	@Override
	public void draw(Canvas canvas, MapView mv, boolean flag) {
		super.draw(canvas, mv, flag);

		updateScreenPosition(mv.getProjection());

		drawPresence(canvas);

		// Point p = new Point(51049566, 13737035);
		// int[] screenCoords = new int[2];
		// pixelCalculator.getPointXY(p, screenCoords);
		// Paint paint = new Paint();
		// paint.setARGB(255, 0, 0, 0);
		// paint.setTextSize(20);
		// paint.setAntiAlias(true);
		// canvas.drawText("hier", screenCoords[0], screenCoords[1], paint);
	}

	/**
	 * Draw marker for every online contact.
	 * 
	 * @param canvas
	 */
	private void drawPresence(Canvas canvas) {
		int markerX = 0;
		int markerY = 0;

		for (MemberData currMember : memberContainer.values()) {
			if (currMember.person) {
				// draw person
				markerX = currMember.onScreenPosition.x
						- markerPerson.getWidth() / 2;
				markerY = currMember.onScreenPosition.y
						- markerPerson.getHeight();

				canvas.drawBitmap(markerPerson, markerX, markerY, mPaint);
			} else {
				// draw marker
				markerX = currMember.onScreenPosition.x - markerRed.getWidth()
						/ 2;
				markerY = currMember.onScreenPosition.y - markerRed.getHeight();

				canvas.drawBitmap(shadowMarker, markerX, markerY, mPaint);
				canvas.drawBitmap(markerRed, markerX, markerY, mPaint);
			}
		}
		
		for (MemberData buddy: buddyContainer) {
			Bitmap marker;
			if (buddy.proximity)
				marker = markerBuddyProximity;
			else if (!buddy.proximity && buddy.alert)
				marker = markerBuddyOut;
			else
				marker = markerBuddy;
			markerX = buddy.onScreenPosition.x - marker.getWidth()/2;
			markerY = buddy.onScreenPosition.y - marker.getHeight();
			canvas.drawBitmap(marker, markerX, markerY, mPaint);
		}
	}

	/**
	 * Calculates the current screen position of all group members.
	 * 
	 * @param proj
	 */
	private void updateScreenPosition(Projection proj) {
		for (MemberData currMember : memberContainer.values()) {
			proj.toPixels(new GeoPoint(currMember.latitudeE6,
					currMember.longitudeE6), currMember.onScreenPosition);
		}
		for (MemberData proximityHit: buddyContainer) {
			proj.toPixels(new GeoPoint(proximityHit.latitudeE6,
					proximityHit.longitudeE6), proximityHit.onScreenPosition);
		}
	}

	/**
	 * Transform the groupMembers list to the slighter internal representation.
	 * 
	 * @param groupMembers
	 */
	private void initializeLayout() {
		// for (GroupMember currMember : groupMembers) {
		// if ((currMember.getGeoloc() != null) &&
		// currMember.getGeoloc().hasGeoLocation()) {
		// memberContainer.put(currMember.getJid(), new MemberData(
		// currMember.getJid(), currMember.getGeoloc()
		// .getLocation()));
		// }
		// }
	}

	/**
	 * Implements the parent's onTap method, generates the on screen coordinates
	 * and forwards results to the position comparer.
	 */
	@Override
	public boolean onTap(GeoPoint p, MapView mv) {
		if (mClickable) {
			SessionService serv = SessionService.getInstance();

			Point tapPosition = new Point();
			mv.getProjection().toPixels(p, tapPosition);

			checkTapPosition(tapPosition);
			serv.toString();
			return true;
		}
		return false;
	}

	private void checkTapPosition(Point position) {
		int markerWidth = markerRed.getWidth();
		int markerHeight = markerRed.getHeight();

		for (MemberData currMemb : memberContainer.values()) {
			Rect r = new Rect(currMemb.onScreenPosition.x - (markerWidth / 2),
					currMemb.onScreenPosition.y - markerHeight,
					currMemb.onScreenPosition.x + (markerWidth / 2),
					currMemb.onScreenPosition.y);
			if (r.contains(position.x, position.y)) {
				if (currMemb.person) {
					// person selected
					this.showMemberDialog(currMemb);
				} else {
					// marker selected
					Dialog d = new Dialog(locationMap.getContext());
					d.setTitle(currMemb.jid);
					d.setContentView(R.layout.overlaydialogplace);
					d.show();
				}
			}
		}
		
		markerWidth = markerBuddyProximity.getWidth();
		markerHeight = markerBuddyProximity.getHeight();
		
		for (MemberData buddy: buddyContainer) {
			Rect r = new Rect(buddy.onScreenPosition.x - markerWidth/2,
					buddy.onScreenPosition.y - markerHeight,
					buddy.onScreenPosition.x + markerWidth/2,
					buddy.onScreenPosition.y);
			if (r.contains(position.x, position.y)) {
				this.showMemberDialog(buddy); 
			}
		}
	}

	public void showBuddyDetails(int buddyIndex) {
		this.showMemberDialog(buddyContainer.get(buddyIndex));
	}
	
	private void showMemberDialog(MemberData member) {
		// person selected
		Dialog d = new Dialog(locationMap.getContext());
		d.setTitle(member.jid);
		d.setContentView(R.layout.overlaydialog);
		((Button) d.findViewById(R.id.overlay_btn_call))
				.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent i = new Intent(
								"android.intent.action.CALL",
								Uri.parse("tel:(+49) 172 1234567"));
						PresenceOverlay.this.locationMap
								.getContext().startActivity(i);
					}
				});
		d.show();
	}

	public void updateLocation(String jid, Location geoloc, boolean person) {
		MemberData md = new MemberData(jid, geoloc);
		md.person = person;
		if (jid.equals("me")){
		GeoPoint p = new GeoPoint(
				(int) (geoloc.getLatitude() * 1E6), 
				(int) (geoloc.getLongitude() * 1E6));
		
		mc = locationMap.getController();
		mc.animateTo(p);
		mc.setZoom(16); 
		memberContainer.put(jid, md);
		locationMap.invalidate();
		}
	}
	
	public int addOrUpdateBuddy(String jid, Location geoloc, boolean proximity, boolean alert) {
		MemberData md = new MemberData(jid, geoloc);
		md.proximity = proximity;
		md.alert = alert;
		boolean found = false;
		for (int i = 0; i < buddyContainer.size(); i++) {
			MemberData buddy = buddyContainer.get(i);
			if (buddy.jid.equals(md.jid)) {
				buddyContainer.set(i, md);
				found = true;
			}
		}
		if (!found)
			buddyContainer.add(md);
		locationMap.invalidate();
		return buddyContainer.size()-1;
	}

	private class MemberData {
		public String jid;
		public int altitudeE6;
		public int latitudeE6;
		public int longitudeE6;
		public Point onScreenPosition;
		public boolean person;
		public boolean alert;
		public boolean proximity;

		public MemberData(String jid, Location loc) {
			this.jid = jid;
			altitudeE6 = (int) (loc.getAltitude() * 1E6);
			latitudeE6 = (int) (loc.getLatitude() * 1E6);
			longitudeE6 = (int) (loc.getLongitude() * 1E6);
			onScreenPosition = new Point();
		}		
	}
	
	class LookupBuddyReceiver extends BroadcastReceiver {
		
		public LookupBuddyReceiver(Context c) {
			c.registerReceiver(this, new IntentFilter(Const.INTENT_PREFIX + "lookup_buddy"));
		}
		
		@Override
		public void onReceive(Context context, Intent intent) {
			int proximityHitIndex = intent.getIntExtra(Const.INTENT_PREFIX + "lookup_buddy.handle", -1);
			PresenceOverlay.this.showBuddyDetails(proximityHitIndex);
		}
	}
	
}
