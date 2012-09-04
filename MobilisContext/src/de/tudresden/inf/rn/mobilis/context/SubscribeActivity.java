package de.tudresden.inf.rn.mobilis.context;

import de.tudresden.inf.rn.mobilis.mxa.MXAController;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SubscribeActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.subscribe);
        initComponents();        
    }
    
    private void initComponents() {		
		Button btn_sub = (Button) findViewById(R.id.subscribe_button);
		btn_sub.setOnClickListener(new OnClickListener() {        	
        	@Override
            public void onClick(View v) {        		
        		String user, node;        		
        		user = ((EditText) findViewById(R.id.subscribe_user)).getText().toString();
        		node = ((EditText) findViewById(R.id.subscribe_node)).getText().toString();        		     		
        		XMPPManager.getInstance().sendSubscriptionSet(user, node);
         		finish();
            }
        });
		
		Button btn_unsub = (Button) findViewById(R.id.unsubscribe_button);
		btn_unsub.setOnClickListener(new OnClickListener() {        	
        	@Override
            public void onClick(View v) {        		
        		String user, node;        		
        		user = ((EditText) findViewById(R.id.subscribe_user)).getText().toString();
        		node = ((EditText) findViewById(R.id.subscribe_node)).getText().toString();        		     		
        		XMPPManager.getInstance().sendUnsubscriptionSet(user, node);
        		finish();
            }
        });
	}
    
}