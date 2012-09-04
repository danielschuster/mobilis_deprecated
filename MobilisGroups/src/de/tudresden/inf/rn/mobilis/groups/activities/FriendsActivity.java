package de.tudresden.inf.rn.mobilis.groups.activities;

import java.util.Date;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import de.tudresden.inf.rn.mobilis.groups.ConstMGroups;
import de.tudresden.inf.rn.mobilis.groups.R;
import de.tudresden.inf.rn.mobilis.mxa.IXMPPService;
import de.tudresden.inf.rn.mobilis.mxa.MXAController;
import de.tudresden.inf.rn.mobilis.mxa.ConstMXA.RosterItems;

/**
 * 
 * @author Robert Lübke
 *
 */
public class FriendsActivity extends ListActivity {

	/** The TAG for the Log. */
	private final static String TAG = "FriendsActivity";
	
	IXMPPService xmppService;
	private FriendsActivity friendsActivity;	

	
	private Cursor mFriendsCursor;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	  
		this.friendsActivity = this;
	  
		xmppService = MXAController.get().getXMPPService();
		
		this.setTitle(R.string.app_name_friendsactivity);
	    
		ListView lv = getListView();
		  
		lv.setTextFilterEnabled(true);
		
		this.updateBuddyList();
	  
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ConstMGroups.REQUEST_CODE_MEMBERINFO) {
            if (resultCode == RESULT_OK) {
                //Back to map
            	Log.i(TAG, "//Back to map");
            	setResult(RESULT_OK);
            	this.finish();
            } else if (resultCode == RESULT_CANCELED) {            	
            	//Refresh
            	updateBuddyList();
            }
        }
    }
	
	
	private void updateBuddyList() {
		// Get a cursor with all roster entries
		mFriendsCursor = getContentResolver().query(RosterItems.CONTENT_URI,
				null, null, null, RosterItems.DEFAULT_SORT_ORDER);
		startManagingCursor(mFriendsCursor);

		ListAdapter adapter = new SimpleCursorAdapter(this,
				// Use a template that displays a text view
				android.R.layout.simple_list_item_1,
				// Give the cursor to the list adapter
				mFriendsCursor,
				// Map the NAME column in the roster database to...
				new String[] { RosterItems.NAME },
				// The "text1" view defined in the XML template
				new int[] { android.R.id.text1 });
		setListAdapter(adapter);	
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu_back_and_refresh, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
    	switch (item.getItemId()) {
    	case R.id.menu_backtomap:
    		this.finish();
    		return true;
        case R.id.menu_refresh:
      	  	this.updateBuddyList();
            return true;        
        default:
        	return super.onOptionsItemSelected(item);
    	}
    } 
    
	
	/**
	 * Creates and shows a dialog with detail information to the selected Buddy. 
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		mFriendsCursor.moveToPosition(position);
		String selectedId = mFriendsCursor.getString(mFriendsCursor
				.getColumnIndex(RosterItems.XMPP_ID));	
		String name = mFriendsCursor.getString(mFriendsCursor
				.getColumnIndex(RosterItems.NAME));
		String presenceMode = mFriendsCursor.getString(mFriendsCursor
				.getColumnIndex(RosterItems.PRESENCE_MODE));
		String presenceStatus = mFriendsCursor.getString(mFriendsCursor
				.getColumnIndex(RosterItems.PRESENCE_STATUS));
		long updatedDate = mFriendsCursor.getLong(mFriendsCursor
				.getColumnIndex(RosterItems.UPDATED_DATE));
		Date date = new Date(updatedDate);
		String dateAsString = date.toLocaleString();
		if (selectedId==null) selectedId="";
		if (name==null) name="";
		if (presenceMode==null) presenceMode="";
		if (presenceStatus==null) presenceStatus="";
		
		AlertDialog.Builder builder;		
		AlertDialog alertDialog;

		Context mContext = getApplicationContext();
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.member_details_dialog,
				(ViewGroup) findViewById(R.id.member_details_layout_root));

		TextView text = (TextView) layout.findViewById(R.id.member_details_text);
		text.setText(Html.fromHtml("<b>XMPP ID (JID):</b> "+selectedId+"<br><br><b>Presence:</b> "+presenceMode+" - "+presenceStatus+"<br><br><b>Last Update:</b> "+dateAsString));		
		ImageView image = (ImageView) layout.findViewById(R.id.member_details_image);
		image.setImageResource(R.drawable.ic_contact_picture);

		final String jid = selectedId;
		
		builder = new AlertDialog.Builder(FriendsActivity.this);
		builder.setView(layout);
		builder.setTitle(name)
			.setCancelable(true)
			.setIcon(R.drawable.ic_menu_contact)
//			.setPositiveButton("Chat", new DialogInterface.OnClickListener() {
//					public void onClick(DialogInterface dialog, int id) {
//						//MyActivity.this.finish();
//					}
//				})
			.setPositiveButton("Details", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					if (jid==null || jid.equals("")) {
						//TODO: put this in strings.xml
						makeToast("XMPP ID (JID) is unknown.");
					} else {
						Intent i = new Intent(getApplicationContext(), MemberInfoActivity.class);
						i.putExtra("jid", jid);   
						startActivityForResult(i, ConstMGroups.REQUEST_CODE_MEMBERINFO);
						//friendsActivity.finish();
					}
				}
			})
			.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});		
		
		alertDialog = builder.create();		
		alertDialog.show();		
	}
	
	/** Shows a short Toast message on the map */
	public void makeToast(String text) {
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
	}
	
	
//	private void getRoster() {
//		XMPPIQ iq = new XMPPIQ();
//		try {
//			iq.from = xmppService.getUsername();
//			iq.to = "xhunt";
//			iq.type = XMPPIQ.TYPE_GET;
//			iq.element = "query";
//			iq.namespace = "jabber:iq:roster";
//			iq.payload = "";			
//			
//		} catch (RemoteException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		//Aim:
////		<iq from='juliet@example.com/balcony' type='get' id='roster_1'>
////		  <query xmlns='jabber:iq:roster'/>
////		</iq>		
//		try {
//			xmppService.sendIQ(new Messenger(ackHandler), new Messenger(rosterResultHandler), 10815, iq);
//		} catch (RemoteException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	
}
