package de.tudresden.inf.rn.mobilis.testing;

import android.app.Activity;
import android.os.Bundle;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.widget.Toast;

public class MainActivity extends Activity {
       
    private MyPhoneStateListener m_psl;
    private TelephonyManager m_tm;
    
	//XMPP
	private static XMPPManager xmppManager;
    
    private static MainActivity instance;
	
	public static MainActivity getInstance() {
		if(instance==null) {
			instance = new MainActivity(); 
		}
		return instance;
	}

    
    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         m_tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
         m_psl = new MyPhoneStateListener();
         m_tm.listen(m_psl, PhoneStateListener.LISTEN_SIGNAL_STRENGTH);
//         m_tm.listen(m_psl, PhoneStateListener.LISTEN_CELL_LOCATION);
         
         MainActivity.xmppManager = XMPPManager.getInstance();
         xmppManager.setMainActivity(this);        
         //Connect application to MXA 
     	xmppManager.connectToMXA();
   }

    @Override
    protected void onDestroy() {
         super.onDestroy();
         m_tm.listen(m_psl, PhoneStateListener.LISTEN_NONE);
    }

    @Override
    protected void onPause() {
         super.onPause();
         m_tm.listen(m_psl, PhoneStateListener.LISTEN_NONE);
    }

    @Override
    protected void onResume() {
         super.onResume();
         m_tm.listen(m_psl, PhoneStateListener.LISTEN_SIGNAL_STRENGTH);
         m_tm.listen(m_psl, PhoneStateListener.LISTEN_CELL_LOCATION);
    }
    
    /** Shows a short Toast message on the map */
    public void makeToast(String text) {
    	Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }

    private class MyPhoneStateListener extends PhoneStateListener{

         @Override
         public void onSignalStrengthsChanged(SignalStrength signalStrength){
              super.onSignalStrengthsChanged(signalStrength);
              Toast.makeText(getApplicationContext(), "Signal strength is now "+signalStrength.getGsmSignalStrength(), Toast.LENGTH_SHORT).show();
         }

         @Override
         public void onCellLocationChanged(CellLocation location) {
              super.onCellLocationChanged(location);
//              Toast.makeText(getApplicationContext(), "Current cell location changed. ", Toast.LENGTH_SHORT).show();
              GsmCellLocation gsmCell = (GsmCellLocation) location;
              Toast.makeText(getApplicationContext(), "new GSM cell: "+gsmCell.getCid(), Toast.LENGTH_SHORT).show();
         }
    }; 
}