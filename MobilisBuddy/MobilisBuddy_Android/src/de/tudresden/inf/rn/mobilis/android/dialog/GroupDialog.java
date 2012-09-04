package de.tudresden.inf.rn.mobilis.android.dialog;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import de.tudresden.inf.rn.mobilis.android.R;

public class GroupDialog extends Dialog implements OnItemSelectedListener {

	// views
	private EditText edtNewGroup;
	private Button btnJoin;
	private ListView lstGroups;
	private TextView txtGroupsEmpty;
	
	// members
	private View.OnClickListener mBtnClickListener;
	private ArrayList<String> mGroups;
	private int mSelectedGroupIndex;
	
	public GroupDialog(Context context, ArrayList<String> groups, View.OnClickListener btnClickListener) {
		super(context);
		
		mGroups = groups;
		mBtnClickListener = btnClickListener;
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		setContentView(R.layout.groupdialog);
		setCancelable(false);
		setTitle(R.string.groupdlg_title);
		
		initComponents();
	}
	
	private void initComponents() {
		edtNewGroup = (EditText) findViewById(R.id.groupdlg_edt_newgroup);
		btnJoin = (Button) findViewById(R.id.groupdlg_btn_join);
		lstGroups = (ListView) findViewById(R.id.groupdlg_lst_groups);
		txtGroupsEmpty = (TextView) findViewById(R.id.groupdlg_txt_groupsempty);
		
		lstGroups.setEmptyView(txtGroupsEmpty);
		lstGroups.setOnItemSelectedListener(this);
		
		btnJoin.setOnClickListener(mBtnClickListener);
		
		fillGroupsList();
	}
	
	public boolean isNewGroupRequested() {
		return !edtNewGroup.getText().toString().equals("");
	}
	
	public String getNewGroupName() {
		return edtNewGroup.getText().toString();
	}
	
	private void fillGroupsList() {
		ArrayAdapter<String> a = new ArrayAdapter<String>(this.getContext(), R.layout.groupdialog_item, mGroups);
		lstGroups.setAdapter(a);
	}
	
	public String getSelectedGroupName() {
		if (mGroups.size() == 0)
			return null;
		return mGroups.get(mSelectedGroupIndex);
	}

	/**
	 * Needed as the selection is lost after clicking the button.
	 */
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		mSelectedGroupIndex = lstGroups.getSelectedItemPosition();
		System.out.println(arg2);
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// not implemented
	}
}
