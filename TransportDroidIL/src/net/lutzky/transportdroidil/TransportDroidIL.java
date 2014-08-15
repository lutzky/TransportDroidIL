package net.lutzky.transportdroidil;

import java.util.Locale;

import net.lutzky.transportdroidil.AutolocationTextView.State;
import net.lutzky.transportdroidil.BusGetter.InteractiveLinkClicked;
import android.app.Activity;
import android.app.ProgressDialog;
import android.text.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.*;

/*
 * Deprecation warnings suppressed for use of old CliboardManager API. We
 * still support gingerbread. (non-Javadoc)
 */
@SuppressWarnings("deprecation")
public class TransportDroidIL extends Activity implements InteractiveLinkClicked {
	private static final String TAG = "TransportDroidIL";

	private Spanned lastResult;
	private Exception lastException;
	
	private final BusGovIlGetter motBg = new BusGovIlGetter();
	private final EggedGetter eggedBg = new EggedGetter();
	
	private AdView adView;
	
	public enum Provider { EGGED, MOT };
	Provider provider = Provider.MOT;

	private void setButtonsEnabled(boolean enabled) {
		QueryView queryView = (QueryView) findViewById(R.id.queryview);

		queryView.setButtonsEnabled(enabled);
	}

	private void updateResultText(Spanned result) {
		SharedPreferences.Editor editor = getPreferences(0).edit();
		editor.putString("Result", Html.toHtml(result));
		editor.commit();
		TextView tv = (TextView)findViewById(R.id.query_result);
		tv.setText(result);
	}

	private void runQuery(final BusGetter bg, final int interactionIndex) {
		final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

		final Handler mHandler = new Handler();

		QueryView queryView = (QueryView) findViewById(R.id.queryview);
		final String query = queryView.getQueryString();

		final ProgressDialog dialog = ProgressDialog.show(this, "", query);

		final Runnable mUpdateResults = new Runnable() {
			@Override
			public void run() {
				updateResultText(lastResult);
				try {
					dialog.dismiss();
				}
				catch (IllegalArgumentException e) {
					Log.w(TAG, "mUpdateResults tried to dismiss dialog, and got: " + e);
				}
				setButtonsEnabled(true);
			}
		};

		final Runnable mShowError = new Runnable() {
			@Override
			public void run() {
				String exceptionText = String.format(getString(R.string.error),
						lastException);

				try {
					dialog.dismiss();
				}
				catch (IllegalArgumentException e) {
					Log.w(TAG, "mShowError tried to dismiss dialog, and got: " + e);
				}

				Toast toast = Toast.makeText(getApplicationContext(),
						exceptionText, Toast.LENGTH_LONG);
				toast.show();

				setButtonsEnabled(true);
			}
		};

		setButtonsEnabled(false);

		Log.d(TAG, "Querying: " + query + ", index = " + interactionIndex);

		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					bg.runQuery(query, interactionIndex);
					lastResult = bg.getFilteredResult();
					if (settings.getBoolean("bidi_numbers_fix", false)) {
						BidiHack bh = new BidiHack();
						lastResult = bh.reorder(lastResult);
					}

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
		// Make sure that theme is set before setContentView and super.onCreate.
		Preferences.applyTheme(this);

		super.onCreate(savedInstanceState);
		
		setLocale();

		setContentView(R.layout.main);
		
		eggedBg.setInteractiveLinkedClicked(this);
		motBg.setInteractiveLinkedClicked(this);

		addQueryView();

		AutolocationTextView altv = addAutoLocationTextView();

		updateLocationProgress(altv.getState());

		addQueryResultTextView();
		
		showAds();
	}

	private void addQueryResultTextView() {
		TextView tvQueryResult = (TextView)findViewById(R.id.query_result);
		tvQueryResult.setText(Html.fromHtml(getPreferences(0).getString("Result", "")));
		tvQueryResult.setMovementMethod(new LinkMovementMethod());
		
		registerForContextMenu(tvQueryResult);
	}

	private AutolocationTextView addAutoLocationTextView() {
		AutolocationTextView altv = (AutolocationTextView)findViewById(R.id.query_from);
		altv.onStateChange(new AutolocationTextView.StateChangeCallback() {
			@Override
			public void stateHasChanged(State newState) {
				updateLocationProgress(newState);
			}
		});
		return altv;
	}

	private void addQueryView() {
		QueryView queryView = (QueryView) findViewById(R.id.queryview);
		applyPreferences();
		queryView.loadPersistentState(getPreferences(0));
		queryView.setOnSearchButtonClickListener(new OnSearchButtonClickListener() {
			@Override
			public void onSearchButtonClick(View source) {
				switch(provider) {
				case EGGED:
					runQuery(eggedBg, 0);
				case MOT:
					runQuery(motBg, 0);
				}
			}
		});
	}

	private void setLocale() {
		Locale locale = new Locale("he");
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config,
		      getBaseContext().getResources().getDisplayMetrics());
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		TextView tvQueryResult = (TextView)findViewById(R.id.query_result);
		
		if (v == tvQueryResult) {
		    menu.add(0, v.getId(), 0, R.string.copy);
	
		    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE); 
		    clipboard.setText(tvQueryResult.getText());
		}
	}
	
	private void showAds() {
		Resources res = getResources();
		if (res.getBoolean(R.bool.useAds) == false) {
			return;
		}
		adView = new AdView(this);
		adView.setAdUnitId(res.getString(R.string.adUnitId));
		adView.setAdSize(AdSize.BANNER);
		
		LinearLayout layout = (LinearLayout)findViewById(R.id.mainLayout);
		layout.addView(adView);
		AdRequest adRequest = new AdRequest.Builder()
			.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
			.build();
		adView.loadAd(adRequest);
	}

	private void updateLocationProgress(AutolocationTextView.State state) {
		Log.d(TAG, "Updating location progress with state " + state);
		ProgressBar locationProgress = (ProgressBar)findViewById(R.id.location_progress);
		ImageButton locateMe = (ImageButton)findViewById(R.id.locate_me);
		switch(state) {
		case SEARCHING:
			locationProgress.setVisibility(View.VISIBLE);
			locateMe.setVisibility(View.GONE);
			setButtonsEnabled(false);
			break;
		default:
			locationProgress.setVisibility(View.GONE);
			locateMe.setVisibility(View.VISIBLE);
			setButtonsEnabled(true);
			break;
		}
	}
	
	@Override
	public void onDestroy() {
		if (adView != null) {
			adView.destroy();
		}
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		QueryView queryView = (QueryView) findViewById(R.id.queryview);
		queryView.savePersistentState(getPreferences(0));
		if (adView != null) {
			adView.pause();
		}
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (adView != null) {
			adView.resume();
		}
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
		case R.id.realtime_bus:
			openRealtimeActivity();
			return true;
		}
		return false;
	}

	private void openPreferences() {
		Intent intent = new Intent(this, Preferences.class);
		startActivityForResult(intent, 0);
	}

	private void openRealtimeActivity() {
		Intent intent = new Intent(this, RealtimePickRouteActivity.class); 
		startActivity(intent);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		applyPreferences();
	}
	
	private void applyPreferences() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

		applyProviderPreference(settings);

		applyRTLFix(settings);

		if (settings.getBoolean("clear_completions", false)) {
			clearHistory(settings);
		}
	}

	private void applyRTLFix(SharedPreferences settings) {
		TextView tv = (TextView)findViewById(R.id.query_result);

		if (settings.getBoolean("rtl_fix", false)) {
			tv.setGravity(Gravity.RIGHT);
		}
		else {
			tv.setGravity(Gravity.NO_GRAVITY);
		}
	}

	private void applyProviderPreference(SharedPreferences settings) {
		String stored_provider_name = settings.getString("provider", "mot");
		
		if (stored_provider_name.equals("egged")) {
			provider = Provider.EGGED;
		}
		else if (stored_provider_name.equals("mot")) {
			provider = Provider.MOT;
		}
		else {
			Log.w(TAG, "Invalid provider stored in preferences: " + provider);
			Log.w(TAG, "Using provider " + provider.name() + " instead.");
		}
		
		getQueryView().setProvider(provider);
	}

	private void clearHistory(SharedPreferences settings) {
		Log.i(TAG, "User requested a history clear");

		getQueryView().clearCompletionOptions(settings);

		Toast toast = Toast.makeText(getApplicationContext(),
				R.string.clear_completions_toast, Toast.LENGTH_LONG);
		toast.show();

		// Reset the relevant preference, so the user can set it again
		SharedPreferences.Editor e = settings.edit();
		e.putBoolean("clear_completions", false);
		e.commit();
	}

	private QueryView getQueryView() {
		return (QueryView) findViewById(R.id.queryview);
	}

	@Override
	public void onInteractiveLinkClicked(BusGetter bg, int index) {
		runQuery(bg, index);
	}
}
