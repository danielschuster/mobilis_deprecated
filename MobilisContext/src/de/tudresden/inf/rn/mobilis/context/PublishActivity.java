package de.tudresden.inf.rn.mobilis.context;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class PublishActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.publish);
                
        Spinner spinner = (Spinner) findViewById(R.id.publish_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.context_type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);     
        
        initComponents();        
    }
    
    private void initComponents() {
		
		Button btn_pub = (Button) findViewById(R.id.publish_button);
		btn_pub.setOnClickListener(new OnClickListener() {        	
        	@Override
            public void onClick(View v) {        		
        		String type, key, value, path;        		
        		key = ((EditText) findViewById(R.id.publish_key)).getText().toString();
        		value = ((EditText) findViewById(R.id.publish_value)).getText().toString();
        		path = ((EditText) findViewById(R.id.publish_path)).getText().toString();
        		type =(((Spinner) findViewById(R.id.publish_spinner)).getSelectedItem()).toString();        		
        		XMPPManager.getInstance().sendUserContextInfoSet(type, key, value, path);
        		finish();
            }
        });		
	}
    
}