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
package de.tudresden.inf.rn.mobilis.context;

import java.util.List;

import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import de.tudresden.inf.rn.mobilis.mxa.ConstMXA;
import de.tudresden.inf.rn.mobilis.mxa.IXMPPService;
import de.tudresden.inf.rn.mobilis.mxa.MXAController;
import de.tudresden.inf.rn.mobilis.mxa.MXAListener;
import de.tudresden.inf.rn.mobilis.mxa.callbacks.IConnectionCallback;
import de.tudresden.inf.rn.mobilis.mxa.callbacks.IXMPPIQCallback;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPIQ;
import de.tudresden.inf.rn.mobilis.mxa.services.callbacks.ISubscribeCallback;
import de.tudresden.inf.rn.mobilis.xmpp.beans.Mobilis;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPUtil;
import de.tudresden.inf.rn.mobilis.xmpp.beans.context.AuthorizationBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.context.ContextItemInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.context.PubSubBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.context.PublishItemInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.context.SubscribeItemInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.context.UnsubscribeItemInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.context.UserContextInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.context.UserLocationInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.context.UserMoodInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.context.UserTuneInfo;
import de.tudresden.inf.rn.mobilis.xmpp.beans.coordination.MobilisServiceDiscoveryBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.coordination.MobilisServiceInfo;

/**
 * 
 * @author Robert Lübke
 *
 */
public class XMPPManager {
	
	/** The TAG for the Log. */
	private final static String TAG = "XMPPManager";
	
	//Namespace
	public static final String NAMESPACE_SERVICES = Mobilis.NAMESPACE + "#services";
	public static final String NAMESPACE_USERCONTEXT_SERVICE = NAMESPACE_SERVICES + "/UserContextService";
		
	private static XMPPManager xmppManager;
	
	private MXAController mxaController;
	private IXMPPService xmppService;
	private MainActivity mainActivity;
	
	//
	private String userContextService=null;	
	
	private String textToShowInToast;
	
	public static XMPPManager getInstance() {
		if(xmppManager==null) {
			xmppManager = new XMPPManager(); 
		}
		return xmppManager;
	}
	
	private XMPPManager() {		
		mxaController = MXAController.get();		
		this.mainActivity = MainActivity.getInstance();
		
		//Register bean prototypes to the Parceller
		MobilisServiceDiscoveryBean beanPrototype1 = new MobilisServiceDiscoveryBean();
		AuthorizationBean beanPrototype2 = new AuthorizationBean();
		PubSubBean beanPrototype3 = new PubSubBean();
//		GroupDeleteBean beanPrototype4 = new GroupDeleteBean();		
        Parceller.getInstance().registerXMPPBean(beanPrototype1);
        Parceller.getInstance().registerXMPPBean(beanPrototype2);
        Parceller.getInstance().registerXMPPBean(beanPrototype3);
//      Parceller.getInstance().registerXMPPBean(beanPrototype4);        
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
			xmppService.registerIQCallback(mobilisServiceDiscoveryCallback, MobilisServiceDiscoveryBean.CHILD_ELEMENT, MobilisServiceDiscoveryBean.NAMESPACE);
			xmppService.registerIQCallback(authorizationCallback, AuthorizationBean.CHILD_ELEMENT, AuthorizationBean.NAMESPACE);
			xmppService.registerIQCallback(pubsubCallback, PubSubBean.CHILD_ELEMENT, PubSubBean.NAMESPACE);
//			mainActivity.makeToast("IQ Callbacks registered.");
						
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendServiceDiscoveryIQ() {
	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mainActivity.getApplicationContext());
	String mobilisServerJid = pref.getString(mainActivity.getString(R.string.pref_mobilisserver_key), "");
	if (mobilisServerJid.equals("")) {
		this.makeToast("Specify the MobilisServer JID in the settings!");
	} else {
		//TODO: Make "/Coordinator" generic
		String coordinatorFullJid = XMPPUtil.jidWithoutRessource(mobilisServerJid)+"/Coordinator";
		userContextService = null;	
		MobilisServiceDiscoveryBean beanToSend = new MobilisServiceDiscoveryBean();	
		if (xmppService!=null) {
			try {						
				beanToSend.setFrom(xmppService.getUsername());				
				beanToSend.setTo(coordinatorFullJid);
				this.makeToast("Looking for a UserContextService at "+coordinatorFullJid);
				xmppService.sendIQ(new Messenger(ackHandler), null, 1, Parceller.getInstance().convertXMPPBeanToIQ(beanToSend, true));
				Log.i(TAG, "MobilisServiceDiscoveryBean with type=GET was sent. packetID:"+beanToSend.getId()+" from:"+beanToSend.getFrom()+" to:"+beanToSend.getTo());		
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		} else {
			Log.e(TAG, "sendServiceDiscoveryIQ() --> xmppService is still null");
		}
	}
}
	
//	public void sendServiceDiscoveryIQ() {
//		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mainActivity.getApplicationContext());
//		mobilisServerJid = "mobilis@robert-pc"; //pref.getString(mainActivity.getString(R.string.pref_mobilisserver_key), "");
//		userContextService = null;
//		if (xmppService!=null) {
//			try {
//				//TODO: requestcode
//				
//				this.xmppService.getServiceDiscoveryService().discoverItem(
//						new Messenger(serviceDiscoveryHandler),
//						new Messenger(serviceDiscoveryHandler),
//						1,
//						mobilisServerJid,
//						null);
//				Log.i(TAG, "sendServiceDiscoveryIQ --> Discovery-IQ sent to: "+mobilisServerJid);
//			} catch (RemoteException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}	
//		} else {
//			Log.e(TAG, "sendServiceDiscoveryIQ --> xmppService is still null");
//		}
//	}
	
//	public void sendServiceDiscoveryIQ(String broker) {		
//		try {
//			//TODO: requestcode			
//			this.xmppService.getServiceDiscoveryService().discoverItem(
//					new Messenger(serviceDiscoveryHandler),
//					new Messenger(serviceDiscoveryHandler),
//					1,
//					broker,
//					NAMESPACE_SERVICES);
//			Log.i(TAG, "sendServiceDiscoveryIQ(broker) --> Discovery-IQ sent to: "+broker);
//		} catch (RemoteException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}	
//	}	
			
	public void sendUserMoodInfoSet(String moodElement, String moodDescription) {		
		UserMoodInfo userMoodInfo = new UserMoodInfo(moodElement, moodDescription);
		
		PubSubBean beanToSend = new PubSubBean(
				new PublishItemInfo(
						UserMoodInfo.NAMESPACE,
						new ContextItemInfo(userMoodInfo)
				));
		if (userContextService!=null) {
			try {
				beanToSend.setFrom(xmppService.getUsername());				
				beanToSend.setTo(userContextService);
//				xmppService.registerIQCallback(pubsubCallback, PubSubBean.CHILD_ELEMENT, PubSubBean.NAMESPACE);
				xmppService.sendIQ(new Messenger(ackHandler), null, 1, Parceller.getInstance().convertXMPPBeanToIQ(beanToSend, true));
				Log.i(TAG, "PubSubBean with userMoodInfo and type=SET was sent. packetID:"+beanToSend.getId()+" from:"+beanToSend.getFrom()+" to:"+beanToSend.getTo());		
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Log.e(TAG, "sendUserMoodInfoSet --> contextService is still null. //Sending Discovery-IQ now.");
			sendServiceDiscoveryIQ();
		}
	}
	
	public void sendUserTuneInfoSet(String artist, int length, int rating, String source, String title, String track, String uri) {		
		UserTuneInfo userTuneInfo = new UserTuneInfo();
		userTuneInfo.artist=artist;
		userTuneInfo.length=length;
		userTuneInfo.rating=rating;
		userTuneInfo.source=source;
		userTuneInfo.title=title;
		userTuneInfo.track=track;
		userTuneInfo.uri=uri;
		PubSubBean beanToSend = new PubSubBean(
				new PublishItemInfo(
						UserTuneInfo.NAMESPACE,
						new ContextItemInfo(userTuneInfo)
				));
		if (userContextService!=null) {
			try {
				beanToSend.setFrom(xmppService.getUsername());				
				beanToSend.setTo(userContextService);
//				xmppService.registerIQCallback(pubsubCallback, PubSubBean.CHILD_ELEMENT, PubSubBean.NAMESPACE);
				xmppService.sendIQ(new Messenger(ackHandler), null, 1, Parceller.getInstance().convertXMPPBeanToIQ(beanToSend, true));
				Log.i(TAG, "PubSubBean with userTuneInfo and type=SET was sent. packetID:"+beanToSend.getId()+" from:"+beanToSend.getFrom()+" to:"+beanToSend.getTo());		
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Log.e(TAG, "sendUserTuneInfoSet --> contextService is still null. //Sending Discovery-IQ now.");
			sendServiceDiscoveryIQ();
		}
	}
	
	public void sendUserLocationInfoSet(double longitude, double latitude, String timestamp) {		
		UserLocationInfo userLocationInfo = new UserLocationInfo();
		userLocationInfo.setLon(longitude);
		userLocationInfo.setLat(latitude);
		userLocationInfo.setTimestamp(timestamp);
		PubSubBean beanToSend = new PubSubBean(
				new PublishItemInfo(
						UserLocationInfo.NAMESPACE,
						new ContextItemInfo(userLocationInfo)
				));
		if (userContextService!=null) {
			try {
				beanToSend.setFrom(xmppService.getUsername());				
				beanToSend.setTo(userContextService);
//				xmppService.registerIQCallback(pubsubCallback, PubSubBean.CHILD_ELEMENT, PubSubBean.NAMESPACE);
				xmppService.sendIQ(new Messenger(ackHandler), null, 1, Parceller.getInstance().convertXMPPBeanToIQ(beanToSend, true));
				Log.i(TAG, "PubSubBean with userLocationInfo and type=SET was sent. packetID:"+beanToSend.getId()+" from:"+beanToSend.getFrom()+" to:"+beanToSend.getTo());		
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Log.e(TAG, "sendUserLocationInfoSet --> contextService is still null. //Sending Discovery-IQ now.");
			sendServiceDiscoveryIQ();
		}
	}
	
	public void sendUserContextInfoSet(String type, String key, String value, String path) {		
		int typeInt;
		type = type.toLowerCase();
		if (type.equals("string")) typeInt=Mobilis.USERCONTEXT_DATATYPE_STRING;
		else if (type.equals("integer")) typeInt=Mobilis.USERCONTEXT_DATATYPE_INTEGER;
		else if (type.equals("long")) typeInt=Mobilis.USERCONTEXT_DATATYPE_LONG;
		else if (type.equals("double")) typeInt=Mobilis.USERCONTEXT_DATATYPE_DOUBLE;
		else if (type.equals("boolean")) typeInt=Mobilis.USERCONTEXT_DATATYPE_BOOLEAN;
		else typeInt=Mobilis.USERCONTEXT_DATATYPE_UNKNOWN;
				
		UserContextInfo userContextInfo = new UserContextInfo(typeInt, key, value, path);
		
		PubSubBean beanToSend = new PubSubBean(
				new PublishItemInfo(
						path,
						new ContextItemInfo(userContextInfo)
				));
		if (userContextService!=null) {
			try {
				beanToSend.setFrom(xmppService.getUsername());				
				beanToSend.setTo(userContextService);
//				xmppService.registerIQCallback(pubsubCallback, PubSubBean.CHILD_ELEMENT, PubSubBean.NAMESPACE);
				xmppService.sendIQ(new Messenger(ackHandler), null, 1, Parceller.getInstance().convertXMPPBeanToIQ(beanToSend, true));
				Log.i(TAG, "PubSubBean with userContextInfo and type=SET was sent. packetID:"+beanToSend.getId()+" from:"+beanToSend.getFrom()+" to:"+beanToSend.getTo());		
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Log.e(TAG, "sendUserLocationInfoSet --> contextService is still null. //Sending Discovery-IQ now.");
			sendServiceDiscoveryIQ();
		}
	}
	
	public void sendSubscriptionSet(String user, String node) {
		PubSubBean beanToSend = new PubSubBean(
				new SubscribeItemInfo(node, user)
				);
		if (userContextService!=null) {
			try {
				beanToSend.setFrom(xmppService.getUsername());				
				beanToSend.setTo(userContextService);	
//				xmppService.registerIQCallback(pubsubCallback, PubSubBean.CHILD_ELEMENT, PubSubBean.NAMESPACE);
				xmppService.sendIQ(new Messenger(ackHandler), null, 1, Parceller.getInstance().convertXMPPBeanToIQ(beanToSend, true));
				Log.i(TAG, "PubSubBean with SUBSCRIPTION and type=SET was sent. packetID:"+beanToSend.getId()+" from:"+beanToSend.getFrom()+" to:"+beanToSend.getTo());		
				String userNodeFormat = XMPPUtil.jidWithoutRessource(user)+"/"+node;
				xmppService.getPubSubService().subscribe(mSubscribeCallback, userContextService, userNodeFormat);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Log.e(TAG, "sendSubscriptionSet --> contextService is still null. //Sending Discovery-IQ now.");
			sendServiceDiscoveryIQ();
		}		
	}
	
	
	public void sendUnsubscriptionSet(String user, String node) {
		PubSubBean beanToSend = new PubSubBean(
				new UnsubscribeItemInfo(node, user)
				);
		if (userContextService!=null) {
			try {
				beanToSend.setFrom(xmppService.getUsername());				
				beanToSend.setTo(userContextService);	
//				xmppService.registerIQCallback(pubsubCallback, PubSubBean.CHILD_ELEMENT, PubSubBean.NAMESPACE);
				xmppService.sendIQ(new Messenger(ackHandler), null, 1, Parceller.getInstance().convertXMPPBeanToIQ(beanToSend, true));
				Log.i(TAG, "PubSubBean with UNSUBSCRIPTION and type=SET was sent. packetID:"+beanToSend.getId()+" from:"+beanToSend.getFrom()+" to:"+beanToSend.getTo());		
				String userNodeFormat = XMPPUtil.jidWithoutRessource(user)+"/"+node;
				xmppService.getPubSubService().unsubscribe(mSubscribeCallback, userContextService, userNodeFormat);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Log.e(TAG, "sendUnsubscriptionSet --> contextService is still null. //Sending Discovery-IQ now.");
			sendServiceDiscoveryIQ();
		}		
	}
			
	public void sendAuthorizationResult(AuthorizationBean beanRequest) {
		AuthorizationBean beanToSend = new AuthorizationBean();
		beanToSend.pathToElement=beanRequest.pathToElement;
		beanToSend.userJidToAuthorize=beanRequest.userJidToAuthorize;
		
		if (userContextService!=null) {
			try {
				beanToSend.setFrom(xmppService.getUsername());				
				beanToSend.setTo(userContextService);
				beanToSend.setId(beanRequest.getId());
				XMPPIQ iq = Parceller.getInstance().convertXMPPBeanToIQ(beanToSend, true);
				xmppService.sendIQ(new Messenger(ackHandler), null, 1, iq);
				Log.i(TAG, "AuthorizationBean type=RESULT was sent. packetID:"+beanToSend.getId()+" from:"+beanToSend.getFrom()+" to:"+beanToSend.getTo());		
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Log.e(TAG, "sendAuthorizationResult --> contextService is still null. //Sending Discovery-IQ now.");
			sendServiceDiscoveryIQ();
		}		
	}
	
	/********************** Listener & Handler for XMPP **********************/
		
		
	/** Callback which is is informed about arrival of a MobilisServiceDiscoveryIQ. */
	IXMPPIQCallback mobilisServiceDiscoveryCallback = new IXMPPIQCallback.Stub() {
		@Override
		public void processIQ(XMPPIQ iq) throws RemoteException {
			XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
			if (b instanceof MobilisServiceDiscoveryBean) {
				MobilisServiceDiscoveryBean bb = (MobilisServiceDiscoveryBean) b;			
				if (b.getType() == XMPPBean.TYPE_GET) {
					// do nothing
				} else if (b.getType() == XMPPBean.TYPE_RESULT) {
					//ResultIQ - Look for the first UserContextService that was discovered
					Log.i(TAG, "MobilisServiceDiscoveryBean type=RESULT arrived. IQ-Payload:" + iq.payload);
					List<MobilisServiceInfo> discoveredMobilisServices = bb.getDiscoveredServices();
					if (discoveredMobilisServices!=null)
						for (MobilisServiceInfo msi : discoveredMobilisServices) {
							if (msi.getServiceNamespace().equals(NAMESPACE_USERCONTEXT_SERVICE)) {
								userContextService = msi.getJid();
								textToShowInToast = "discovered new UserContextService: " + userContextService;
								showToastHandler.sendEmptyMessage(0);
								Log.i(TAG, "discovered new UserContextService: " + userContextService);	
								break;
							}								
						}
				} else if (b.getType() == XMPPBean.TYPE_ERROR) {
					//ErrorIQ - do something
					Log.e(TAG, "MobilisServiceDiscoveryBean type=ERROR arrived. IQ-Payload:" + iq.payload);	
				}
			}				
		}
	};
	
	/** Callback which is is informed about arrival of a AuthorizationIQ. */
	IXMPPIQCallback authorizationCallback = new IXMPPIQCallback.Stub() {
		@Override
		public void processIQ(XMPPIQ iq) throws RemoteException {
			Log.i(TAG,"authorizationCallback --> processIQ --> iqPacketId:"+iq.packetID);
			XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
			if (b instanceof AuthorizationBean) {				
				if (b.getType() == XMPPBean.TYPE_GET) {
					AuthorizationBean bb = (AuthorizationBean) b;
					// send back the permission
					sendAuthorizationResult(bb);					
				} else if (b.getType() == XMPPBean.TYPE_RESULT) {
					//ResultIQ - do nothing
				} else if (b.getType() == XMPPBean.TYPE_ERROR) {
					//ErrorIQ - do something
					Log.e(TAG, "MobilisServiceDiscoveryBean type=ERROR arrived. IQ-Payload:" + iq.payload);	
				}
			}				
		}
	};
	
	/** Callback which is is informed about arrival of a PubSubBean. */
	IXMPPIQCallback pubsubCallback = new IXMPPIQCallback.Stub() {
		@Override
		public void processIQ(XMPPIQ iq) throws RemoteException {			
			Log.i(TAG,"pubsubCallback --> processIQ --> iqPacketId:"+iq.packetID);			
			XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
			if (b instanceof PubSubBean) {	
//				xmppService.unregisterIQCallback(pubsubCallback, PubSubBean.CHILD_ELEMENT, PubSubBean.NAMESPACE);
				PubSubBean bb = (PubSubBean) b;
				if (b.getType() == XMPPBean.TYPE_GET) {					
					// do something
										
				} else if (b.getType() == XMPPBean.TYPE_RESULT) {
					//ResultIQ
					if (bb.publish!=null) {
						textToShowInToast="Published successfully";
						showToastHandler.sendEmptyMessage(0);
					} else if (bb.subscribe!=null) {
						textToShowInToast="Subscribed successfully";
						showToastHandler.sendEmptyMessage(0);
					} else if (bb.unsubscribe!=null) {
						textToShowInToast="Unsubscribed successfully";
						showToastHandler.sendEmptyMessage(0);
					}
						
				} else if (b.getType() == XMPPBean.TYPE_ERROR) {
					//ErrorIQ - do something
					Log.e(TAG, "PubSubBean type=ERROR arrived. IQ-Payload:" + iq.payload);
					textToShowInToast="ERROR: " + bb.errorText;
					showToastHandler.sendEmptyMessage(0);
				}
			}				
		}
	};

	
	/** Listener which is informed about connection / disconntection to MXA (NOT to XMPP server!) */
	private MXAListener mxaConnectionListener = new MXAListener() {			
		@Override
		public void onMXADisconnected() {
			userContextService = null;
			mainActivity.makeToast("Connection to MXA lost.");				
		}			
		@Override
		public void onMXAConnected() {
			mainActivity.makeToast("Connection to MXA established.");
			
			try {
				MXAController.get().getXMPPService().registerConnectionCallback(
						mConnectionCallback);
			} catch (RemoteException e) {
				Log.e(TAG, "RemoteException!!");
				e.printStackTrace();
			}
									
			connectToXMPPServer();
		}
	};
	
	/** Handler which gets a message if connection to xmpp server is established or lost */
    private Handler xmppConnectionResultHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mainActivity.makeToast("XMPP connected");
			//Sending out a Mobilis Service Discovery to find a UserContextService
			sendServiceDiscoveryIQ();
		}
	};
	
	
	private IConnectionCallback mConnectionCallback = new IConnectionCallback.Stub() {

		@Override
		public void onConnectionChanged(boolean connected)
				throws RemoteException {
			Log.i(TAG, "XMPP Connection changed. connected="+connected);
//			mainActivity.makeToast("XMPP Connection changed. connected="+connected);
			
		}
	};
	
	private ISubscribeCallback mSubscribeCallback = new ISubscribeCallback.Stub() {

		@Override
		public void onPublishEvent(String from, String node, String items)
				throws RemoteException {
			// TODO
			Log.i(TAG, "mSubscribeCallback --> onPublishEvent(). from="+from+" node="+node+" items="+items);	
			textToShowInToast = "Node "+node+" updated.";
			showToastHandler.sendEmptyMessage(0);
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
	
	public String getUserContextService() {
		return this.userContextService;
	}

	public void setMainActivity(MainActivity mainActivity) {
		this.mainActivity=mainActivity;		
	}

	private Handler showToastHandler = new Handler(){
    	@Override
    	public void handleMessage(Message msg) {    		
    		makeToast(textToShowInToast);
    	}
    };
	
	/** Shows a short Toast message on the map */
	public void makeToast(String text) {
		mainActivity.makeToast(text);
	}
	
	
}
