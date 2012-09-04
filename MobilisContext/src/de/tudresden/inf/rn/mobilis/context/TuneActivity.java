package de.tudresden.inf.rn.mobilis.context;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class TuneActivity extends Activity {
    
	private TuneActivity instance=null;
	
	
	public TuneActivity getInstance() {
		if (instance==null)
			instance = new TuneActivity();
		return instance;
	}	
	
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        setContentView(R.layout.tune);
        
        initComponents();
        
    }


	private void initComponents() {
			
		Button btn_play = (Button) findViewById(R.id.tune_button_play);
        btn_play.setOnClickListener(new OnClickListener() {        	
        	@Override
            public void onClick(View v) {
        		System.out.println("Tune OnClick!");
        		String artist = null, source = null, title = null, track = null, uri = null;
        		int length=Integer.MIN_VALUE, rating=Integer.MIN_VALUE;
        		       		
        		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
        		int checkedRadioBtnId = radioGroup.getCheckedRadioButtonId();		
        		if (checkedRadioBtnId>-1) {
        			RadioButton radio = (RadioButton) findViewById(checkedRadioBtnId);
        			//TODO:
        			artist= radio.getText().toString();
        		}      		
        		
        		XMPPManager.getInstance().sendUserTuneInfoSet(artist, length, rating, source, title, track, uri);
            }
        });
		
	}
	
	
}