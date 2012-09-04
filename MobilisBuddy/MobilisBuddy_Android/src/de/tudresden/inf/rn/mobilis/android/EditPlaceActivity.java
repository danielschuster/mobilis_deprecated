package de.tudresden.inf.rn.mobilis.android;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import de.tudresden.inf.rn.mobilis.android.util.Const;

/**
 * Implements a simple activity to create and edit places.
 * 
 * @author Istvan
 * 
 */
public class EditPlaceActivity extends Activity implements OnClickListener {

	// views
	private Button btnSave;
	private Button btnCancel;
	private EditText edtTitle;
	private EditText edtAddress;
	private EditText edtNotes;

	// fields
	private Location mLocation;

	/**
	 * Reads out the location parameter.
	 */
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.editplaceactivity);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mLocation = (Location) extras
					.getParcelable(Const.INTENT_PREFIX + "createplace.location");
		}

		initComponents();
	}

	private void initComponents() {
		btnSave = (Button) findViewById(R.id.editplace_btn_save);
		btnCancel = (Button) findViewById(R.id.editplace_btn_cancel);
		edtTitle = (EditText) findViewById(R.id.editplace_edt_title);
		edtAddress = (EditText) findViewById(R.id.editplace_edt_address);
		edtNotes = (EditText) findViewById(R.id.editplace_edt_notes);

		btnSave.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
		
		Geocoder gc = new Geocoder(this);
		Address ad;
		try {
			ad = gc.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 2).get(0);
			edtAddress.setText(ad.getThoroughfare());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Calls the PlacesManagementService in order to save and publish the edited
	 * place.
	 */
	private void savePlace() {
		String title = edtTitle.getText().toString();
		String address = edtAddress.getText().toString();
		String notes = edtNotes.getText().toString();

		callPublishPlace(mLocation, title, address, notes);

		setResult(RESULT_OK);
		finish();
	}

	/**
	 * Sends a place intent to the places management service.
	 * 
	 * @param loc
	 * @param title
	 * @param address
	 * @param notes
	 */
	private void callPublishPlace(Location loc, String title, String address,
			String notes) {
		Intent i = new Intent(
				Const.INTENT_PREFIX + "servicecall.createplace");
		i
				.putExtra(
						Const.INTENT_PREFIX + "servicecall.createplace.location",
						loc);
		i
				.putExtra(
						Const.INTENT_PREFIX + "servicecall.createplace.title",
						title);
		i
				.putExtra(
						Const.INTENT_PREFIX + "servicecall.createplace.address",
						address);
		i
				.putExtra(
						Const.INTENT_PREFIX + "servicecall.createplace.notes",
						notes);
		sendBroadcast(i);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.editplace_btn_save:
			savePlace();
			break;
		case R.id.editplace_btn_cancel:
			break;
		}
	}
}
