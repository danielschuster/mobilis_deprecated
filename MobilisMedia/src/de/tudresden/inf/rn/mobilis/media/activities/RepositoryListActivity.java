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

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import de.tudresden.inf.rn.mobilis.media.ConstMMedia;
import de.tudresden.inf.rn.mobilis.media.R;
import de.tudresden.inf.rn.mobilis.media.activities.RepositorySubActivityHandler.SubActivityListener;
import de.tudresden.inf.rn.mobilis.media.parcelables.ConditionParcel;
import de.tudresden.inf.rn.mobilis.media.parcelables.RepositoryItemParcel;
import de.tudresden.inf.rn.mobilis.media.views.RepositoryListAdapter;
import de.tudresden.inf.rn.mobilis.media.views.RosterSpinnerAdapter;

public class RepositoryListActivity extends Activity
		implements SubActivityListener, OnItemSelectedListener, OnClickListener, OnItemClickListener {
	
	private static final int DIALOG_CONDITION_DATE_TAKEN_GE = 0;
	private static final int DIALOG_CONDITION_DATE_TAKEN_LE = 1;
	private static final int DIALOG_CONDITION_TIME_TAKEN_GE = 2;
	private static final int DIALOG_CONDITION_TIME_TAKEN_LE = 3;
	
	private Button buttonConditionDateTakenGe;
	private Button buttonConditionDateTakenLe;
	private Button buttonConditionTimeTakenGe;
	private Button buttonConditionTimeTakenLe;
	private Date conditionTakenGe; 
	private Date conditionTakenLe; 
	
	private Spinner spinnerConditionOwner;
	private String  conditionOwner;
	
	private RepositoryListAdapter listAdapter; 
	
	private RepositorySubActivityHandler subActivityHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Calendar calendar = Calendar.getInstance();
		this.conditionTakenGe = this.conditionTakenLe = calendar.getTime();
		this.conditionOwner = null;
		this.setContentView(R.layout.repository_list);
		this.setupSpinnerConditionOwner();
		this.setupButtonConditionDatetaken();
		this.setupListView();
	}
	
	private void setupSpinnerConditionOwner() {
		Spinner s = this.spinnerConditionOwner
				= (Spinner) this.findViewById(R.id.condition_owner);
		s.setAdapter(new RosterSpinnerAdapter(new Handler(), this,
				android.R.layout.simple_spinner_item,
				android.R.layout.simple_spinner_dropdown_item,
				true
			));
		s.setOnItemSelectedListener(this);
	}
	
	private void setupButtonConditionDatetaken() {
		final Calendar c = Calendar.getInstance();
		Button[] bb = new Button[4];
		bb[1] = this.buttonConditionDateTakenGe = (Button) this.findViewById(R.id.condition_datetaken_ge);
		bb[3] = this.buttonConditionTimeTakenGe = (Button) this.findViewById(R.id.condition_timetaken_ge);
		bb[0] = this.buttonConditionDateTakenLe = (Button) this.findViewById(R.id.condition_datetaken_le);
		bb[2] = this.buttonConditionTimeTakenLe = (Button) this.findViewById(R.id.condition_timetaken_le);
		for (Button b: bb) b.setOnClickListener(this);
		c.setTime(this.conditionTakenGe);
		this.writeDateToButton(c, this.buttonConditionDateTakenGe);
		this.writeTimeToButton(c, this.buttonConditionTimeTakenGe);
		c.setTime(this.conditionTakenLe);
		this.writeDateToButton(c, this.buttonConditionDateTakenLe);
		this.writeTimeToButton(c, this.buttonConditionTimeTakenLe);
	}
	
	private void setupListView() {
		ListView lv = (ListView) this.findViewById(android.R.id.list);
		ListAdapter la = this.listAdapter = new RepositoryListAdapter(this);
		lv.setAdapter(la);
		lv.setOnItemClickListener(this);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		this.subActivityHandler = new RepositorySubActivityHandler(this.getIntent());
		this.subActivityHandler.setSubActivityListener(this);
		this.subActivityHandler.subActivityRegister();
	}
	
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		this.conditionOwner = (String) this.spinnerConditionOwner.getSelectedItem(); 
	}

	public void onNothingSelected(AdapterView<?> parent) {
		this.conditionOwner = null;
	}

	public void onClick(View sender) {
		if (sender == this.buttonConditionDateTakenLe)
			this.showDialog(RepositoryListActivity.DIALOG_CONDITION_DATE_TAKEN_LE);
		else if (sender == this.buttonConditionDateTakenGe)
			this.showDialog(RepositoryListActivity.DIALOG_CONDITION_DATE_TAKEN_GE);
		else if (sender == this.buttonConditionTimeTakenLe)
			this.showDialog(RepositoryListActivity.DIALOG_CONDITION_TIME_TAKEN_LE);
		else if (sender == this.buttonConditionTimeTakenGe)
			this.showDialog(RepositoryListActivity.DIALOG_CONDITION_TIME_TAKEN_GE);
	}
	
	public void onItemClick(AdapterView<?> view, View parentView, int position, long id) {
		this.subActivityHandler.subActivityDisplay(
				this.listAdapter.getItem(position)
			);
	}

		
	@Override
	protected Dialog onCreateDialog(int id) {
		final Calendar c = Calendar.getInstance();
		Dialog d;
		if (id == RepositoryListActivity.DIALOG_CONDITION_DATE_TAKEN_GE
				|| id == RepositoryListActivity.DIALOG_CONDITION_DATE_TAKEN_LE) {
			if (id == RepositoryListActivity.DIALOG_CONDITION_DATE_TAKEN_GE)
				c.setTime(this.conditionTakenGe);
			else
				c.setTime(this.conditionTakenLe);
			d = new DatePickerDialog(this,
					new RepositoryListActivity.ConditionDateTakenSetListener(id),
					c.get(Calendar.YEAR),
					c.get(Calendar.MONTH),
					c.get(Calendar.DAY_OF_MONTH));
		} else if (id == RepositoryListActivity.DIALOG_CONDITION_TIME_TAKEN_GE
					|| id == RepositoryListActivity.DIALOG_CONDITION_TIME_TAKEN_LE) {
			if (id == RepositoryListActivity.DIALOG_CONDITION_DATE_TAKEN_GE)
				c.setTime(this.conditionTakenGe);
			else
				c.setTime(this.conditionTakenLe);
			d = new TimePickerDialog(this, 
					new RepositoryListActivity.ConditionTimeTakenSetListener(id),
					c.get(Calendar.HOUR_OF_DAY),
					c.get(Calendar.MINUTE),
					true);
		} else
			d = super.onCreateDialog(id);
		return d;
	}
	
	protected ConditionParcel evaluateCondition() {
		final Calendar calendar = Calendar.getInstance();
		ConditionParcel[] subconditions;
		if (this.conditionOwner == null) {
			subconditions = new ConditionParcel[2];
		} else {
			subconditions = new ConditionParcel[3];
			subconditions[2] = new ConditionParcel();
			subconditions[2].key   = ConstMMedia.database.SLICE_OWNER;
			subconditions[2].op    = ConditionParcel.OP_EQ;
			subconditions[2].value = this.conditionOwner;
		}
		calendar.setTime(this.conditionTakenGe);
		subconditions[0] = new ConditionParcel();
		subconditions[0].key   = ConstMMedia.database.SLICE_TAKEN;
		subconditions[0].op    = ConditionParcel.OP_GE;
		subconditions[0].value = String.valueOf(calendar.getTimeInMillis());
		calendar.setTime(this.conditionTakenLe);
		subconditions[1] = new ConditionParcel();
		subconditions[1].key   = ConstMMedia.database.SLICE_TAKEN;
		subconditions[1].op    = ConditionParcel.OP_LE;
		subconditions[1].value = String.valueOf(calendar.getTimeInMillis());
		ConditionParcel condition = new ConditionParcel();
		condition.op         = ConditionParcel.OP_AND;
		condition.conditions = subconditions;
		return condition;
	}
	
	
	@Override
	protected void onStop() {
		super.onStop();
		this.subActivityHandler.subActivityUnregister();
	}

	public void onSubActivityHide() { }

	public void onSubActivityOutdate() {
		this.subActivityHandler.subActivityUpdate(this.evaluateCondition());
	}

	public void onSubActivityShow() {
		this.subActivityHandler.subActivityUpdate(this.evaluateCondition());
	}

	public void onSubActivityUpdate(RepositoryItemParcel[] repositoryItems) {
		this.listAdapter.setRepositoryItems(repositoryItems);
	}

	public void onSubActivityUpdateError() { }
	
	private class ConditionDateTakenSetListener implements OnDateSetListener {
		private int which;
		public ConditionDateTakenSetListener(int which) {
			this.which = which;
		}
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			final RepositoryListActivity owner = RepositoryListActivity.this;
			final Calendar calendar = Calendar.getInstance();
			switch (this.which) {
			case RepositoryListActivity.DIALOG_CONDITION_DATE_TAKEN_GE:
				calendar.setTime(owner.conditionTakenGe);
				calendar.set(year, monthOfYear, dayOfMonth);
				owner.writeDateToButton(calendar, owner.buttonConditionDateTakenGe);
				owner.conditionTakenGe = calendar.getTime();
				break;
			case RepositoryListActivity.DIALOG_CONDITION_DATE_TAKEN_LE:
				calendar.setTime(owner.conditionTakenLe);
				calendar.set(year, monthOfYear, dayOfMonth);
				owner.writeDateToButton(calendar, owner.buttonConditionDateTakenLe);
				owner.conditionTakenLe = calendar.getTime();
				break;
			}
			owner.subActivityHandler.subActivityUpdate(owner.evaluateCondition());
		}
	}
	
	private void writeDateToButton(Calendar c, Button b) {
		int d = c.get(Calendar.DAY_OF_MONTH);
		int m = c.get(Calendar.MONTH)+1;
		int y = c.get(Calendar.YEAR);
		b.setText(new StringBuilder(8)
				.append(d < 10 ? "0" : "").append(d)
				.append('.')
				.append(m < 10 ? "0" : "").append(m)
				.append('.')
				.append(y%100<10 ? "0" : "").append(y % 100)
				.toString()
			);
	}
	
	private class ConditionTimeTakenSetListener implements OnTimeSetListener {
		private int which;
		public ConditionTimeTakenSetListener(int which) {
			this.which = which;
		}
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			final RepositoryListActivity owner = RepositoryListActivity.this;
			final Calendar calendar = Calendar.getInstance();
			switch (this.which) {
			case RepositoryListActivity.DIALOG_CONDITION_TIME_TAKEN_GE:
				calendar.setTime(owner.conditionTakenGe);
				calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				calendar.set(Calendar.MINUTE,      minute);
				owner.writeTimeToButton(calendar, owner.buttonConditionTimeTakenGe);
				owner.conditionTakenGe = calendar.getTime();
				break;
			case RepositoryListActivity.DIALOG_CONDITION_TIME_TAKEN_LE:
				calendar.setTime(owner.conditionTakenLe);
				calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				calendar.set(Calendar.MINUTE,      minute);
				owner.writeTimeToButton(calendar, owner.buttonConditionTimeTakenLe);
				owner.conditionTakenLe = calendar.getTime();
				break;
			}
			owner.subActivityHandler.subActivityUpdate(owner.evaluateCondition());
		}
		
	}
	
	private void writeTimeToButton(Calendar c, Button b) {
		int h = c.get(Calendar.HOUR_OF_DAY);
		int m = c.get(Calendar.MINUTE);
		b.setText(new StringBuilder(8)
				.append(h<10 ? "0" : "").append(h)
				.append(':')
				.append(m<10 ? "0" : "").append(m)
				.toString()
			);
	}
}
