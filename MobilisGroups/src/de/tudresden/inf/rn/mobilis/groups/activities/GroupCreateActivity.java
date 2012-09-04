package de.tudresden.inf.rn.mobilis.groups.activities;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import de.tudresden.inf.rn.mobilis.groups.Parceller;
import de.tudresden.inf.rn.mobilis.groups.R;
import de.tudresden.inf.rn.mobilis.groups.XMPPManager;
import de.tudresden.inf.rn.mobilis.mxa.ConstMXA;
import de.tudresden.inf.rn.mobilis.mxa.IXMPPService;
import de.tudresden.inf.rn.mobilis.mxa.MXAController;
import de.tudresden.inf.rn.mobilis.mxa.callbacks.IXMPPIQCallback;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPIQ;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupCreateBean;
import de.tudresden.inf.rn.mobilis.xmpp.beans.groups.GroupDeleteBean;

public class GroupCreateActivity extends Activity {

	/** The TAG for the Log. */
	private final static String TAG = "GroupCreateActivity";
	
	private IXMPPService xmppService;
	private GroupCreateActivity groupCreateActivity;
	private String groupId, textToShowInToast;
	
	
	static final int STARTTIME_DIALOG_ID = 0;
	static final int ENDTIME_DIALOG_ID = 1;
	static final int STARTJOINTIME_DIALOG_ID = 2;
	static final int ENDJOINTIME_DIALOG_ID = 3;
	
	private long startTime=Long.MIN_VALUE, endTime=Long.MIN_VALUE,
		startJoinTime=Long.MIN_VALUE, endJoinTime=Long.MIN_VALUE; 
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.groupCreateActivity = this;               
        setContentView(R.layout.groupcreate);
        
        xmppService = MXAController.get().getXMPPService();
                
        Bundle extras = this.getIntent().getExtras();
        if (extras.containsKey("group_groupId")) {
        	// It's an Update call.
        	this.groupId=extras.getString("group_groupId");
        	this.setTitle(R.string.app_name_groupcreateactivity_update);
        } else {
        	// It's a normal Create call.
        	this.setTitle(R.string.app_name_groupcreateactivity);
        	this.groupId=null;
        }
               
        Spinner s = (Spinner) findViewById(R.id.spinner_privacy);
        ArrayAdapter adapter = ArrayAdapter.createFromResource(
                this, R.array.group_privacy, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);       
        s.setAdapter(adapter);
        
        initGUIElements();
        readDefaultTextFromIntent();               
        
        Button button = (Button) findViewById(R.id.button_create_group);
        if (groupId!=null)
        	button.setText("Update");
        else
        	button.setText("Create");
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	int longitude, latitude, visibilityRadius, joinRadius;
            	try {
	    			longitude = Integer.valueOf(((EditText) findViewById(R.id.edittext_longitude)).getText().toString()).intValue();	    			
            	} catch(NumberFormatException e) {
            		Log.e(TAG, "NumberFormatException while parsing longitude - set to default value 0.");
            		longitude=0;
            	}
            	try {
	    			latitude = Integer.valueOf(((EditText) findViewById(R.id.edittext_latitude)).getText().toString()).intValue();
	    		} catch(NumberFormatException e) {
            		Log.e(TAG, "NumberFormatException while parsing latitude - set to default value 0.");
            		latitude=0;
            	}
            	try {	    			
	    			visibilityRadius = Integer.valueOf(((EditText) findViewById(R.id.edittext_visibilityradius)).getText().toString()).intValue();
            	} catch(NumberFormatException e) {
            		Log.e(TAG, "NumberFormatException while parsing visibility radius - set to default value 0.");
            		visibilityRadius=0;
            	}
            	try {	    			
	    			joinRadius = Integer.valueOf(((EditText) findViewById(R.id.edittext_joinradius)).getText().toString()).intValue();
            	} catch(NumberFormatException e) {
            		Log.e(TAG, "NumberFormatException while parsing join radius - set to default value 0.");
            		joinRadius=0;
            	}
            	
            	Log.i(TAG, "startjoinTime: "+(new Date(startJoinTime)).toString());
            	long a=startJoinTime, b=endJoinTime, c=startTime, d=endTime;
            	
            	if (!((CheckBox) (findViewById(R.id.checkbox_starttime))).isChecked())
            		c=Long.MIN_VALUE;
            	if (!((CheckBox) (findViewById(R.id.checkbox_endtime))).isChecked())
            		d=Long.MIN_VALUE;
            	if (!((CheckBox) (findViewById(R.id.checkbox_startjointime))).isChecked())
            		a=Long.MIN_VALUE;
            	if (!((CheckBox) (findViewById(R.id.checkbox_endjointime))).isChecked())
            		b=Long.MIN_VALUE;
            	
            	
            	
            	GroupCreateBean bean = new GroupCreateBean(
            			((EditText) findViewById(R.id.edittext_name)).getText().toString().replaceAll("&", "+"),
            			((EditText) findViewById(R.id.edittext_description)).getText().toString().replaceAll("&", "+"),            			
            			((EditText) findViewById(R.id.edittext_address)).getText().toString().replaceAll("&", "+"),
            			longitude, latitude,
            			visibilityRadius, longitude, latitude,
            			joinRadius, longitude, latitude,
            			a, b, c, d,
            			((Spinner) findViewById(R.id.spinner_privacy)).getSelectedItem().toString(),
            			((EditText) findViewById(R.id.edittext_link)).getText().toString().replaceAll("&", "+") );
            	if (groupId!=null) {
            		//It's an Update Call.
            		bean.setGroupId(groupId);
            	}            	

            	if (XMPPManager.getInstance().getGroupingService()!=null) {	            		
            		try {
						xmppService.registerIQCallback(groupCreateCallback, GroupCreateBean.CHILD_ELEMENT, GroupCreateBean.NAMESPACE);
						
	            		bean.setFrom(xmppService.getUsername());
		            	bean.setTo(XMPPManager.getInstance().getGroupingService());
		            	bean.setType(XMPPBean.TYPE_SET);            	
						xmppService.sendIQ(new Messenger(ackHandler),
								null,
								1, 
								Parceller.getInstance().convertXMPPBeanToIQ(bean, true));
						Log.i(TAG, "GroupCreateBean "+bean.getId()+" sent from "+bean.getFrom()+ " to "+bean.getTo());
            		} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}  
            	} else {
            		Log.e(TAG, "Create/Update Button clicked --> groupingService is still null. Sending Discovery-IQ now.");            		
        			XMPPManager.getInstance().sendServiceDiscoveryIQ();
        			makeToast("No GroupingService found. Check MobilisServer-JID Settings.");
            	}

				            	
            }
        });        
        
    }
    
    private void initGUIElements() {
    	Button button = (Button) findViewById(R.id.button_pickDate_starttime);        
        button.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				showDialog(STARTTIME_DIALOG_ID);				
			}
		});        
        button = (Button) findViewById(R.id.button_pickDate_endtime);        
        button.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				showDialog(ENDTIME_DIALOG_ID);				
			}
		});        
        button = (Button) findViewById(R.id.button_pickDate_startjointime);        
        button.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				showDialog(STARTJOINTIME_DIALOG_ID);				
			}
		});        
        button = (Button) findViewById(R.id.button_pickDate_endjointime);        
        button.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				showDialog(ENDJOINTIME_DIALOG_ID);				
			}
		});
        
        // get the current date
        Date now = new Date();
        int mYear = now.getYear();
        int mMonth = now.getMonth();
        int mDay = now.getDate();
//        final Calendar c = Calendar.getInstance();
//        int mYear = c.get(Calendar.YEAR);
//        int mMonth = c.get(Calendar.MONTH);
//        int mDay = c.get(Calendar.DAY_OF_MONTH);
        
        Date d = new Date(mYear, mMonth, mDay);
        
        startTime = d.getTime();
        Log.i(TAG, "initGUIElements --> startTime: "+(new Date(startTime)).toString());
        endTime = d.getTime();
        startJoinTime = d.getTime();        
        endJoinTime = d.getTime();
        
        mYear+=1900;
        
        datePickerDialogStartTime = new DatePickerDialog(
    			this, dateSetListenerStartTime,
                mYear, mMonth, mDay);    
    	datePickerDialogEndTime = new DatePickerDialog(
    			this, dateSetListenerEndTime,
                mYear, mMonth, mDay);
    	datePickerDialogStartJoinTime = new DatePickerDialog(
    			this, dateSetListenerStartJoinTime,
    			mYear, mMonth, mDay);
    	datePickerDialogEndJoinTime = new DatePickerDialog(this,
        			dateSetListenerEndJoinTime,
                    mYear, mMonth, mDay);
    	
        ((CheckBox) findViewById(R.id.checkbox_starttime)).setText(mDay+"."+(mMonth+1)+"."+mYear);
        ((CheckBox) findViewById(R.id.checkbox_starttime)).setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {				
				((Button) findViewById(R.id.button_pickDate_starttime)).setEnabled(isChecked);
			}
		});
        ((CheckBox) findViewById(R.id.checkbox_endtime)).setText(mDay+"."+(mMonth+1)+"."+mYear);
        ((CheckBox) findViewById(R.id.checkbox_endtime)).setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {				
				((Button) findViewById(R.id.button_pickDate_endtime)).setEnabled(isChecked);
			}
		});
        ((CheckBox) findViewById(R.id.checkbox_startjointime)).setText(mDay+"."+(mMonth+1)+"."+mYear);
        ((CheckBox) findViewById(R.id.checkbox_startjointime)).setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {				
				((Button) findViewById(R.id.button_pickDate_startjointime)).setEnabled(isChecked);
			}
		});
        ((CheckBox) findViewById(R.id.checkbox_endjointime)).setText(mDay+"."+(mMonth+1)+"."+mYear);
        ((CheckBox) findViewById(R.id.checkbox_endjointime)).setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {				
				((Button) findViewById(R.id.button_pickDate_endjointime)).setEnabled(isChecked);
			}
		});
		
    	
	}

	/**
     * Handles the extras of the intent. 
     */
    private void readDefaultTextFromIntent() {
    	Bundle extras = this.getIntent().getExtras();
        if (extras.containsKey("group_name"))
        	((EditText) findViewById(R.id.edittext_name)).setText(extras.getString("group_name"));
        if (extras.containsKey("group_description"))
        	((EditText) findViewById(R.id.edittext_description)).setText(extras.getString("group_description"));
        if (extras.containsKey("group_address"))
        	((EditText) findViewById(R.id.edittext_address)).setText(extras.getString("group_address"));
        if (extras.containsKey("group_latitude"))
        	((EditText) findViewById(R.id.edittext_latitude)).setText(String.valueOf(extras.getInt("group_latitude")));
        if (extras.containsKey("group_longitude"))
        	((EditText) findViewById(R.id.edittext_longitude)).setText(String.valueOf(extras.getInt("group_longitude")));
        if (extras.containsKey("group_visibilityradius"))
        	((EditText) findViewById(R.id.edittext_visibilityradius)).setText(String.valueOf(extras.getInt("group_visibilityradius")));
        if (extras.containsKey("group_joinradius"))
        	((EditText) findViewById(R.id.edittext_joinradius)).setText(String.valueOf(extras.getInt("group_joinradius")));
                
        if (extras.containsKey("group_starttime")) {
        	Date date = new Date(extras.getLong("group_starttime"));
        	CheckBox cb = ((CheckBox) findViewById(R.id.checkbox_starttime));
        	cb.setChecked(true);
        	cb.setText(date.getDate()+"."+(date.getMonth()+1)+"."+(date.getYear()+1900));     	     	
        	datePickerDialogStartTime.updateDate(date.getYear()+1900, date.getMonth(), date.getDate());
        	((Button) findViewById(R.id.button_pickDate_starttime)).setEnabled(true);       	
        }
        if (extras.containsKey("group_endtime")) {
        	Date date = new Date(extras.getLong("group_endtime"));
        	CheckBox cb = ((CheckBox) findViewById(R.id.checkbox_endtime));
        	cb.setChecked(true);
        	cb.setText(date.getDate()+"."+(date.getMonth()+1)+"."+(date.getYear()+1900));     	     	
        	datePickerDialogEndTime.updateDate(date.getYear()+1900, date.getMonth(), date.getDate());
        	((Button) findViewById(R.id.button_pickDate_endtime)).setEnabled(true);       	
        }
        if (extras.containsKey("group_joinstarttime")) {
        	Date date = new Date(extras.getLong("group_joinstarttime"));
        	CheckBox cb = ((CheckBox) findViewById(R.id.checkbox_startjointime));
        	cb.setChecked(true);
        	cb.setText(date.getDate()+"."+(date.getMonth()+1)+"."+(date.getYear()+1900));     	     	
        	datePickerDialogStartJoinTime.updateDate(date.getYear()+1900, date.getMonth(), date.getDate());
        	((Button) findViewById(R.id.button_pickDate_startjointime)).setEnabled(true);       	
        }
        if (extras.containsKey("group_joinendtime")) {
        	Date date = new Date(extras.getLong("group_joinendtime"));
        	CheckBox cb = ((CheckBox) findViewById(R.id.checkbox_endjointime));
        	cb.setChecked(true);
        	cb.setText(date.getDate()+"."+(date.getMonth()+1)+"."+(date.getYear()+1900));     	     	
        	datePickerDialogEndJoinTime.updateDate(date.getYear()+1900, date.getMonth(), date.getDate());
        	((Button) findViewById(R.id.button_pickDate_endjointime)).setEnabled(true);       	
        }        
        	
        if (extras.containsKey("group_privacy")) {
        	String extraPrivacy = extras.getString("group_privacy");
        	String[] array = getResources().getStringArray(R.array.group_privacy);
        	for (int i=0; i<array.length; i++)
        		if (extraPrivacy.equals(array[i])) {
        			((Spinner) findViewById(R.id.spinner_privacy)).setSelection(i);
        			break;
        		}
        }
        if (extras.containsKey("group_link"))
        	((EditText) findViewById(R.id.edittext_link)).setText(extras.getString("group_link"));    
    }
     
    private DatePickerDialog datePickerDialogStartTime, datePickerDialogEndTime,
    	datePickerDialogStartJoinTime, datePickerDialogEndJoinTime;
    
    @Override
	protected Dialog onCreateDialog(int id) {
	    switch (id) {
	    case STARTTIME_DIALOG_ID:	    	
	        return datePickerDialogStartTime;
	    case ENDTIME_DIALOG_ID:
	        return datePickerDialogEndTime;
	    case STARTJOINTIME_DIALOG_ID:
	    	return datePickerDialogStartJoinTime;
	    case ENDJOINTIME_DIALOG_ID:
	        return datePickerDialogEndJoinTime;
	    }
	    return null;
	}
	
    
    // HANDLER & LISTENER //
    
        
	// the callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener dateSetListenerStartTime =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, 
                                      int monthOfYear, int dayOfMonth) {
                	((CheckBox) (findViewById(R.id.checkbox_starttime)))
                			.setText(
                					new StringBuilder()
                					.append(dayOfMonth).append(".")
                					// Month is 0 based so add 1
                					.append(monthOfYear + 1).append(".")
                                    .append(year).append(" "));
                	startTime = (new Date(year-1900, monthOfYear, dayOfMonth)).getTime();
                }
    };
    private DatePickerDialog.OnDateSetListener dateSetListenerEndTime =
        new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, 
                                  int monthOfYear, int dayOfMonth) {
            	((CheckBox) (findViewById(R.id.checkbox_endtime)))
            			.setText(
            					new StringBuilder()
            					.append(dayOfMonth).append(".")
            					// Month is 0 based so add 1
            					.append(monthOfYear + 1).append(".")
                                .append(year).append(" "));
            	endTime = (new Date(year-1900, monthOfYear, dayOfMonth)).getTime();
            }
        };
    private DatePickerDialog.OnDateSetListener dateSetListenerStartJoinTime =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, 
                                      int monthOfYear, int dayOfMonth) {
                ((CheckBox) (findViewById(R.id.checkbox_startjointime)))
            			.setText(
            					new StringBuilder()
            					.append(dayOfMonth).append(".")
            					// Month is 0 based so add 1
            					.append(monthOfYear + 1).append(".")
                                .append(year).append(" "));
                startJoinTime = (new Date(year-1900, monthOfYear, dayOfMonth)).getTime();
                }
    };
    private DatePickerDialog.OnDateSetListener dateSetListenerEndJoinTime =
        new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, 
                                  int monthOfYear, int dayOfMonth) {
            	((CheckBox) (findViewById(R.id.checkbox_endjointime)))
            			.setText(
            					new StringBuilder()
            					.append(dayOfMonth).append(".")
            					// Month is 0 based so add 1
            					.append(monthOfYear + 1).append(".")
                                .append(year).append(" "));
            	endJoinTime = (new Date(year-1900, monthOfYear, dayOfMonth)).getTime();
            }
        };
	
    
//    /**  */
//	private Handler groupCreateResultHandler = new Handler() {
//		@Override
//		public void handleMessage(Message msg) {			
//			switch(msg.arg1) {
//			case ConstMXA.MSG_STATUS_ERROR :
//				makeToast("GroupCreate IQ Result: MSG_STATUS_ERROR");
//				break;
//			case ConstMXA.MSG_STATUS_SUCCESS :
//				//makeToast("Test IQ Result: MSG_STATUS_SUCCESS");
//				setResult(RESULT_OK);
//				groupCreateActivity.finish();
//				break;							
//			}
//		}
//	};
	
	/** Callback which is informed about arrival of a GroupCreateIQ. */
	private IXMPPIQCallback groupCreateCallback = new IXMPPIQCallback.Stub() {
		@Override
		public void processIQ(XMPPIQ iq) throws RemoteException {
			Log.i(TAG,"groupCreateCallback --> processIQ --> iqPacketId:"+iq.packetID);								
			XMPPBean b = Parceller.getInstance().convertXMPPIQToBean(iq);
			Log.i(TAG,"groupCreateCallback --> processIQ --> beanPacketId:"+b.getId());		
			if (b instanceof GroupCreateBean) {
				xmppService.unregisterIQCallback(groupCreateCallback, GroupCreateBean.CHILD_ELEMENT, GroupCreateBean.NAMESPACE);
				GroupCreateBean bb = (GroupCreateBean) b;
				if (b.getType() == XMPPBean.TYPE_RESULT) {
					if (groupId==null)
						textToShowInToast="Successfully created the group.";
					else
						textToShowInToast="Successfully updated the group.";
					showToastHandler.sendEmptyMessage(0);
					setResult(RESULT_OK);
					groupCreateActivity.finish();
					
				} else if (b.getType() == XMPPBean.TYPE_ERROR) {
					Log.e(TAG, "GroupCreateBean type=ERROR arrived. IQ-Payload:" + iq.payload);
					Log.e(TAG, "GroupCreateBean type=ERROR arrived. Error type: " + bb.errorType);
					Log.e(TAG, "GroupCreateBean type=ERROR arrived. Error condition: " + bb.errorCondition);
					Log.e(TAG, "GroupCreateBean type=ERROR arrived. Error text: " + bb.errorText);
					textToShowInToast="ERROR: " + bb.errorText;
					showToastHandler.sendEmptyMessage(0);					
				}
			}
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
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
	}
	
	
    
	
}
