package de.tudresden.inf.rn.mobilis.android.buddylist;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import de.tudresden.inf.rn.mobilis.android.ContactsListener;
import de.tudresden.inf.rn.mobilis.android.MainView;
import de.tudresden.inf.rn.mobilis.android.R;
import de.tudresden.inf.rn.mobilis.android.services.BuddyListService;
import de.tudresden.inf.rn.mobilis.android.services.SessionService;

/**
 * ListActivity representing a buddy list showing all online buddies, which are on your 
 * phonebook, roster, or facebook friend list.
 * @author Dirk
 */
public class BuddyList extends ListActivity implements ContactsListener {
	
    
	private BuddyListService buddyListService;
	ArrayAdapter<String> a;
	private CheckListView listView;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		buddyListService = SessionService.getInstance().
		    getSocialNetworkManagementService().getBuddyListService();
		createGUIContents();		
    }
    
    private void createGUIContents() {
        setContentView(R.layout.buddylist);
        listView = (CheckListView) getListView();
        listView.setItemsCanFocus(false);
        listView.setTextFilterEnabled(true);
	}
    
    /**
     * Called when clicked on the buddies tab -> fetch the buddy list from the server.
     */
    @Override
    protected void onResume() {
        super.onResume();
        updateBuddyList();
    }
    
    /**
     * Asks for all buddies at the mobilis server.
     */
    private void updateBuddyList() {
    	buddyListService.fetchContactsFromServer(this);
    }

    /**
     * Callback after a change in the buddylist is present at the client, fetches the updated buddy list,
     * updates the buddylist in the GUI.
     */
	@Override
	public void onContactsUpdated() {
	    MainView.getMainThreadHandler().post(new Runnable() {
	        @Override
	        public void run() {
	            ArrayList<String> buddies = buddyListService.getBuddyList();
	            List<String> marked = buddyListService.getBuddiesOnRosterAndList(buddies);
	            a = new ArrayAdapter<String>(BuddyList.this, R.layout.buddylist_row, R.id.buddylist_buddyname, buddies);
	            listView.setAdapter(a, marked);
	        }
	    });
	}
}