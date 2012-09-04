package de.tudresden.inf.rn.mobilis.groups.activities;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import de.tudresden.inf.rn.mobilis.groups.ConstMGroups;
import de.tudresden.inf.rn.mobilis.groups.Parceller;
import de.tudresden.inf.rn.mobilis.groups.R;
import de.tudresden.inf.rn.mobilis.groups.XMPPManager;
import de.tudresden.inf.rn.mobilis.mxa.ConstMXA;
import de.tudresden.inf.rn.mobilis.mxa.IXMPPService;
import de.tudresden.inf.rn.mobilis.mxa.MXAController;
import de.tudresden.inf.rn.mobilis.mxa.callbacks.IXMPPIQCallback;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPIQ;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupInfoBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupItemInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupJoinBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupMemberInfoBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupQueryBean;

public class MemberInfoActivity extends ListActivity {

	/** The TAG for the Log. */
	private final static String TAG = "MemberInfoActivity";
	
	IXMPPService xmppService;
	private GroupMemberInfoBean groupMemberInfoBean=null;	
	private MemberInfoActivity memberInfoActivity;
	private String textToShowInToast=null;

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.memberInfoActivity=this;
        
        this.sendGroupMemberInfo();
        
        this.setTitle(R.string.app_name_memberinfoactivity);
        setContentView(R.layout.memberinfo);
        
        ListView lv = getListView();        
        lv.setTextFilterEnabled(true);
        TextView headerTextView = new TextView(this);
        headerTextView.setText(Html.fromHtml("<b>Groups:</b>"));
        lv.addHeaderView(headerTextView);    
    }
    
	

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ConstMGroups.REQUEST_CODE_GROUPINFO) {
            if (resultCode == RESULT_OK) {
                //Back to map
            	Log.i(TAG, "//Back to map");
            	setResult(RESULT_OK);
            	this.finish();
            } else if (resultCode == RESULT_CANCELED) {
            	//Refresh
            	this.sendGroupMemberInfo();
            }
        }
    }
	
    private void sendGroupMemberInfo() {
    	Bundle extras = this.getIntent().getExtras();        
        String jid = extras.getString("jid");       
        if (jid!=null) {
        	//Register result callback & send GroupMemberInfoBean with type=GET
        	//to get all information about the member   	
        	xmppService = MXAController.get().getXMPPService(); 
        	if (xmppService!=null) {
	        	try {
					xmppService.registerIQCallback(groupMemberInfoCallback, GroupMemberInfoBean.CHILD_ELEMENT, GroupMemberInfoBean.NAMESPACE);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}  
        	} else {
        		Log.e(TAG, "onCreate() --> xmppService is still null");
        	}
        	XMPPManager.getInstance().sendGroupMemberInfoBeanGet(jid);
        } else {
        	//TODO: Error handling
        }		
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {    	
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu_memberinfo, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
    	switch (item.getItemId()) {
    	case R.id.menu_memberinfo_backtomap:
    		setResult(RESULT_OK);
    		this.finish();
    		return true;
        case R.id.menu_memberinfo_addtoroster:      	  	
        	//TODO: Add Member to own Roster
        	if (groupMemberInfoBean!=null) {
	        	xmppService = MXAController.get().getXMPPService(); 
	        	try {
					xmppService.registerIQCallback(rosterIQCallback, ConstMGroups.XMPP_ADD_ROSTERITEM_IQ_CHILD, ConstMGroups.XMPP_ADD_ROSTERITEM_IQ_NAMESPACE);
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
	        	XMPPManager.getInstance().sendRosterIQ(groupMemberInfoBean.jidWithoutResource,
	        			groupMemberInfoBean.realName,
	        			rosterResultHandler);
        	}
            return true; 
        case R.id.menu_memberinfo_refresh:
    		this.sendGroupMemberInfo();
    		return true;
        default:
        	return super.onOptionsItemSelected(item);
    	}
    }    
    
    private void onGroupMemberInfoBeanArrival() {
    	Log.i(TAG,"onGroupMemberInfoBeanArrival() --> ");
    	
    	
    	
    	TextView tvTitle = (TextView) findViewById(R.id.memberinfo_title);
        TextView tvText = (TextView) findViewById(R.id.memberinfo_text);
        
        String title = groupMemberInfoBean.realName;
        if (title==null || title.equals("")) title =" ";
        tvTitle.setText(title);
         
        String text =       	
	        	"<br><b>XMPP ID (JID):</b> "+groupMemberInfoBean.jidWithoutResource+
	    		"<br><br><b>Age:</b> "+groupMemberInfoBean.age+
	    		"<br><b>Home town:</b> "+groupMemberInfoBean.city+
	    		"<br><br><b>Email:</b> <a href='mailto:"+groupMemberInfoBean.email+"'>"+groupMemberInfoBean.email+"</a>"+
	    		"<br><b>Homepage:</b> <a href='"+groupMemberInfoBean.homepage+"'>"+groupMemberInfoBean.homepage+"</a>";
	    
        tvText.setMovementMethod(LinkMovementMethod.getInstance());
        tvText.setText(Html.fromHtml(text));
        
        final List<String> groupIdList= new ArrayList<String>();
        final List<String> groupNamesList = new ArrayList<String>();
        if (groupMemberInfoBean.groups!=null)
	        for (String groupId : groupMemberInfoBean.groups.keySet()) {
	        	groupIdList.add(groupId);
	        	groupNamesList.add(groupMemberInfoBean.groups.get(groupId));
	        }
        
        setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, groupNamesList));
        
        ListView lv = getListView();
        
        lv.setOnItemClickListener(new OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {       		
        		if (position>0) {
	            	//Clicked on a real item and not on the header            	
	            	AlertDialog.Builder builder;		
	        		AlertDialog alertDialog;
	        		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
	        		View layout = inflater.inflate(R.layout.groups_details_dialog,
	        		                               (ViewGroup) findViewById(R.id.groups_details_layout_root));
	
	        		TextView text = (TextView) layout.findViewById(R.id.groups_details_text);
	        		text.setText(Html.fromHtml("<b>Mobilis Group ID:</b> "+groupIdList.get(position-1)));		
//	        		ImageView image = (ImageView) layout.findViewById(R.id.member_details_image);
//	        		image.setImageResource(R.drawable.ic_contact_picture);
	
	        		//Prepare the Intent
	        		final Intent i = new Intent(getApplicationContext(), GroupInfoActivity.class);
	        		i.putExtra("group_id", groupIdList.get(position-1));  
	        		
	        		builder = new AlertDialog.Builder(MemberInfoActivity.this);
	        		builder.setTitle(groupNamesList.get(position-1))
	        			.setView(layout)
	        			.setCancelable(true)
	        			.setIcon(R.drawable.group_marker_24)
	        			.setPositiveButton("Details", new DialogInterface.OnClickListener() {
	        				public void onClick(DialogInterface dialog, int id) {		  					   	
	        					startActivityForResult(i, ConstMGroups.REQUEST_CODE_GROUPINFO);
	        					//memberInfoActivity.finish();
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
 
    
	/** Callback which is is informed about arrival of a rosterIQ. */
	IXMPPIQCallback rosterIQCallback = new IXMPPIQCallback.Stub() {
		@Override
		public void processIQ(XMPPIQ iq) throws RemoteException {
			Log.i(TAG,"rosterIQCallback --> processIQ --> iqPacketId:"+iq.packetID);				
//			XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
//			Log.i(TAG,"rosterIQCallback --> processIQ --> beanPacketId:"+b.getId());
			xmppService.unregisterIQCallback(rosterIQCallback, ConstMGroups.XMPP_ADD_ROSTERITEM_IQ_CHILD, ConstMGroups.XMPP_ADD_ROSTERITEM_IQ_NAMESPACE);	
			
			if (iq.type == XMPPIQ.TYPE_RESULT) {					
				Log.e(TAG, "GroupMemberInfoBean type=RESULT arrived. IQ-Payload:" + iq.payload);
			} else if (iq.type == XMPPIQ.TYPE_ERROR) {
				//TODO: error handling?
				Log.e(TAG, "GroupMemberInfoBean type=ERROR arrived. IQ-Payload:" + iq.payload);
//				Log.e(TAG, "GroupMemberInfoBean type=ERROR arrived. Error type: " + bb.errorType);
//				Log.e(TAG, "GroupMemberInfoBean type=ERROR arrived. Error condition: " + bb.errorCondition);
//				Log.e(TAG, "GroupMemberInfoBean type=ERROR arrived. Error text: " + bb.errorText);
//				textToShowInToast="ERROR: " + bb.errorText;
//				showToastHandler.sendEmptyMessage(0);								
			}
		}
	};    
    
    
	/** Callback which is is informed about arrival of a GroupInfoIQ. */
	IXMPPIQCallback groupMemberInfoCallback = new IXMPPIQCallback.Stub() {
		@Override
		public void processIQ(XMPPIQ iq) throws RemoteException {
			Log.i(TAG,"groupMemberInfoCallback --> processIQ --> iqPacketId:"+iq.packetID);								
			XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
			Log.i(TAG,"groupMemberInfoCallback --> processIQ --> beanPacketId:"+b.getId());		
			if (b instanceof GroupMemberInfoBean) {
				xmppService.unregisterIQCallback(groupMemberInfoCallback, GroupMemberInfoBean.CHILD_ELEMENT, GroupMemberInfoBean.NAMESPACE);
				GroupMemberInfoBean bb = (GroupMemberInfoBean) b;
				if (b.getType() == XMPPBean.TYPE_RESULT) {					
					groupMemberInfoBean = bb;
					onGroupMemberInfoBeanArrivalHandler.sendEmptyMessage(0);
				} else if (b.getType() == XMPPBean.TYPE_ERROR) {
					//TODO: error handling?
					Log.e(TAG, "GroupMemberInfoBean type=ERROR arrived. IQ-Payload:" + iq.payload);
					Log.e(TAG, "GroupMemberInfoBean type=ERROR arrived. Error type: " + bb.errorType);
					Log.e(TAG, "GroupMemberInfoBean type=ERROR arrived. Error condition: " + bb.errorCondition);
					Log.e(TAG, "GroupMemberInfoBean type=ERROR arrived. Error text: " + bb.errorText);
					textToShowInToast="ERROR: " + bb.errorText;
					showToastHandler.sendEmptyMessage(0);
					setResult(Activity.RESULT_CANCELED);
					memberInfoActivity.finish();
				}				
			}
		}
	};
    
	private Handler onGroupMemberInfoBeanArrivalHandler = new Handler(){
    	@Override
    	public void handleMessage(Message msg) {    		
    		onGroupMemberInfoBeanArrival();
    	}
    };
    
    
	/**  */
    private Handler rosterResultHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Log.i(TAG, "rosterResultHandler -->");
			//arg1: status
			switch (msg.arg1) {
			case ConstMXA.MSG_STATUS_SUCCESS :
				Log.i(TAG, "rosterResultHandler --> success");
				textToShowInToast="Successfully added to roster.";
				showToastHandler.sendEmptyMessage(0);				
				break;			
			case ConstMXA.MSG_STATUS_ERROR :
				Log.i(TAG, "rosterResultHandler --> error");
				textToShowInToast="Error while adding to roster.";
				showToastHandler.sendEmptyMessage(0);
				break;			
//		    case ConstMXA.MSG_STATUS_IQ_RESULT :
//				Log.i(TAG, "rosterResultHandler --> iq_result");				
//				break;			
//			case ConstMXA.MSG_STATUS_IQ_ERROR :
//				Log.i(TAG, "rosterResultHandler --> iq_error");				
//				break;
			default :
				Log.i(TAG, "rosterResultHandler --> ...default");				
				break;
			}
		}			
};
                
	private Handler showToastHandler = new Handler(){
    	@Override
    	public void handleMessage(Message msg) {    		
    		makeToast(textToShowInToast);
    	}
    };   
		
	/** Shows a short Toast message on the map */
	public void makeToast(String text) {
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
	}
	
	
    
	
}
