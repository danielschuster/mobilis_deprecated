/*******************************************************************************
 * Copyright (C) 2010 Technische Universität Dresden
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 ******************************************************************************/
package de.tudresden.inf.rn.mobilis.groups;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import de.tudresden.inf.rn.mobilis.groups.activities.MainActivity;
import de.tudresden.inf.rn.mobilis.mxa.ConstMXA;
import de.tudresden.inf.rn.mobilis.mxa.IXMPPService;
import de.tudresden.inf.rn.mobilis.mxa.MXAController;
import de.tudresden.inf.rn.mobilis.mxa.MXAListener;
import de.tudresden.inf.rn.mobilis.mxa.callbacks.IXMPPIQCallback;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPIQ;
import de.tudresden.inf.rn.mobilis.mxa.services.parcelable.DiscoverItem;
import de.tudresden.inf.rn.mobilis.xmpp.beans.ConditionInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupCreateBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupDeleteBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupInfoBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupInviteBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupItemInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupJoinBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupLeaveBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupMemberInfoBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupQueryBean;

/**
 * 
 * @author Robert Lübke
 *
 */
public class XMPPManager extends Service {
	
	/** The TAG for the Log. */
	private final static String TAG = "XMPPManager";

	private static XMPPManager xmppManager;
	
	private MXAController mxaController;
	private IXMPPService xmppService;
	private MainActivity mainActivity;
	
	private String mobilisServerJid, groupingService;
	
	
	public static XMPPManager getInstance() {
		if(xmppManager==null) {
			xmppManager = new XMPPManager(); 
		}
		return xmppManager;
	}
	
	private XMPPManager() {		
		mxaController = MXAController.get();		
		mainActivity = ApplicationManager.getInstance().getMainActivity();	
		
		//Register bean prototypes to the Parceller
		GroupMemberInfoBean beanPrototype1 = new GroupMemberInfoBean();
		GroupCreateBean beanPrototype2 = new GroupCreateBean();
		GroupInfoBean beanPrototype3 = new GroupInfoBean();
		GroupDeleteBean beanPrototype5 = new GroupDeleteBean();
		GroupJoinBean beanPrototype6 = new GroupJoinBean();
		GroupLeaveBean beanPrototype7 = new GroupLeaveBean();	
		GroupQueryBean beanPrototype8 = new GroupQueryBean();
		GroupInviteBean beanPrototype9 = new GroupInviteBean();
        Parceller.getInstance().registerXMPPBean(beanPrototype1);
        Parceller.getInstance().registerXMPPBean(beanPrototype2);
        Parceller.getInstance().registerXMPPBean(beanPrototype3);
        Parceller.getInstance().registerXMPPBean(beanPrototype5);
        Parceller.getInstance().registerXMPPBean(beanPrototype6);
        Parceller.getInstance().registerXMPPBean(beanPrototype7);
        Parceller.getInstance().registerXMPPBean(beanPrototype8);
        Parceller.getInstance().registerXMPPBean(beanPrototype9);
        
	}
	
	
	public void connectToMXA() {		
        mxaController.connectMXA(mainActivity.getApplicationContext(), mxaConnectionListener);        
	}
		
	public void connectToXMPPServer() {
		try {
			xmppService = mxaController.getXMPPService();
			Messenger m = new Messenger(xmppConnectionResultHandler);
			xmppService.connect(m);
			
			//Register IQ Callbacks:
			xmppService.registerIQCallback(groupMemberInfoCallback, GroupMemberInfoBean.CHILD_ELEMENT, GroupMemberInfoBean.NAMESPACE);
			xmppService.registerIQCallback(groupQueryCallback, GroupQueryBean.CHILD_ELEMENT, GroupQueryBean.NAMESPACE);
			//mainActivity.makeToast("IQ Callbacks registered.");	
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendServiceDiscoveryIQ() {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mainActivity.getApplicationContext());
		mobilisServerJid = pref.getString(mainActivity.getString(R.string.pref_mobilisserver_key), "");
		groupingService = null;
		if (xmppService!=null) {
			try {
				//TODO: requestcode
				
				this.xmppService.getServiceDiscoveryService().discoverItem(
						new Messenger(serviceDiscoveryHandler),
						new Messenger(serviceDiscoveryHandler),
						1,
						mobilisServerJid,
						null);
				Log.i(TAG, "sendServiceDiscoveryIQ --> Discovery-IQ sent to: "+mobilisServerJid);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		} else {
			Log.e(TAG, "sendServiceDiscoveryIQ --> xmppService is still null");
		}
	}
	
	public void sendServiceDiscoveryIQ(String broker) {		
		try {
			//TODO: requestcode			
			this.xmppService.getServiceDiscoveryService().discoverItem(
					new Messenger(serviceDiscoveryHandler),
					new Messenger(serviceDiscoveryHandler),
					1,
					broker,
					ConstMGroups.NAMESPACE_SERVICES);
			Log.i(TAG, "sendServiceDiscoveryIQ(broker) --> Discovery-IQ sent to: "+broker);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	/** Send a roster IQ to add a friend (jid/name) to your own roster. */
	public void sendRosterIQ(String jid, String name, Handler rosterResultHandler) {		
		try {			
			if (jid!=null && !jid.equals("") && name!=null) {
				XMPPIQ iq = new XMPPIQ();
				iq.from = xmppService.getUsername();			
				iq.type=XMPPIQ.TYPE_SET;
				iq.namespace = ConstMGroups.XMPP_ADD_ROSTERITEM_IQ_NAMESPACE;
				iq.element = ConstMGroups.XMPP_ADD_ROSTERITEM_IQ_CHILD;
				iq.payload = "<item jid='"+jid+"' name='"+name+"'></item>";
				//TODO: requestcode
				this.xmppService.sendIQ(new Messenger(ackHandler), new Messenger(rosterResultHandler), 1, iq);
				Log.i(TAG, "sendRosterIQ --> IQ sent.");			
				
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	
	
	public void sendGroupQueryBeanGet(MapView mapView) {
		
		GeoPoint center = mapView.getMapCenter();
		int widthE6  = mapView.getLongitudeSpan();
		int heightE6 = mapView.getLatitudeSpan();
		String latStartE6 = String.valueOf(center.getLatitudeE6()  - widthE6/2);
		String lonStartE6 = String.valueOf(center.getLongitudeE6() - heightE6/2);
		String latEndE6   = String.valueOf(center.getLatitudeE6()  + widthE6/2);
		String lonEndE6   = String.valueOf(center.getLongitudeE6() + heightE6/2);
				
		Location l = ApplicationManager.getInstance().getLastKnownLocation();
	
		GroupQueryBean beanToSend;
		if (l!=null)
			beanToSend = new GroupQueryBean((int)(l.getLongitude()*1E6), (int)(l.getLatitude()*1E6));
		else
			beanToSend = new GroupQueryBean();
		
		ConditionInfo condition = new ConditionInfo(ConditionInfo.OP_AND);
		condition.getConditions().clear();		
		condition.getConditions().add(
				new ConditionInfo("latitude_e6", ConditionInfo.OP_GE ,latStartE6));
		condition.getConditions().add(
				new ConditionInfo("latitude_e6", ConditionInfo.OP_LE ,latEndE6));
		condition.getConditions().add(
				new ConditionInfo("longitude_e6", ConditionInfo.OP_GE ,lonStartE6));
		condition.getConditions().add(
				new ConditionInfo("longitude_e6", ConditionInfo.OP_LE ,lonEndE6));						
		beanToSend.setCondition(condition);
			
		if (groupingService!=null) {
			try {
				beanToSend.setType(XMPPBean.TYPE_GET);
				beanToSend.setFrom(xmppService.getUsername());
				beanToSend.setTo(groupingService);						
				xmppService.sendIQ(new Messenger(ackHandler), null, 1, Parceller.getInstance().convertXMPPBeanToIQ(beanToSend, true));
				Log.i(TAG, "GroupQueryBean with type=GET was sent. packetID:"+beanToSend.getId()+" from:"+beanToSend.getFrom()+" to:"+beanToSend.getTo());		
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Log.e(TAG, "sendGroupQueryBeanGet --> groupingService is still null. Sending Discovery-IQ now.");
			mainActivity.makeToast("No GroupingService found on MobilisServer "+mobilisServerJid);
			sendServiceDiscoveryIQ();
		}
	}
	
	
	/**
	 * Sends a groupInfoBean with type=GET to get all Information about a MobilisGroup.
	 * @param groupId The ID of the group
	 */
	public void sendGroupInfoBeanGet(String groupId) {					
		GroupInfoBean beanToSend = new GroupInfoBean(groupId);			
		if (groupingService!=null) {
			try {
				beanToSend.setType(XMPPBean.TYPE_GET);
				beanToSend.setFrom(xmppService.getUsername());				
				beanToSend.setTo(groupingService);						
				xmppService.sendIQ(new Messenger(ackHandler), null, 1, Parceller.getInstance().convertXMPPBeanToIQ(beanToSend, true));
				Log.i(TAG, "GroupInfoBean with type=GET was sent. packetID:"+beanToSend.getId()+" from:"+beanToSend.getFrom()+" to:"+beanToSend.getTo());		
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Log.e(TAG, "sendGroupInfoBeanGet --> groupingService is still null. Sending Discovery-IQ now.");
			sendServiceDiscoveryIQ();
		}
	}
		
	/**
	 * Sends a groupInfoBean with type=GET to get all Information about a MobilisGroup.
	 * @param groupId The ID of the group
	 */
	public void sendGroupDeleteBeanSet(String groupId) {					
		GroupDeleteBean beanToSend = new GroupDeleteBean(groupId);			
		if (groupingService!=null) {
			try {
				beanToSend.setType(XMPPBean.TYPE_SET);
				beanToSend.setFrom(xmppService.getUsername());				
				beanToSend.setTo(groupingService);						
				xmppService.sendIQ(new Messenger(ackHandler), null, 1, Parceller.getInstance().convertXMPPBeanToIQ(beanToSend, true));
				Log.i(TAG, "GroupDeleteBean with type=SET was sent. packetID:"+beanToSend.getId()+" from:"+beanToSend.getFrom()+" to:"+beanToSend.getTo());		
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Log.e(TAG, "sendGroupDeleteBeanSet --> groupingService is still null. Sending Discovery-IQ now.");
			sendServiceDiscoveryIQ();
		}
	}
	
	
	/**
	 * Sends a groupInfoBean with type=GET to get all Information about a MobilisGroup.
	 * @param groupId The ID of the group
	 */
	public void sendGroupLeaveBeanSet(String groupId) {					
		GroupLeaveBean beanToSend = new GroupLeaveBean(groupId);			
		if (groupingService!=null) {
			try {
				beanToSend.setType(XMPPBean.TYPE_SET);
				beanToSend.setFrom(xmppService.getUsername());				
				beanToSend.setTo(groupingService);
				xmppService.sendIQ(new Messenger(ackHandler), null, 1, Parceller.getInstance().convertXMPPBeanToIQ(beanToSend, true));
				Log.i(TAG, "GroupLeaveBean with type=SET was sent. packetID:"+beanToSend.getId()+" from:"+beanToSend.getFrom()+" to:"+beanToSend.getTo());		
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Log.e(TAG, "sendGroupLeaveBeanGet --> groupingService is still null. Sending Discovery-IQ now.");
			sendServiceDiscoveryIQ();
		}
	}
	
	
	/**
	 * Sends new GroupMemberInfoBean with type=GET to get all
	 * information about a MobilisMember.
	 * @param jid XMPP ID (JID) of the MobilisMember
	 */
	public void sendGroupMemberInfoBeanGet(String jid) {					
		GroupMemberInfoBean beanToSend = new GroupMemberInfoBean(jid);			
		if (groupingService!=null) {
			try {
				beanToSend.setType(XMPPBean.TYPE_GET);
				beanToSend.setFrom(xmppService.getUsername());				
				beanToSend.setTo(groupingService);						
				xmppService.sendIQ(new Messenger(ackHandler), null, 1, Parceller.getInstance().convertXMPPBeanToIQ(beanToSend, true));
				Log.i(TAG, "GroupMemberInfoBean with type=GET was sent. packetID:"+beanToSend.getId()+" from:"+beanToSend.getFrom()+" to:"+beanToSend.getTo());		
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Log.e(TAG, "sendGroupMemberInfoBeanSet --> groupingService is still null. Sending Discovery-IQ now.");
			sendServiceDiscoveryIQ();
		}
	}
	
	public void sendGroupJoinBeanSet(String groupId, int userLongitude, int userLatitude) {
		
		GroupJoinBean beanToSend = new GroupJoinBean(groupId, userLongitude, userLatitude);      
				
		if (groupingService!=null) {
			try {
				beanToSend.setType(XMPPBean.TYPE_SET);
				beanToSend.setFrom(xmppService.getUsername());
				
				beanToSend.setTo(groupingService);						
				xmppService.sendIQ(new Messenger(ackHandler), null, 1, Parceller.getInstance().convertXMPPBeanToIQ(beanToSend, true));
				Log.i(TAG, "GroupJoinBean with type=SET was sent. packetID:"+beanToSend.getId()+" from:"+beanToSend.getFrom()+" to:"+beanToSend.getTo());		
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Log.e(TAG, "sendGroupMemberJoinBeanSet --> groupingService is still null. Sending Discovery-IQ now.");
			sendServiceDiscoveryIQ();
		}
	}
	
	
	/**
	 * Sends new GroupMemberInfoBean with data from current preferences
	 * @param packetId 
	 */
	public void sendGroupMemberInfoBeanSet(String packetId) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mainActivity.getApplicationContext());
		int age=Integer.MIN_VALUE;
		try {
			age = Integer.valueOf(pref.getString(mainActivity.getString(R.string.pref_personal_information_age_key), ""));
		} catch(NumberFormatException ex) {
			Log.e(TAG, "NumberFormatException while parsing age - set to default value.");
			age=Integer.MIN_VALUE;
		}
			
		GroupMemberInfoBean beanToSend = new GroupMemberInfoBean(
				pref.getString(mainActivity.getString(R.string.pref_personal_information_realname_key), ""),
				pref.getString(mainActivity.getString(R.string.pref_personal_information_city_key), ""),
				pref.getString(mainActivity.getString(R.string.pref_personal_information_email_key), ""),
				pref.getString(mainActivity.getString(R.string.pref_personal_information_homepage_key), ""),
				age);
		beanToSend.setType(XMPPBean.TYPE_SET);
		if (packetId!=null) beanToSend.setId(packetId);
		
		if (groupingService!=null) {
			try {
				beanToSend.setFrom(xmppService.getUsername());
				beanToSend.setTo(groupingService);				
				xmppService.sendIQ(new Messenger(ackHandler), null, 1, Parceller.getInstance().convertXMPPBeanToIQ(beanToSend, true));
				Log.i(TAG, "GroupMemberInfoBean with type=SET was sent. packetID:"+beanToSend.getId()+" from:"+beanToSend.getFrom()+" to:"+beanToSend.getTo());
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		} else {
			Log.e(TAG, "sendGroupMemberInfoBeanSet --> groupingService is still null. Sending Discovery-IQ now.");
			sendServiceDiscoveryIQ();
		}
	}	
	
	
	/**
	 * Sends new GroupInviteBean
	 * @param invitees Set of JID's of the friends to invite
	 * @param groupId the group which the friends are invited to
	 */
	public void sendGroupInviteBeanSet(Set<String> invitees, String groupId) {					
		GroupInviteBean beanToSend = new GroupInviteBean(invitees, groupId);
		beanToSend.setType(XMPPBean.TYPE_SET);		
		if (groupingService!=null) {
			try {
				beanToSend.setFrom(xmppService.getUsername());
				beanToSend.setTo(groupingService);	
				xmppService.sendIQ(new Messenger(ackHandler), null, 1, Parceller.getInstance().convertXMPPBeanToIQ(beanToSend, true));
				Log.i(TAG, "GroupInviteBean with type=SET was sent. packetID:"+beanToSend.getId()+" from:"+beanToSend.getFrom()+" to:"+beanToSend.getTo());
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		} else {
			Log.e(TAG, "sendGroupInviteBeanSet --> groupingService is still null. Sending Discovery-IQ now.");
			sendServiceDiscoveryIQ();
		}
	}	

	/********************** Listener & Handler for XMPP **********************/
	
			
	/** Callback which is is informed about arrival of a GroupQuery-IQ. */
	IXMPPIQCallback groupQueryCallback = new IXMPPIQCallback.Stub() {
		@Override
		public void processIQ(XMPPIQ iq) throws RemoteException {
							
			XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
			
			//TODO: Demonstration of Exception problem:
			//Exceptions in IQCallbacks are not noticed in the application or in LogCat. 
//			Log.i(TAG, "vorher");
//			String x=null;
//			x.equals("bla");								
//			Log.i(TAG, "nachher");
			
			if (b instanceof GroupQueryBean) {				
				if (b.getType() == XMPPBean.TYPE_RESULT) {					
					GroupQueryBean bb = (GroupQueryBean) b;
					ArrayList<GroupItemInfo> list = bb.getItems();
					mainActivity.setGroupItemsToShowOnMap(list);
				} else if (b.getType() == XMPPBean.TYPE_ERROR) {
					//TODO: error handling?
					Log.e(TAG, "GroupQueryBean type=ERROR arrived. IQ-Payload:" + iq.payload);
				}
						
			}
		}
		
	};

	
	/** Callback which is is informed about arrival of a GroupMemberInfoIQ. */
	IXMPPIQCallback groupMemberInfoCallback = new IXMPPIQCallback.Stub() {
		@Override
		public void processIQ(XMPPIQ iq) throws RemoteException {
			XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
//			Log.i(TAG, "groupMemberInfoCallback --> processBean: "+b.toString());
			if (b instanceof GroupMemberInfoBean) {				
				if (b.getType() == XMPPBean.TYPE_GET) {
//					Log.i(TAG, "GroupMemberInfoBean type=GET arrived.");
					//Mobilis server wants to get a GroupMemberInfoIQ with personal information
					sendGroupMemberInfoBeanSet(iq.packetID);
					
				} else if (b.getType() == XMPPBean.TYPE_RESULT) {
					//ResultIQ - do nothing
//					Log.i(TAG, "GroupMemberInfoBean type=RESULT arrived.");
				} else if (b.getType() == XMPPBean.TYPE_ERROR) {
					//ErrorIQ - do nothing
					Log.e(TAG, "GroupMemberInfoBean type=ERROR arrived. IQ-Payload:" + iq.payload);	
				}
			}				
		}
	};

	
	/** Listener which is informed about connection / disconntection to MXA (NOT to XMPP server!) */
	private MXAListener mxaConnectionListener = new MXAListener() {			
		@Override
		public void onMXADisconnected() {
			groupingService = null;
			mainActivity.makeToast("Connection to MXA lost.");				
		}			
		@Override
		public void onMXAConnected() {
			//mainActivity.makeToast("Connection to MXA established.");
			connectToXMPPServer();
		}
	};
	
	/** Handler which gets a message if connection to xmpp server is established or lost */
    private Handler xmppConnectionResultHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mainActivity.makeToast("XMPP connected");	
			sendServiceDiscoveryIQ();
		}
	};
		
    private Handler serviceDiscoveryHandler = new Handler() {		
		@Override
		public void handleMessage(Message msg) {
			Log.i(TAG, "serviceDiscoveryHandler is called");
			if (msg.what == ConstMXA.MSG_DISCOVER_ITEMS)
				if (msg.arg1 == ConstMXA.MSG_STATUS_SUCCESS) {
					Bundle d = msg.getData();					
					d.setClassLoader(mainActivity.getClassLoader());
					String                  jid   = d.getString("JID"); 
					ArrayList<DiscoverItem> items = d.getParcelableArrayList("DISCOVER_ITEMS");
					if (jid.equals(mobilisServerJid)) {
						Log.i(TAG, "serviceDiscoveryHandler --> received response of MobilisServer ("+items.size()+" registered resources)");
						for (DiscoverItem item: items)
							if (!item.jid.equals(mobilisServerJid)) {							
								sendServiceDiscoveryIQ(item.jid);
							}
					} else {						
						for (DiscoverItem item: items)							
							if (item.node.startsWith(ConstMGroups.NAMESPACE_GROUPING_SERVICE)) {
								groupingService = jid;
								Log.i(TAG, "serviceDiscoveryHandler --> groupingService set to: "+groupingService);
								sendGroupQueryBeanGet(mainActivity.getMapView());
								mainActivity.makeToast("GroupingService found: "+groupingService);
								break;
							}
					}
				} else if (msg.arg1 != ConstMXA.MSG_STATUS_DELIVERED)
					Log.e(TAG, "serviceDiscoveryHandler --> Error at Service Discovery");
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
//			makeToast(toast);
			
		}
	};
	
	public String getGroupingService() {
		return this.groupingService;
	}
	

	private final IGroupManager.Stub mBinder = new IGroupManager.Stub(){

		@Override
		public void sendGroupDeleteBeanSet(String groupId)
				throws RemoteException {
			xmppManager.sendGroupDeleteBeanSet(groupId);
			
		}

		@Override
		public void sendGroupInfoBeanGet(String groupId) throws RemoteException {
			xmppManager.sendGroupInfoBeanGet(groupId);			
		}

		@Override
		public void sendGroupLeaveBeanSet(String groupId)
				throws RemoteException {
			xmppManager.sendGroupLeaveBeanSet(groupId);		
		}

		@Override
		public void sendGroupMemberInfoBeanGet(String jid)
				throws RemoteException {
			xmppManager.sendGroupMemberInfoBeanGet(jid);			
		}

		@Override
		public void connectToMXA() throws RemoteException {
			xmppManager.connectToMXA();			
		}

		@Override
		public void connectToXMPPServer() throws RemoteException {
			xmppManager.connectToXMPPServer();			
		}

		@Override
		public void sendGroupInviteBeanSet(List<String> invitees, String groupId)
				throws RemoteException {
			Set<String> inviteesSet = new HashSet<String>(invitees.size());
			inviteesSet.addAll(invitees);
			xmppManager.sendGroupInviteBeanSet(inviteesSet, groupId);			
		}

		@Override
		public void sendGroupJoinBeanSet(String groupId, int userLongitude, int userLatitude) throws RemoteException {
			xmppManager.sendGroupJoinBeanSet(groupId, userLongitude, userLatitude);
			
		}

		@Override
		public void sendGroupMemberInfoBeanSet(String packetId)
				throws RemoteException {
			// TODO Auto-generated method stub
			
		}
		    
	};

	@Override
    public IBinder onBind(Intent intent) {        
            return mBinder;        
    }
	
}
