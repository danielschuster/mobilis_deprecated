package de.tudresden.inf.rn.mobilis.groups.activities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import de.tudresden.inf.rn.mobilis.groups.ApplicationManager;
import de.tudresden.inf.rn.mobilis.groups.ConstMGroups;
import de.tudresden.inf.rn.mobilis.groups.Parceller;
import de.tudresden.inf.rn.mobilis.groups.R;
import de.tudresden.inf.rn.mobilis.groups.XMPPManager;
import de.tudresden.inf.rn.mobilis.mxa.ConstMXA;
import de.tudresden.inf.rn.mobilis.mxa.IXMPPService;
import de.tudresden.inf.rn.mobilis.mxa.MXAController;
import de.tudresden.inf.rn.mobilis.mxa.callbacks.IXMPPIQCallback;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPIQ;
import de.tudresden.inf.rn.mobilis.mxa.services.multiuserchat.IMultiUserChatService;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupInfoBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupItemInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupJoinBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupQueryBean;

public class GroupInfoActivity extends ListActivity {

	/** The TAG for the Log. */
	private final static String TAG = "GroupInfoActivity";
	
	IXMPPService xmppService;
	private GroupInfoBean groupInfoBean=null;	
	private GroupInfoActivity groupInfoActivity;
	private String textToShowInToast=null;
	private Bundle savedInstanceState;
	private Menu optionsMenu;
	private boolean isMember = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.savedInstanceState=savedInstanceState;
        this.groupInfoActivity=this;
        
         sendGroupInfoBeanGet();
        
        this.setTitle(R.string.app_name_groupinfoactivity);
        setContentView(R.layout.groupinfo);
        
        ListView lv = getListView();
        lv.setTextFilterEnabled(true);
        TextView headerTextView = new TextView(this);
        headerTextView.setText(Html.fromHtml("<b>Members:</b>"));
        lv.addHeaderView(headerTextView);    
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
            	this.sendGroupInfoBeanGet();
            }
        }
        if (requestCode == ConstMGroups.REQUEST_CODE_MUC) {
            if (resultCode == RESULT_OK) {
                //Back to map
            	Log.i(TAG, "//Back to map");
            	this.finish();
            } else if (resultCode == RESULT_CANCELED) {
            	//Refresh
            	this.sendGroupInfoBeanGet();
            }
        }
    }
    
    private void sendGroupInfoBeanGet() {    
	    Bundle extras = this.getIntent().getExtras();        
	    String groupId = extras.getString("group_id");       
	    if (groupId!=null) {
	    	//Register result callback & send GroupInfoBean to get all information about the group        	
	    	xmppService = MXAController.get().getXMPPService(); 
	    	if (xmppService!=null) {
	        	try {
					xmppService.registerIQCallback(groupInfoCallback, GroupInfoBean.CHILD_ELEMENT, GroupInfoBean.NAMESPACE);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
	    	} else {
	    		Log.e(TAG, "onCreate() --> xmppService is still null");
	    	}
	    	XMPPManager.getInstance().sendGroupInfoBeanGet(groupId);
	    } else {
	    	Log.e(TAG, "onCreate() --> Could not get groupId from intent extras. Closing GroupInfoActivity now.");
	    	this.finish();
	    }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {            	
    	if (menu!=null) { 
    		menu.clear();
	    	MenuInflater inflater = getMenuInflater();
	    	if (inflater!=null) {	    		
		        inflater.inflate(R.menu.options_menu_groupinfo, menu);
//	    		this.optionsMenu = menu;	
//	    		MenuItem mi = menu.findItem(R.id.menu_groupinfo_join);
//		        mi.setVisible(isMember);
//		        mi = menu.findItem(R.id.menu_groupinfo_invite);
//		        mi.setVisible(!isMember);
		        //MenuItem mi = ((MenuItem) findViewById(R.id.menu_groupinfo_invite));	    			        
		        return true;        
	    	}
	        
    	}
        return false;
    }
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
    	switch (item.getItemId()) {
    	case R.id.menu_groupinfo_backtomap:
    		setResult(RESULT_OK);
    		this.finish();
    		return true;
    	case R.id.menu_groupinfo_chat:
    		
    		Intent i = new Intent(getApplicationContext(), MUCActivity.class);
    		i.putExtra("title", groupInfoBean.name);
    		i.putExtra("group-id", groupInfoBean.groupId);
    		startActivityForResult(i, ConstMGroups.REQUEST_CODE_MUC);
    		return true;
        case R.id.menu_groupinfo_join:
        	//Register result callback & send GroupJoinBean to join the group   	
        	xmppService = MXAController.get().getXMPPService(); 
        	try {
				xmppService.registerIQCallback(groupJoinCallback, GroupJoinBean.CHILD_ELEMENT, GroupJoinBean.NAMESPACE);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			
			Location l = ApplicationManager.getInstance().getLastKnownLocation();
			if (l!=null)			
	        	XMPPManager.getInstance().sendGroupJoinBeanSet(
	        			groupInfoBean.groupId,
	        			(int)(l.getLongitude()*1E6),
	        			(int)(l.getLatitude()*1E6));
			else
				makeToast("Error: Location information could not be retrieved");
            return true;  
//        case R.id.menu_groupinfo_invite:
//        	//TODO: Invite
//    		makeToast("Invite");
//    		return true;
        case R.id.menu_groupinfo_refresh:
    		sendGroupInfoBeanGet();
    		return true;
        default:
        	return super.onOptionsItemSelected(item);
    	}
    }    
    
    private void onGroupInfoBeanArrival() {
    	Log.i(TAG,"onGroupInfoBeanArrival() --> ");    	    	
    	
    	TextView tvTitle = (TextView) findViewById(R.id.groupinfo_title);
        TextView tvText = (TextView) findViewById(R.id.groupinfo_text);
        
        String title = groupInfoBean.name;
        if (title==null || title.equals("")) title =" ";
        tvTitle.setText(title);
         
        String text="";
        if (groupInfoBean.description!=null && !groupInfoBean.description.equals("")) 	
	        	text += "<b>Description:</b> "+groupInfoBean.description;	    
	    if (groupInfoBean.address!=null && !groupInfoBean.address.equals(""))		
	    		text += "<br><b>Address:</b> "+groupInfoBean.address;
	    text += "<br><b>Latitude:</b> "+groupInfoBean.latitude_e6/1E6+
	    		"<br><b>Longitude:</b> "+groupInfoBean.longitude_e6/1E6+
	    		"<br><b>Radii (in m):</b> "+groupInfoBean.visibilityRadius+" (visible), "+groupInfoBean.joinRadius+" (join)";
        if (groupInfoBean.startTime>Long.MIN_VALUE)
	    		text += "<br><b>Start time:</b> "+timestampToDateString(groupInfoBean.startTime); 
        if (groupInfoBean.endTime>Long.MIN_VALUE)
    		text += "<br><b>End time:</b> "+timestampToDateString(groupInfoBean.endTime); 
        if (groupInfoBean.joinStartTime>Long.MIN_VALUE)
    		text += "<br><b>Join start time:</b> "+timestampToDateString(groupInfoBean.joinStartTime); 
        if (groupInfoBean.joinEndTime>Long.MIN_VALUE)
    		text += "<br><b>Join end time:</b> "+timestampToDateString(groupInfoBean.joinEndTime); 
        if (groupInfoBean.link!=null && !groupInfoBean.link.equals(""))		
          	text += "<br><b>Link:</b> <a href=\""+groupInfoBean.link+"\">"+groupInfoBean.link+"</a>";
        
	    text += "<br><b>Founder:</b> "+groupInfoBean.founder+
	    		"<br><b>Privacy:</b> "+groupInfoBean.privacy+
	    		"<br><b>Number of members:</b> "+groupInfoBean.memberCount;
        
        tvText.setMovementMethod(LinkMovementMethod.getInstance());
        tvText.setText(Html.fromHtml(text));
        
        final List<String> memberNamesList= new ArrayList<String>();
        final List<String> memberJidList = new ArrayList<String>();
//        String ownJid="";
//        try {
//			ownJid = xmppService.getUsername();
//		} catch (RemoteException e) {
//			e.printStackTrace();
//		}	
//		isMember=false;
        for (String jid : groupInfoBean.members.keySet()) {
        	memberJidList.add(jid);
        	memberNamesList.add(groupInfoBean.members.get(jid));
//        	if (ownJid.equals(jid)) isMember=true;
        }
//        onCreateOptionsMenu(optionsMenu);
        
        setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, memberNamesList));
        
        ListView lv = getListView();
        
        lv.setOnItemClickListener(new OnItemClickListener() {
          public void onItemClick(AdapterView<?> parent, View view,
              final int position, long id) {
            // When clicked, show a toast with the TextView text        	
            if (position>0) {
            	//Clicked on a real item and not on the header            	
            	AlertDialog.Builder builder;		
        		AlertDialog alertDialog;

        		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        		View layout = inflater.inflate(R.layout.member_details_dialog,
        		                               (ViewGroup) findViewById(R.id.member_details_layout_root));

        		TextView text = (TextView) layout.findViewById(R.id.member_details_text);
        		text.setText(Html.fromHtml("<b>XMPP ID (JID):</b> "+memberJidList.get(position-1)));		
        		ImageView image = (ImageView) layout.findViewById(R.id.member_details_image);
        		image.setImageResource(R.drawable.ic_contact_picture);

        		builder = new AlertDialog.Builder(GroupInfoActivity.this);
        		builder.setView(layout);
        		builder.setTitle(memberNamesList.get(position-1))
        	       .setCancelable(true)
        	       .setIcon(R.drawable.ic_menu_contact)
//        	       .setPositiveButton("Chat", new DialogInterface.OnClickListener() {
//        	           public void onClick(DialogInterface dialog, int id) {
//        	               //TODO 
//        	        	   //this.finish();
//        	           }
//        	       })
        	       .setPositiveButton("Details", new DialogInterface.OnClickListener() {
        	           public void onClick(DialogInterface dialog, int id) {
        	        	   Intent i = new Intent(getApplicationContext(), MemberInfoActivity.class);
        	        	   i.putExtra("jid", memberJidList.get(position-1));   
        	        	   startActivityForResult(i, ConstMGroups.REQUEST_CODE_MEMBERINFO);
        	        	   //groupInfoActivity.finish();
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
        	  
          }
        });
            	
    }
    
    
    /**
     * Converts a timestamp into its Date String representaion.
     * @param timestamp timestamp value to be converted
     * @return the String representation of the timestamp
     * in the following form: dd.mm.yyy
     */
	private String timestampToDateString(long timestamp) {
		Date d = new Date(timestamp);
		return d.getDate() + "." + (d.getMonth()+1) + "." + (d.getYear()+1900);	 
	}


	/** Callback which is is informed about arrival of a GroupInfoIQ. */
	IXMPPIQCallback groupInfoCallback = new IXMPPIQCallback.Stub() {
		@Override
		public void processIQ(XMPPIQ iq) throws RemoteException {
			Log.i(TAG,"groupInfoCallback --> processIQ --> iqPacketId:"+iq.packetID);								
			XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
			Log.i(TAG,"groupInfoCallback --> processIQ --> beanPacketId:"+b.getId());		
			if (b instanceof GroupInfoBean) {
				xmppService.unregisterIQCallback(groupInfoCallback, GroupInfoBean.CHILD_ELEMENT, GroupInfoBean.NAMESPACE);
				if (b.getType() == XMPPBean.TYPE_RESULT) {					
					groupInfoBean = (GroupInfoBean) b;
					onGroupInfoBeanArrivalHandler.sendEmptyMessage(0);
				} else if (b.getType() == XMPPBean.TYPE_ERROR) {
					//TODO: error handling?
					Log.e(TAG, "GroupQueryBean type=ERROR arrived. IQ-Payload:" + iq.payload);
				}
				
			}
		}
	};
    
	private Handler onGroupInfoBeanArrivalHandler = new Handler(){
    	@Override
    	public void handleMessage(Message msg) {    		
    		onGroupInfoBeanArrival();
    	}
    };
    
    
    /** Callback which is is informed about arrival of a GroupJoinIQ. */
	IXMPPIQCallback groupJoinCallback = new IXMPPIQCallback.Stub() {
		@Override
		public void processIQ(XMPPIQ iq) throws RemoteException {
			Log.i(TAG,"groupJoinCallback --> processIQ --> iqPacketId:"+iq.packetID);								
			XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
			Log.i(TAG,"groupJoinCallback --> processIQ --> beanPacketId:"+b.getId());		
			if (b instanceof GroupJoinBean) {
				GroupJoinBean bb = (GroupJoinBean) b;
				xmppService.unregisterIQCallback(groupJoinCallback, GroupJoinBean.CHILD_ELEMENT, GroupJoinBean.NAMESPACE);
				if (bb.getType() == XMPPBean.TYPE_RESULT) {				
					Log.i(TAG,"groupJoinCallback --> Successfully joined!");
					textToShowInToast=groupInfoActivity.getString(R.string.groupinfo_joined);
					showToastHandler.sendEmptyMessage(0);
					groupJoinArrivalHandler.sendEmptyMessage(0);
				} else if (bb.getType() == XMPPBean.TYPE_ERROR) {
					Log.e(TAG, "GroupQueryBean type=ERROR arrived. IQ-Payload:" + iq.payload);
					Log.e(TAG, "GroupQueryBean type=ERROR arrived. Error type: " + bb.errorType);
					Log.e(TAG, "GroupQueryBean type=ERROR arrived. Error condition: " + bb.errorCondition);
					Log.e(TAG, "GroupQueryBean type=ERROR arrived. Error text: " + bb.errorText);
					textToShowInToast=bb.errorText;
					showToastHandler.sendEmptyMessage(0);
				}
						
			}
		}
	};
    
	private Handler groupJoinArrivalHandler = new Handler(){
    	@Override
    	public void handleMessage(Message msg) {    		
    		//TODO
    		//groupInfoActivity.getMenuInflater().inflate(menuRes, menu);
    		groupInfoActivity.sendGroupInfoBeanGet();
    	}
    };
	
	private Handler showToastHandler = new Handler(){
    	@Override
    	public void handleMessage(Message msg) {    		
    		makeToast(textToShowInToast);
    	}
    };
    
    
    /**  */
	private Handler testIQResultHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			switch(msg.arg1) {
			case ConstMXA.MSG_STATUS_ERROR :
				makeToast("Test IQ Result: MSG_STATUS_ERROR");
				break;
			case ConstMXA.MSG_STATUS_SUCCESS :
				makeToast("Test IQ Result: MSG_STATUS_SUCCESS");
				break;
			default :
				makeToast("Test IQ Result!");					
			}		
//			Bundle data = msg.getData();
//			XMPPIQ iq = data.getParcelable("PAYLOAD");
		}
	};

	/**  */
	private Handler ackHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {						
			String toast = "ack: ";
			
			switch(msg.what) {
				case ConstMXA.MSG_SEND_MESSAGE :
					toast+="message ";
					break;
				case ConstMXA.MSG_SEND_IQ :
					toast+="iq ";
					break;
				case ConstMXA.MSG_SEND_PRESENCE :
					toast+="presence ";
					break;
				case ConstMXA.MSG_SEND_FILE :
					toast+="file ";
					break;
			}			
			switch(msg.arg1) {
			case ConstMXA.MSG_STATUS_DELIVERED :
				toast+="delivered";
				break;
			case ConstMXA.MSG_STATUS_ERROR :
				toast+="error";
				break;
			case ConstMXA.MSG_STATUS_REQUEST :
				toast+="request";
				break;
			case ConstMXA.MSG_STATUS_SUCCESS :
				toast+="success";
				break;
			case ConstMXA.MSG_STATUS_IQ_ERROR :
				toast+="iq_error";
				break;
			case ConstMXA.MSG_STATUS_IQ_RESULT :
				toast+="iq_result";
				break;
			}
			
			Log.i(TAG, "Ack received. "+toast);
			makeToast(toast);
			
		}
	};
	
	
	/** Shows a short Toast message on the map */
	public void makeToast(String text) {
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
	}
	
	
    
	
}
