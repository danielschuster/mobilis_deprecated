package de.tudresden.inf.rn.mobilis.android;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import de.tudresden.inf.rn.mobilis.android.util.Const;

public class ChatActivity extends Activity {

	// fields
	private ArrayList<String> messages = new ArrayList<String>();
	private Handler mHandler = new Handler();
	private CallbackIntentReceiver cir;

	// Views
	private ListView lstMessages;
	private EditText edtMessage;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.chatactivity);
		initComponents();
		setListAdapter();
	}
	
	@Override
	public void onStart() {
	    super.onStart();
	    initIntentReceiver();
	}
	
	@Override
	public void onStop() {
	    super.onStop();
	    unregisterReceiver(cir);
	}

	private void initComponents() {
		lstMessages = (ListView) findViewById(R.id.chat_lst_messages);
		edtMessage = (EditText) findViewById(R.id.chat_edt_message);

		lstMessages.setDivider(null);
		lstMessages.setFocusableInTouchMode(false);
		lstMessages.setItemsCanFocus(false);

		edtMessage.setOnKeyListener(mEdtKeyListener);
	}

	private void initIntentReceiver() {
		cir = new CallbackIntentReceiver();
		registerReceiver(cir, new IntentFilter(
				Const.INTENT_PREFIX + "callback.groupchat"));
	}

	// Create an anonymous class to act as a key listener
	private OnKeyListener mEdtKeyListener = new OnKeyListener() {
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_CENTER:
				case KeyEvent.KEYCODE_ENTER:
					sendMessage();
					return true;
				}
			}
			return false;
		}
	};

	public void handleGroupChatCallback(String from, String message) {
		messages.add(from + ": " + message);
		setListAdapter();
	}

	/**
	 * Sends a message intent to the group chat service.
	 * 
	 * @param message
	 */
	private void callGroupChatMessage(String message) {
		Intent i = new Intent(
				Const.INTENT_PREFIX + "servicecall.groupchat");
		i
				.putExtra(
						Const.INTENT_PREFIX + "servicecall.groupchat.message",
						message);
		sendBroadcast(i);
	}

	private void sendMessage() {
		callGroupChatMessage(edtMessage.getText().toString());
		edtMessage.setText(null);
	}

	private void setListAdapter() {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.chatlistitem, messages);
		lstMessages.setAdapter(adapter);
		// TODO: scroll automatically
	}

	/**
	 * Listens to Intents with callback actions of this class.
	 * 
	 * @author sealpuppy
	 * 
	 */
	private class CallbackIntentReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action
					.equals(Const.INTENT_PREFIX + "callback.groupchat")) {
				String from = intent
						.getStringExtra(Const.INTENT_PREFIX + "callback.groupchat.from");
				String message = intent
						.getStringExtra(Const.INTENT_PREFIX + "callback.groupchat.message");
				ChatActivity.this.handleGroupChatCallback(from, message);
			}
		}
	}
}
