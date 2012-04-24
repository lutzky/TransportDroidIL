package net.lutzky.transportdroidil;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.lutzky.transportdroidil.RealtimeBusUpdater.Bus;
import net.lutzky.transportdroidil.RealtimeBusUpdater.Entity;
import net.lutzky.transportdroidil.RealtimeBusUpdater.EntityVisitor;
import net.lutzky.transportdroidil.RealtimeBusUpdater.Eta;
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
	private Exception lastException;
	private final DateFormat timeFormat = new SimpleDateFormat("HH:mm");
	private ScheduledFuture<?> scheduledUpdate;
	
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
			else if ("MockCompany".equals(company))
				model = new MockRealtimeBusUpdater();
		}
		timer = new ScheduledThreadPoolExecutor(1);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		startUpdates();
	}
	
	@Override
	protected void onPause() {
		stopUpdates();
		super.onPause();
	}

	private void startUpdates() {
		if (model != null && scheduledUpdate == null) {
			final Handler handler = new Handler();
			scheduledUpdate = timer.scheduleWithFixedDelay(new Runnable() {
				@Override
				public void run() {
					update(handler);
				}
			}, 0, 5, TimeUnit.SECONDS);
		}
	}
	
	private void stopUpdates() {
		scheduledUpdate.cancel(true);
		scheduledUpdate = null;
	}
	
	private void update(Handler handler) {
		if (model == null) return;

		handler.post(new Runnable() {
			@Override
			public void run() {
				updateLocationProgress(true);
				TextView lastUpdate = (TextView) findViewById(R.id.lastUpdateTextView);
				lastUpdate.setText(getString(R.string.updating));
			}
		});
		
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

				lastException.printStackTrace();
				lastException = null;
			}
		};

		try {
			model.update();

			handler.post(mUpdateResults);
		} catch (Exception e) {
			lastException = e;
			handler.post(mShowError);
		}
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
		final StringBuilder result = new StringBuilder();
		List<Stop> stops = model.getStops();
		List<Eta> etas = model.getEtas();
		List<Bus> buses = model.getBuses();
		Date nextBus = model.getNextBus();
		
		if (nextBus != null) {
			result.append(getString(R.string.next_bus_at_time));
			result.append(' ');
			result.append(timeFormat.format(nextBus));
			result.append('\n');
		}
		result.append('\n');
		result.append(getString(R.string.stops));
		result.append('\n');
		
		List<Entity> allEntities = new ArrayList<Entity>(stops.size() + etas.size() + buses.size());
		allEntities.addAll(stops);
		allEntities.addAll(etas);
		allEntities.addAll(buses);
		Collections.sort(allEntities, new Comparator<Entity>() {
			@Override
			public int compare(Entity lhs, Entity rhs) {
				return Double.compare(lhs.getPosition(), rhs.getPosition());
			}
		});
		
		for (Entity entity : allEntities) {
			entity.visit(new EntityVisitor() {
				@Override
				public void visitStop(Stop stop) {
					result.append("  ");
					result.append(stop.getTitle());
				}
				
				@Override
				public void visitEta(Eta eta) {
					result.append(timeFormat.format(eta.getEta()));
				}
				
				@Override
				public void visitBus(Bus bus) {
					if (bus.getDirection())
						result.append("↓ ");
					else
						result.append("↑ ");
					result.append(getString(R.string.bus_is_here));
				}
			});
			result.append('\n');
		}
		return result.toString();
	}
	
	private void updateLocationProgress(boolean updating) {
		ProgressBar progress = (ProgressBar)findViewById(R.id.realtimeUpdateProgress);
		progress.setVisibility(updating ? View.VISIBLE : View.GONE);
	}

	public RealtimeBusUpdater getModel() {
		return model;
	}
}
