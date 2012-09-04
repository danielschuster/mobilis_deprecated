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
package de.tudresden.inf.rn.mobilis.media.views;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.os.RemoteException;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import de.tudresden.inf.rn.mobilis.media.ConstMMedia;
import de.tudresden.inf.rn.mobilis.media.R;
import de.tudresden.inf.rn.mobilis.media.parcelables.TransferParcel;
import de.tudresden.inf.rn.mobilis.media.services.ITransferService;
import de.tudresden.inf.rn.mobilis.mxa.services.parcelable.FileTransfer;

public class TransferAdapter implements ListAdapter {
		
	private List<TransferParcel> transferParcels;
	private List<DataSetObserver> dataSetObservers;
	private Context context;
	
	public TransferAdapter(Context c) {
		this.context = c;
		this.transferParcels = new LinkedList<TransferParcel>();
		this.dataSetObservers = new LinkedList<DataSetObserver>();
	}
	
	public void fillWithItems(ITransferService service, int direction) throws RemoteException {
		int ids[] = service.getIds(direction);
		List<TransferParcel> infos = this.transferParcels;
		infos.clear();
		for (int id: ids) {
			TransferParcel p = service.getTransferParcel(id);
			infos.add(p);
		}
		this.notifyDataSetChanged();
	}
	
	public boolean areAllItemsEnabled() {
		return true;
	}

	public boolean isEnabled(int position) {
		return true;
	}

	public int getCount() {
		return this.transferParcels.size();
	}

	public TransferParcel getItem(int position) {
		return this.transferParcels.get(position);
	}

	public long getItemId(int position) {
		return this.transferParcels.get(position).id;
	}

	public int getItemViewType(int position) {
		return 0;
	}

	public int getViewTypeCount() {
		return 1;
	}

	public boolean hasStableIds() {
		return false;
	}

	public boolean isEmpty() {
		return this.transferParcels.size() == 0;
	}
	
	public View getView(int position, View target, ViewGroup parentView) {
		Context context = this.context;
		Resources resources = context.getResources();
		TransferParcel source = this.getItem(position);
		FileTransfer sourceFile = source.xmppFile;
		TransferAdapter.ViewHolder targetHolder; 
		// recycle view
		if (target == null) {
			target = (ViewGroup)View.inflate(context, R.layout.media_transfer_row, null);
			targetHolder = new TransferAdapter.ViewHolder();
			targetHolder.filename = (TextView) target.findViewById(R.id.transfer_row_filename_text);
			targetHolder.toFrom = (TextView) target.findViewById(R.id.transfer_row_tofrom_text);
			targetHolder.jid = (TextView) target.findViewById(R.id.transfer_row_jid_text); 
			targetHolder.state = (TextView) target.findViewById(R.id.transfer_row_state_text);
			targetHolder.progress = (ProgressBar) target.findViewById(R.id.transfer_row_progress);
			target.setTag(targetHolder);
		} else {
			targetHolder = (TransferAdapter.ViewHolder) target.getTag();
		}
		// Set common values
		targetHolder.filename.setText(sourceFile.path);
		if (source.direction == ConstMMedia.enumeration.DIRECTION_OUT) {
			targetHolder.toFrom.setText(resources.getString(R.string.to_label));
			targetHolder.jid.setText(sourceFile.to);
		} else if (source.direction == ConstMMedia.enumeration.DIRECTION_IN) {
			targetHolder.toFrom.setText(resources.getString(R.string.from_label));
			targetHolder.jid.setText(sourceFile.from);
		}
		// Set values depending on state
		String pattern, text;
		switch (source.state) {
			case ConstMMedia.enumeration.STATE_REQUESTED:
				targetHolder.progress.setVisibility(View.VISIBLE);
				targetHolder.progress.setIndeterminate(true);
				targetHolder.state.setText(
						resources.getString(R.string.transfer_state_asking));
				break;
			case ConstMMedia.enumeration.STATE_STANDBY:
			case ConstMMedia.enumeration.STATE_INITIATED:
			case ConstMMedia.enumeration.STATE_NEGOTIATED:
				targetHolder.progress.setVisibility(View.VISIBLE);
				targetHolder.progress.setIndeterminate(true);
				targetHolder.state.setText(
						resources.getString(R.string.transfer_state_waiting));
				break;
			case ConstMMedia.enumeration.STATE_INPROGRESS:
				targetHolder.progress.setVisibility(View.VISIBLE);
				targetHolder.progress.setIndeterminate(false);
				targetHolder.progress.setMax(100);
				targetHolder.progress.setProgress((int)Math.ceil(
						(double)source.bytesTransferred / sourceFile.size * 100));
				pattern = resources.getString(R.string.transfer_state_ongoing);
				text = String.format(pattern,
						source.bytesTransferred / 1024,
						sourceFile.size / 1024);
				targetHolder.state.setText(text);
				break;
			case ConstMMedia.enumeration.STATE_FAILED:
				targetHolder.progress.setVisibility(View.GONE);
				pattern = resources.getString(R.string.transfer_state_failed);
				targetHolder.state.setText(
						resources.getString(R.string.transfer_state_failed));
				break;
			case ConstMMedia.enumeration.STATE_FINISHED:
				targetHolder.progress.setVisibility(View.GONE);
				targetHolder.state.setText(
						resources.getString(R.string.transfer_state_finished));
				break;
		}
		return target;
	}
	
	public void registerDataSetObserver(DataSetObserver observer) {
		this.dataSetObservers.add(observer);
	}
	
	public void unregisterDataSetObserver(DataSetObserver observer) {
		this.dataSetObservers.remove(observer);
	}
	
	private void updateMediaTransferParcel(TransferParcel info) {
		List<TransferParcel> infos = this.transferParcels;
		int infosSize = transferParcels.size();
		int infoId = info.id;
		for (int infoIndex = 0; infoIndex < infosSize; infoIndex++) {
			if (infos.get(infoIndex).id == infoId) {
				infos.set(infoIndex, info);
				return;
			}
		}
		infos.add(0, info);
	}
	
	public void notifyDataSetChanged() {
		for (DataSetObserver observer: this.dataSetObservers)
			observer.onChanged();
	}

	public void notifyDataSetChanged(TransferParcel info) {
		this.updateMediaTransferParcel(info);
		this.notifyDataSetChanged();
	}
	
	public void notifyDataSetInvalidated() {
		for (DataSetObserver observer: this.dataSetObservers)
			observer.onInvalidated();
	}
	
	private static class ViewHolder {
		public TextView filename;
		public TextView toFrom;
		public TextView jid; 
		public TextView state;
		public ProgressBar progress;
	}
	
}
