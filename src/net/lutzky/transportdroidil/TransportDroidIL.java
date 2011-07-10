package net.lutzky.transportdroidil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class TransportDroidIL extends Activity {
	private static final String TAG = "TransportDroidIL"; 
	
	private String lastResult;
	private Exception lastException;

	private void setButtonsEnabled(boolean enabled) {
		QueryView queryView = (QueryView) findViewById(R.id.queryview);

		queryView.setButtonsEnabled(enabled);
	}

	private void updateResultText(String result) {
		SharedPreferences.Editor editor = getPreferences(0).edit();
		editor.putString("Result", result);
		editor.commit();
		TextView tv = (TextView)findViewById(R.id.query_result);
		tv.setText(result);
	}

	private void runQuery(final BusGetter bg) {
		final Handler mHandler = new Handler();
		final ProgressDialog dialog = ProgressDialog.show(this, "",
				getString(R.string.please_wait));

		final Runnable mUpdateResults = new Runnable() {
			@Override
			public void run() {
				updateResultText(lastResult);
				dialog.hide();
				setButtonsEnabled(true);
			}
		};

		final Runnable mShowError = new Runnable() {
			@Override
			public void run() {
				String exceptionText = String.format(getString(R.string.error),
						lastException);

				dialog.hide();

				Toast toast = Toast.makeText(getApplicationContext(),
						exceptionText, Toast.LENGTH_LONG);
				toast.show();

				setButtonsEnabled(true);
			}
		};

		setButtonsEnabled(false);

		QueryView queryView = (QueryView) findViewById(R.id.queryview);
		final String query = queryView.getQueryString();
		
		Log.d(TAG, "Querying: " + query);

		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					bg.runQuery(query);
					lastResult = bg.getFilteredResult();
					mHandler.post(mUpdateResults);
				} catch (Exception e) {
					lastException = e;
					mHandler.post(mShowError);
				}
			}
		};

		t.start();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		QueryView queryView = (QueryView) findViewById(R.id.queryview);
		updateGoButton();
		queryView.loadPersistentState(getPreferences(0));
		queryView.setOnSearchButtonClickListener(new OnSearchButtonClickListener() {
			@Override
			public void onSearchButtonClick(View source, int provider) {
				if (provider == R.id.submit_egged)
					runQuery(new EggedGetter());
				else if (provider == R.id.submit_busgovil)
					runQuery(new BusGovIlGetter());
					
			}
		});

		TextView tvQueryResult = (TextView)findViewById(R.id.query_result);
		tvQueryResult.setText(getPreferences(0).getString("Result", ""));
	}
	
	@Override
	protected void onPause() {
		QueryView queryView = (QueryView) findViewById(R.id.queryview);
		queryView.savePersistentState(getPreferences(0));
		super.onPause();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.preferences:
			openPreferences();
			return true;
		}
		return false;
	}

	private void openPreferences() {
		Intent intent = new Intent(this, Preferences.class);
		startActivityForResult(intent, 0);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		updateGoButton();
	}

	private void updateGoButton() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String provider = settings.getString("provider", "mot");
		Log.d(TAG, "Got provider=" + provider + ", updating query view.");
		getQueryView().setProvider(provider);
	}

	private QueryView getQueryView() {
		return (QueryView) findViewById(R.id.queryview);
	}
}