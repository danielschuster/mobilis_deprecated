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
package de.tudresden.inf.rn.mobilis.testing;

import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import de.tudresden.inf.rn.mobilis.mxa.ConstMXA;
import de.tudresden.inf.rn.mobilis.mxa.IXMPPService;
import de.tudresden.inf.rn.mobilis.mxa.MXAController;
import de.tudresden.inf.rn.mobilis.mxa.MXAListener;
import de.tudresden.inf.rn.mobilis.mxa.callbacks.IConnectionCallback;
import de.tudresden.inf.rn.mobilis.mxa.callbacks.IXMPPIQCallback;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPIQ;
import de.tudresden.inf.rn.mobilis.xmpp.beans.Mobilis;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.testing.MobilisPingBean;

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
	public static final String NAMESPACE_TESTING_SERVICE = NAMESPACE_SERVICES + "/Testing";
		
	private static XMPPManager xmppManager;
	
	private MXAController mxaController;
	private IXMPPService xmppService;
	private MainActivity mainActivity;
	
	private String testingService="mobilis@mobilis.tu-dresden.de/Testing";	
	
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
		MobilisPingBean beanPrototype1 = new MobilisPingBean();		
        Parceller.getInstance().registerXMPPBean(beanPrototype1);
        
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
			xmppService.registerIQCallback(pingCallback, MobilisPingBean.CHILD_ELEMENT, MobilisPingBean.NAMESPACE);
			mainActivity.makeToast("IQ Callbacks registered.");
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setMainActivity(MainActivity mainActivity) {
		this.mainActivity=mainActivity;
	}
		
	
	public void sendPingGet() {				
		MobilisPingBean beanToSend = new MobilisPingBean();
		
		if (testingService!=null) {
			try {
				beanToSend.setFrom(xmppService.getUsername());				
				beanToSend.setTo(testingService);
				xmppService.sendIQ(new Messenger(ackHandler), null, 1, Parceller.getInstance().convertXMPPBeanToIQ(beanToSend, true));
				Log.i(TAG, "MobilisPingBean with type=GET was sent. packetID:"+beanToSend.getId()+" from:"+beanToSend.getFrom()+" to:"+beanToSend.getTo());		
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Log.e(TAG, "sendPingGet --> testingService is still null. //Sending Discovery-IQ now.");
//			sendServiceDiscoveryIQ();
		}
	}
	
	private void sendPongResult(MobilisPingBean pingBean) {		
		MobilisPingBean beanToSend = pingBean.clone();
		beanToSend.setType(XMPPBean.TYPE_RESULT);
		beanToSend.setFrom(pingBean.getTo());
		beanToSend.setTo(pingBean.getFrom());
		
		try {			
			xmppService.sendIQ(new Messenger(ackHandler), null, 1, Parceller.getInstance().convertXMPPBeanToIQ(beanToSend, true));
			Log.i(TAG, "MobilisPingBean with type=RESULT was sent. packetID:"+beanToSend.getId()+" from:"+beanToSend.getFrom()+" to:"+beanToSend.getTo());		
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	
	/********************** Listener & Handler for XMPP **********************/
		
	
	/** Callback which is is informed about arrival of a MobilisPingBean. */
	IXMPPIQCallback pingCallback = new IXMPPIQCallback.Stub() {
		@Override
		public void processIQ(XMPPIQ iq) throws RemoteException {			
			Log.i(TAG,"pingCallback --> processIQ --> iqPacketId:"+iq.packetID);			
			XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
			if (b instanceof MobilisPingBean) {	
			
				MobilisPingBean bb = (MobilisPingBean) b;
				if (b.getType() == XMPPBean.TYPE_GET) {					
					// incoming ping. Now send the pong
					Log.i(TAG, "Incoming Ping with type=GET arrived. Now sending the Pong...");
					textToShowInToast="Incoming Ping";
					showToastHandler.sendEmptyMessage(0);
					sendPongResult(bb);						
				} else if (b.getType() == XMPPBean.TYPE_RESULT) {
					// incoming pong.					
					Log.i(TAG, "Incoming Pong with type=RESULT arrived.");	
				} else if (b.getType() == XMPPBean.TYPE_ERROR) {
					//ErrorIQ - do something
					Log.e(TAG, "MobilisPingBean type=ERROR arrived. IQ-Payload:" + iq.payload);
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
			testingService = null;
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
//			sendServiceDiscoveryIQ();
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
