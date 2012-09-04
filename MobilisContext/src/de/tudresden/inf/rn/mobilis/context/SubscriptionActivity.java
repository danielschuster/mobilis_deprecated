package de.tudresden.inf.rn.mobilis.context;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class SubscriptionActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.subscription);
        initComponents();                
    }
    
    private void initComponents() {
		
		Button btn_subscribeTo = (Button) findViewById(R.id.subscribe_button);
		btn_subscribeTo.setOnClickListener(new OnClickListener() {        	
        	@Override
            public void onClick(View v) {     		        		        		
        		startActivity(new Intent(getApplicationContext(), SubscribeActivity.class));
            }
        });
		
	}
    
    
}