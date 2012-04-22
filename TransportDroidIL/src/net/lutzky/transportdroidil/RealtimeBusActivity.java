package net.lutzky.transportdroidil;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.lutzky.transportdroidil.RealtimeBusUpdater.Stop;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class RealtimeBusActivity extends Activity {
	private RealtimeBusUpdater model = null;
	private ScheduledThreadPoolExecutor timer;
	protected Object lastException;
	private final DateFormat timeFormat = new SimpleDateFormat("HH:mm");
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.realtime_bus);
		
		Intent intent = getIntent();
		if (intent != null) {
			String company = intent.getExtras().getString("company");
			String routeId = intent.getExtras().getString("routeId");
			if ("OmniExpress".equals(company))
				model = new OmniExpBusUpdater(routeId);
			if (model != null) {
				final Handler mHandler = new Handler();
				timer = new ScheduledThreadPoolExecutor(1);
				timer.scheduleWithFixedDelay(new Runnable() {
					@Override
					public void run() {
						mHandler.post(new Runnable() {
							
							@Override
							public void run() {
								update();
							}
						});
					}
				}, 0, 5, TimeUnit.SECONDS);
			}
		}
	}
	
	private void update() {
		if (model == null) return;
		
		updateLocationProgress(true);
		TextView lastUpdate = (TextView) findViewById(R.id.lastUpdateTextView);
		lastUpdate.setText(getString(R.string.updating));
		
		final Handler mHandler = new Handler();

		final Runnable mUpdateResults = new Runnable() {
			@Override
			public void run() {
				updateViewFromModel();
				updateLocationProgress(false);
			}
		};

		final Runnable mShowError = new Runnable() {
			@Override
			public void run() {
				String exceptionText = String.format(getString(R.string.error),
						lastException);

				Toast toast = Toast.makeText(getApplicationContext(),
						exceptionText, Toast.LENGTH_LONG);
				toast.show();
				
				updateLocationProgress(false);
			}
		};

		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					model.update();

					mHandler.post(mUpdateResults);
				} catch (Exception e) {
					lastException = e;
					mHandler.post(mShowError);
				}
			}
		};

		t.start();
	}

	protected void updateViewFromModel() {
		if (model == null) return;
		String title = "" + model.getRouteNumber() + ": " + model.getRouteTitle();
		TextView routeTitle = (TextView) findViewById(R.id.routeTitleTextView);
		routeTitle.setText(title);
		
		String realtimeText = buildRealtimeTextFromModel();
		TextView realtime = (TextView) findViewById(R.id.realtimeStatusTextView);
		realtime.setText(realtimeText);
		
		TextView lastUpdate = (TextView) findViewById(R.id.lastUpdateTextView);
		lastUpdate.setText(getString(R.string.last_update) + timeFormat.format(model.getLastUpdateTime()));
	}

	private String buildRealtimeTextFromModel() {
		StringBuilder result = new StringBuilder();
		Float busPosition = model.getBusPosition();
		Date nextBus = model.getNextBus();
		List<Stop> stops = model.getStops();
		float prevPosition = -1;
		if (nextBus != null) {
			result.append(getString(R.string.next_bus_at_time));
			result.append(' ');
			result.append(timeFormat.format(nextBus));
			result.append('\n');
		}
		result.append('\n');
		result.append(getString(R.string.stops));
		result.append('\n');
		for (Stop stop : stops) {
			if (busPosition != null && busPosition >= prevPosition && busPosition < stop.getPosition()) {
				result.append(getString(R.string.bus_is_here));
				result.append('\n');
			}
			result.append("  ");
			result.append(stop.getTitle());
			result.append('\n');
			prevPosition = (float) stop.getPosition();
		}
		return result.toString();
	}
	
	private void updateLocationProgress(boolean updating) {
		ProgressBar progress = (ProgressBar)findViewById(R.id.realtimeUpdateProgress);
		progress.setVisibility(updating ? View.VISIBLE : View.GONE);
	}
}
