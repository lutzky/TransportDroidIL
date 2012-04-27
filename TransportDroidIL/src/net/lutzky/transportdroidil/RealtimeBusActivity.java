package net.lutzky.transportdroidil;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class RealtimeBusActivity extends Activity implements OnItemSelectedListener {
	private RealtimeBusUpdater model = null;
	private ScheduledThreadPoolExecutor timer;
	private Exception lastException;
	private final DateFormat timeFormat = new SimpleDateFormat("HH:mm");
	private ScheduledFuture<?> scheduledUpdate;
	private SimpleAdapter listAdapter;
	private SimpleCursorAdapter variantAdapter;
	private SQLiteDatabase database;
	private String company;
	private String routeId;
	private String number;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.realtime_bus);
		
		Intent intent = getIntent();
		if (intent != null) {
			String provider = intent.getExtras().getString("provider");
			company = intent.getExtras().getString("company");
			routeId = intent.getExtras().getString("routeId");
			number = intent.getExtras().getString("number");
			if ("OmniExpress".equals(provider)) {
				model = new OmniExpBusUpdater(routeId);
				database = (new RealtimeRoutesOpenHelper(this)).getReadableDatabase();
				Cursor variantCursor = database.query(
						"routes", 
						new String[] { "variant", "id", "variant_index as _id" }, 
						"number = ? and company = ?", 
						new String[] { number, company },
						null, 
						null, 
						"variant_index");
				variantAdapter = new SimpleCursorAdapter(
						this, 
						android.R.layout.simple_spinner_item, 
						variantCursor, 
						new String[] { "variant" }, 
						new int[] { android.R.id.text1 });
				variantAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
				Spinner variantSpinner = (Spinner) findViewById(R.id.variantSpinner);
				variantSpinner.setAdapter(variantAdapter);
				variantSpinner.setOnItemSelectedListener(this);

			}
			else if ("MockCompany".equals(provider))
				model = new MockRealtimeBusUpdater();
		}
		timer = new ScheduledThreadPoolExecutor(1);
		
		ListView lv = (ListView) findViewById(R.id.realtimeUpdateListView);
		lv.addHeaderView(getLayoutInflater().inflate(R.layout.realtime_bus_table_header, null));		
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
		if (scheduledUpdate != null) {
			scheduledUpdate.cancel(true);
			scheduledUpdate = null;
		}
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
		
		findViewById(R.id.inactive_service).setVisibility(
				model.isServiceActive() ? View.GONE : View.VISIBLE);
		
		List<Map<String, String>> data = buildRealtimeDataFromModel();
		ListView lv = (ListView) findViewById(R.id.realtimeUpdateListView);
		listAdapter = new SimpleAdapter(
				this, 
				data, 
				R.layout.realtime_bus_table_item, 
				new String[] { "stop", "eta-up", "eta-down", "bus-up", "bus-down" }, 
				new int[] { R.id.stop, R.id.eta_up, R.id.eta_down, R.id.bus_up, R.id.bus_down });
		lv.setAdapter(listAdapter);

		
		TextView lastUpdate = (TextView) findViewById(R.id.lastUpdateTextView);
		Date lastUpdateTime = model.getLastUpdateTime();
		if (lastUpdateTime != null)
			lastUpdate.setText(getString(R.string.last_update) + timeFormat.format(lastUpdateTime));
		else
			lastUpdate.setText("");
	}

	private List<Map<String, String>> buildRealtimeDataFromModel() {
		final List<Map<String, String>> result = new ArrayList<Map<String, String>>();
		List<Stop> stops = model.getStops();
		stops = stops == null ? Collections.<Stop>emptyList() : stops;
		List<Eta> etas = model.getEtas();
		etas = etas == null ? Collections.<Eta>emptyList() : etas;
		List<Bus> buses = model.getBuses();
		buses = buses == null ? Collections.<Bus>emptyList() : buses;

		List<Entity> allEntities = new ArrayList<Entity>(stops.size() + etas.size() + buses.size());
		allEntities.addAll(stops);
		allEntities.addAll(etas);
		allEntities.addAll(buses);
		Collections.sort(allEntities, new Comparator<Entity>() {
			@Override
			public int compare(Entity lhs, Entity rhs) {
				int posCompare = Double.compare(lhs.getPosition(), rhs.getPosition());
				if (posCompare == 0) {
					int lhsType = getTypeOrder(lhs),
						rhsType = getTypeOrder(rhs);
					return lhsType - rhsType;
				}
				else
					return posCompare;
			}

			private int getTypeOrder(Entity e) {
				if (e instanceof Bus)
					return 0;
				else if (e instanceof Stop)
					return 1;
				else // if (e instanceof Eta)
					return 2;
			}
		});
		
		final Map<String, String> item = new HashMap<String, String>();
		EntityVisitor visitor = new EntityVisitor() {
			double prevPosition = 0;
			boolean prevIsBus = false;
			
			@Override
			public void visitBus(Bus bus) {
				prevIsBus = true;
				addItem(bus.getPosition());
				boolean dir = bus.getDirection();
				String busArrow = dir ? "↓" : "↑";
				String cell = dir ? "bus-down" : "bus-up";
				item.put(cell, busArrow);
			}
			
			@Override
			public void visitStop(Stop stop) {
				if (!prevIsBus)
					addItem(stop.getPosition());
				else
					prevPosition = stop.getPosition();
				prevIsBus = false;
				item.put("stop", stop.getTitle());
			}
			
			@Override
			public void visitEta(Eta eta) {
				prevIsBus = false;
				if (eta.getPosition() != prevPosition)
					addItem(eta.getPosition());
				boolean dir = eta.getDirection();
				String cell = dir ? "eta-down" : "eta-up";
				item.put(cell, timeFormat.format(eta.getEta()));
			}

			private boolean first = true;
			private void addItem(double position) {
				if (!first)
					result.add(new HashMap<String, String>(item));
				item.put("stop", "");
				item.put("bus-up", "");
				item.put("bus-down", "");
				item.put("eta-up", "");
				item.put("eta-down", "");
				prevPosition = position;
				first = false;
			}
		};
		for (Entity entity : allEntities) {
			entity.visit(visitor);
		}
		result.add(item);
		return result;
	}
	
	private void updateLocationProgress(boolean updating) {
		ProgressBar progress = (ProgressBar)findViewById(R.id.realtimeUpdateProgress);
		progress.setVisibility(updating ? View.VISIBLE : View.GONE);
	}

	public RealtimeBusUpdater getModel() {
		return model;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		Cursor cursor = (Cursor) variantAdapter.getItem(position);
		String routeId = cursor.getString(1);
		if (routeId != this.routeId) {
			this.routeId = routeId;
			stopUpdates();
			model = new OmniExpBusUpdater(routeId);
			startUpdates();
		}
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}
}
