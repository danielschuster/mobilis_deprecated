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
package de.tudresden.inf.rn.mobilis.media.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;
import de.tudresden.inf.rn.mobilis.commons.views.AggregatedAdapter;
import de.tudresden.inf.rn.mobilis.commons.views.TitledAdapter;
import de.tudresden.inf.rn.mobilis.media.ConstMMedia;
import de.tudresden.inf.rn.mobilis.media.R;
import de.tudresden.inf.rn.mobilis.media.parcelables.TransferParcel;
import de.tudresden.inf.rn.mobilis.media.services.ITransferService;
import de.tudresden.inf.rn.mobilis.media.views.TransferAdapter;

public class TransferActivity extends ListActivity implements ServiceConnection, DialogInterface.OnClickListener {
	
	private final static int DIALOG_REQUEST = 1;
	
	private ITransferService service = null;
	private int clickedListItem = -1;
	private Messenger incomingMessenger;
	private Messenger outgoingMessenger;
	private TransferAdapter incomingAdapter;
	private TransferAdapter outgoingAdapter;
	private EditText requestFilenameEdit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.incomingMessenger = new Messenger(
			new TransferActivity.ListUpdateHandler(ConstMMedia.enumeration.DIRECTION_IN));
		this.outgoingMessenger = new Messenger(
				new TransferActivity.ListUpdateHandler(ConstMMedia.enumeration.DIRECTION_IN));
		this.createList();
	}
	
	public void createList() {
		Resources r = this.getResources();
		AggregatedAdapter adapter = new AggregatedAdapter();
		TitledAdapter incomingTitledAdapter = new TitledAdapter(this, r.getString(R.string.transfers_incoming));
		TitledAdapter outgoingTitledAdapter = new TitledAdapter(this, r.getString(R.string.transfers_outgoing));
		TransferAdapter incomingAdapter = this.incomingAdapter = new TransferAdapter(this);
		TransferAdapter outgoingAdapter = this.outgoingAdapter = new TransferAdapter(this);
		incomingTitledAdapter.setContentAdapter(incomingAdapter);
		outgoingTitledAdapter.setContentAdapter(outgoingAdapter);
		adapter.addAdapter(outgoingTitledAdapter);
		adapter.addAdapter(incomingTitledAdapter);
		this.setListAdapter(adapter);
		this.setContentView(R.layout.media_transfer_manager);
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (this.service == null)
			this.bindService(new Intent(ITransferService.class.getName()),
					this, Service.BIND_AUTO_CREATE);
		else
			this.connectAdapter();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		try {
			this.service.unregisterMediaTransferMessenger(this.incomingMessenger, ConstMMedia.enumeration.DIRECTION_IN);
			this.service.registerMediaTransferMessenger(this.outgoingMessenger, ConstMMedia.enumeration.DIRECTION_OUT);
		} catch (RemoteException e) {}
		if (this.service != null)
			this.unbindService(this);
	}

	public void onServiceConnected(ComponentName component, IBinder binder) {
		this.service = ITransferService.Stub.asInterface(binder);
		this.connectAdapter();
	}
	
	public void onServiceDisconnected(ComponentName component) {
		this.service = null;
	}
	
	private void connectAdapter() {
		ITransferService s = this.service;
		try {
			this.incomingAdapter.fillWithItems(s, ConstMMedia.enumeration.DIRECTION_IN);
			this.outgoingAdapter.fillWithItems(s, ConstMMedia.enumeration.DIRECTION_OUT);
			this.service.registerMediaTransferMessenger(this.incomingMessenger, ConstMMedia.enumeration.DIRECTION_IN);
			this.service.registerMediaTransferMessenger(this.outgoingMessenger, ConstMMedia.enumeration.DIRECTION_OUT);
		} catch (RemoteException e) {}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		TransferParcel info = (TransferParcel) this.getListAdapter().getItem(position);
		if (info.direction == ConstMMedia.enumeration.DIRECTION_IN
				&& info.state == ConstMMedia.enumeration.STATE_REQUESTED) {
			this.clickedListItem = position;
			this.showDialog(TransferActivity.DIALOG_REQUEST);
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int dialogId) {
		// Dialog for accepting media transfers
		Resources r = this.getResources();
		// get File transfer information
		if (dialogId == TransferActivity.DIALOG_REQUEST) {
			// set dialog text content
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
					FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			lp.leftMargin = lp.topMargin = lp.rightMargin = lp.bottomMargin = 2;
			EditText et = new EditText(this);
			et.setLayoutParams(lp);
			this.requestFilenameEdit = et;
			// build up dialog
			return new AlertDialog.Builder(this)
					.setTitle(r.getString(R.string.dialog_request_title))
					.setCancelable(true)
					.setView(et)
					.setMessage("")
					.setPositiveButton(r.getString(R.string.dialog_request_yes), this)
					.setNegativeButton(r.getString(R.string.dialog_request_no), this)
					.setNeutralButton(r.getString(R.string.dialog_request_cancel), this)
					.create();
		} else
			return super.onCreateDialog(dialogId);
	}
	
	@Override
	protected void onPrepareDialog(int dialogId, Dialog dialog) {
		super.onPrepareDialog(dialogId, dialog);
		Resources r = this.getResources();
		if (dialogId == TransferActivity.DIALOG_REQUEST) {
			TransferParcel info = (TransferParcel) this.getListAdapter().getItem(this.clickedListItem); 
			AlertDialog acceptDialog = (AlertDialog) dialog;
			acceptDialog.setMessage( String.format(r.getString(R.string.dialog_request_message), info.xmppFile.from) );
			this.requestFilenameEdit.setText( info.xmppFile.path ); 
		}
	}
		
	public void onClick(DialogInterface dialog, int which) {
		int id = (int)this.getListAdapter().getItemId(this.clickedListItem);
		boolean success = false;
		try {
			switch (which) {
			case DialogInterface.BUTTON_NEGATIVE:
				if (this.service != null)
					success = this.service.denyTransferFromJid(id);
				dialog.dismiss();
				break;
			case DialogInterface.BUTTON_POSITIVE:
				String filename = this.requestFilenameEdit.getText().toString();
				if (this.service != null)
					success = this.service.acceptTransferFromJid(filename, id);
				dialog.dismiss();
				break;
			case DialogInterface.BUTTON_NEUTRAL:
				dialog.cancel();
				break;
			}
		} catch (RemoteException e) { }
		if (!success)
			Toast.makeText(this, "Error! Lost connection to service!", Toast.LENGTH_SHORT);
	}
	
	public class ListUpdateHandler extends Handler {
		private int direction;

		public ListUpdateHandler(int direction) {
			this.direction = direction;
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == ConstMMedia.message.WHAT_TRANSFERSTATECHANGED) {
				TransferAdapter adapter = null;
				TransferParcel info = (TransferParcel) msg.getData().getParcelable(
						ConstMMedia.message.data.PAR_TRANSFER);
				if (this.direction == ConstMMedia.enumeration.DIRECTION_IN)
					adapter = TransferActivity.this.incomingAdapter;
				else if (this.direction == ConstMMedia.enumeration.DIRECTION_OUT)
					adapter = TransferActivity.this.outgoingAdapter;
				if (adapter != null)
					adapter.notifyDataSetChanged(info);
			}
		}
	}	
}
