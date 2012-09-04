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

import java.util.Calendar;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import de.tudresden.inf.rn.mobilis.media.ConstMMedia;
import de.tudresden.inf.rn.mobilis.media.R;
import de.tudresden.inf.rn.mobilis.media.parcelables.RepositoryItemParcel;

public class RepositoryListAdapter extends BaseAdapter {

	private RepositoryItemParcel[] repositoryItems;
	private Context context;
	
	public RepositoryListAdapter(Context c) {
		this.context = c;
		this.repositoryItems = new RepositoryItemParcel[0];
	}
	
	public void setRepositoryItems(RepositoryItemParcel[] parcels) {
		this.repositoryItems = parcels;
		this.notifyDataSetChanged();
	}
	
	public int getCount() {
		return this.repositoryItems.length;
	}

	public RepositoryItemParcel getItem(int position) {
		return this.repositoryItems[position];
	}

	public long getItemId(int position) {
		return position;
	}

	public static class ViewHolder {
		public TextView text1;
		public TextView text2;
	}
	
	public View getView(int position, View target, ViewGroup parent) {
		final Resources r = this.context.getResources();
		RepositoryListAdapter.ViewHolder holder = new RepositoryListAdapter.ViewHolder();
		if (target == null) {
			target = View.inflate(this.context, android.R.layout.simple_list_item_2, null);
			holder.text1 = (TextView) ((ViewGroup)target).findViewById(android.R.id.text1);
			holder.text2 = (TextView) ((ViewGroup)target).findViewById(android.R.id.text2);
			target.setTag(holder);
		} else
			holder = (RepositoryListAdapter.ViewHolder) target.getTag();
		// get slice values
		Map<String,String> itemSlices = this.repositoryItems[position].slices;
		String itemTitle = itemSlices.get(ConstMMedia.database.SLICE_TITLE);
		String itemOwner = itemSlices.get(ConstMMedia.database.SLICE_OWNER);
		String itemTaken = itemSlices.get(ConstMMedia.database.SLICE_TAKEN);
		// convert date taken value
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(itemTaken != null ? Long.parseLong(itemTaken) : 0);
		holder.text1.setText(itemTitle);
		holder.text2.setText(String.format(r.getString(R.string.repository_list_entry),
				itemOwner,
				c.get(Calendar.DAY_OF_MONTH),
				c.get(Calendar.MONTH)+1,
				c.get(Calendar.YEAR)%100,
				c.get(Calendar.HOUR_OF_DAY),
				c.get(Calendar.MINUTE)
			));
		return target;
	}

}
