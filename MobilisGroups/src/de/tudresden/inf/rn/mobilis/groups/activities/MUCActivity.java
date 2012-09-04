package de.tudresden.inf.rn.mobilis.groups.activities;

import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import de.tudresden.inf.rn.mobilis.groups.R;
import de.tudresden.inf.rn.mobilis.mxa.IXMPPService;
import de.tudresden.inf.rn.mobilis.mxa.MXAController;
import de.tudresden.inf.rn.mobilis.mxa.ConstMXA.MessageItems;
import de.tudresden.inf.rn.mobilis.mxa.parcelable.XMPPMessage;
import de.tudresden.inf.rn.mobilis.mxa.services.multiuserchat.IMultiUserChatService;
import de.tudresden.inf.rn.mobilis.xmpp.beans.XMPPUtil;

public class MUCActivity extends Activity{
	
	/** Identifier for the Log outputs **/
	private static final String TAG = "MUCActivity";

	private ListView lvMsgHistory;
	private EditText etEditor;
	private Button btnSend;

	private IXMPPService xmppService;
	private IMultiUserChatService mucService;
	
	/** Cursor to point on the history messages **/
	private Cursor msgCursor;
	
	/** Delivered RoomID from the server */
	private String mucRoomID;
	private String groupId;
	
	/**
	 * Constructor for the Activity
	 * @param savedInstanceState
	 */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_muc);
		
		/** Get the chatID from the intent extras */
		Bundle extras = this.getIntent().getExtras(); 
		groupId = uriToGroupId(extras.getString("group-id"));
		//TODO: Get from Settings 
		mucRoomID = groupId + "@conference.xhunt"; 
		
		/** Get the XMPP Service */
		xmppService = MXAController.get().getXMPPService();
		/** Get the MultiUserChat Service */
		boolean joinRoomResult;
		try {
			mucService = xmppService.getMultiUserChatService();
			joinRoomResult = mucService.joinRoom(mucRoomID, "");
			Log.i(TAG, "joinRoomResult="+joinRoomResult);
		} catch (RemoteException e) {			
			e.printStackTrace();
			Log.e(TAG, "Could not get MultiUserChat Service");
		}		
		
		/** Set the Title of the Activity to the chatID of the MUC */
		setTitle(extras.getString("title"));
		
		/** Init the GUI elements and its controlls */
		initResourceRefs();
		
		initMessageHistory();
	}
	
	private String uriToGroupId(String uri) {
		if (uri==null || uri.equals("")) return null;		
		String[] s = uri.split("#");
		if (s.length==2) {
			return s[1];
		}
		return null;
	}
	
	
	/***************************************	XMPP functions	 **************************************/
	
	/**
	 * Send the Message to the XMPP-Service
	 */
	private void sendMessage() {
		/** Create a new XMPPMessage */
		XMPPMessage xMsg = new XMPPMessage();
		/** Set the type of the message to GROUPCHAT */
		xMsg.type = XMPPMessage.TYPE_GROUPCHAT;
		/** Fill the body of the message with the text from the EditorText */
		xMsg.body = etEditor.getText().toString();
		
		try {
			/** Send the Message to the MXAController */
			mucService.sendGroupMessage(mucRoomID, xMsg);
		} catch (RemoteException e) {
			Log.e(TAG, "sendGroupMessage failed. Code: " + e.getMessage());
		}

		/** Clear the EditText */
		etEditor.setText("");
	}
	
	/**************************************************************************************************/
	
	
	/*************************************	Resource functions	***************************************/
	
	/**
	 * Initialize all UI elements from resources.
	 */
	private void initResourceRefs() {
		lvMsgHistory = (ListView) findViewById(R.id.list_view_history);
		etEditor = (EditText) findViewById(R.id.edit_text_editor);
		etEditor.setOnKeyListener(editTextKeyListener);
		etEditor.addTextChangedListener(editorWatcher);
		etEditor.requestFocus();
		btnSend = (Button) findViewById(R.id.button_send);
		btnSend.setEnabled(false);
		btnSend.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
					sendMessage();
			}
		});
	}
	
	private void initMessageHistory() {
		Log.i(TAG, "initMessageHistory");
		
		/** Get a cursor to all messages to the user */
//		String selection = "SUBJECT='"+groupId+"'";
		
		long now = (new Date()).getTime();
		String selection = "DATE_SENT>="+now;
//		String selection = "TYPE='groupchat'";
		msgCursor = getContentResolver().query(MessageItems.CONTENT_URI,
				null, selection, null, MessageItems.DEFAULT_SORT_ORDER);
		/** Start to manage the cursor */
		startManagingCursor(msgCursor);
		
		/** Set up a Listadapter to handle the history messages */
		MUCListAdapter adapter = new MUCListAdapter(this,
				android.R.layout.simple_list_item_2,
				msgCursor,
				new String[] { MessageItems.BODY, MessageItems.SENDER },
				new int[] { android.R.id.text1, android.R.id.text2 });

		
		lvMsgHistory.setAdapter(adapter);
	}
	
	/**************************************************************************************************/
	
	
	/***************************************	Listeners	*******************************************/
	
	private final TextWatcher editorWatcher = new TextWatcher() {
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			/** Update the Send-Button, so that we only send a message, when there is something to send in the EditText **/
			if(etEditor.getText().length() > 0){
				btnSend.setEnabled(true);
			}
			else {
				btnSend.setEnabled(false);
			}
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	};
	
	/** KeyListener for listening on the Enter-Key**/
	private final OnKeyListener editTextKeyListener = new OnKeyListener() {
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if ((event.getAction() == KeyEvent.ACTION_DOWN)
					&& (keyCode == KeyEvent.KEYCODE_ENTER)
					&& !event.isShiftPressed()) {
				if (btnSend.isEnabled()) {
					sendMessage();
				}
				return true;
			} else {
				return false;
			}
		}
	};
	
	/**************************************************************************************************/

	
	/***************************************	InnerClasses	***************************************/
	
	private class MUCListAdapter extends SimpleCursorAdapter{

		/**
		 * Constructor for the ListAdapter
		 * @param context Context to bind
		 * @param layout List style
		 * @param c Cursor wich points on the data
		 * @param from Points on the columns in the database
		 * @param to Points on the views in the list
		 */
		public MUCListAdapter(Context context, int layout, Cursor c, String[] from,
				int[] to) {
			super(context, layout, c, from, to);
		}
		
		/**
		 * Binds the view and the cursors data and represent it to the context
		 * 
		 * We override the standard SimpleCursorAdapter so that we can modify the sender
		 * to the nickname of the player
		 * @param view View to bind on
		 * @param context Context for representation
		 * @param c Cursor wich points on the data
		 */
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			
			int id = cursor.getInt(cursor.getColumnIndex(MessageItems._ID));
			String sender2 = cursor.getString(cursor.getColumnIndex(MessageItems.SENDER));
			String recipient = cursor.getString(cursor.getColumnIndex(MessageItems.RECIPIENT));
			String subject = cursor.getString(cursor.getColumnIndex(MessageItems.SUBJECT));
			String body2 = cursor.getString(cursor.getColumnIndex(MessageItems.BODY));
			long dateSent = cursor.getLong(cursor.getColumnIndex(MessageItems.DATE_SENT));
			String type = cursor.getString(cursor.getColumnIndex(MessageItems.TYPE));
			String status = cursor.getString(cursor.getColumnIndex(MessageItems.STATUS));
			int read = cursor.getInt(cursor.getColumnIndex(MessageItems.READ));
			
			Date when = new Date(dateSent);
			
			//Log.i(TAG, "MUCListAdapter --> bindView --> _ID:"+id+" SENDER:"+sender2+" RECIPIENT:"+recipient+" SUBJECT:"+subject+" BODY:"+body2+" DATE_SENT:"+dateSent+" TYPE:"+type+" STATUS:"+status+" READ:"+read);
			
			/** Get the current body-entry **/
			String body = cursor.getString(cursor.getColumnIndex(MessageItems.BODY));			
			/** Get the current sender-entry and cut out the chatID**/
			String sender = cursor.getString(cursor.getColumnIndex(MessageItems.SENDER)).replace(mucRoomID, "");
			
			/** If the sender wasn't the chat itself, we show the nickname of the sender
			 *  else we let the sender-text empty **/
			if(sender.length() > 0){
				sender = sender.substring(1);
			}
			
			/** Catch the TextView's wich represents the sender and the body **/
			TextView tv_body = (TextView)view.findViewById(android.R.id.text1);
			TextView tv_sender = (TextView)view.findViewById(android.R.id.text2);
			
			String hours, minutes, seconds;
			if (when.getHours()<10) hours = "0"+when.getHours();
			else hours = ""+when.getHours();
			if (when.getMinutes()<10) minutes = "0"+when.getMinutes();
			else minutes = ""+when.getMinutes();
			if (when.getSeconds()<10) seconds = "0"+when.getSeconds();
			else seconds = ""+when.getSeconds();
			
			/** Bind the text to the corresponding cursor-entry**/
			tv_body.setText(body);
			tv_sender.setText(XMPPUtil.jidWithoutRessource(sender)+" - "+
					when.getDate()+"."+(when.getMonth()+1)+"."+(when.getYear()+1900)+" "+hours+":"+minutes+":"+seconds);
		}

	}
	
	/**************************************************************************************************/
	
}


