package de.tudresden.inf.rn.mobilis.context;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class MoodActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mood);
                
        Spinner spinner = (Spinner) findViewById(R.id.mood_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.mood_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        initComponents();                
    }
    
    private void initComponents() {
		
		Button btn_publish = (Button) findViewById(R.id.mood_button);
		btn_publish.setOnClickListener(new OnClickListener() {        	
        	@Override
            public void onClick(View v) {        		        		        		
        		XMPPManager.getInstance().sendUserMoodInfoSet(
        				((Spinner) findViewById(R.id.mood_spinner)).getSelectedItem().toString(),
        				((EditText) findViewById(R.id.mood_text)).getText().toString());        				
            }
        });
		
	}
    
}