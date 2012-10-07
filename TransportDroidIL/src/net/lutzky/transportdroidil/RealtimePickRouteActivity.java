package net.lutzky.transportdroidil;

import java.io.IOException;

import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;
import android.widget.Toast;

public class RealtimePickRouteActivity extends ExpandableListActivity {
	private ExpandableListAdapter adapter;
	private SQLiteDatabase database;
	private Exception lastException;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Preferences.applyTheme(this);
		
		adapter = createListAdapter();
		setListAdapter(adapter);
	}

	private ExpandableListAdapter createListAdapter() {
		database = (new RealtimeRoutesOpenHelper(this)).getReadableDatabase();
		Cursor groupCursor = database.query(
				true,
				"company_names", 
				new String[] { "company_name as _id", "company_name" }, 
				null, 
				null,
				null, 
				null, 
				null,
				null);
		ExpandableListAdapter adapter = new SimpleCursorTreeAdapter(
				this, 
				groupCursor, 
				android.R.layout.simple_expandable_list_item_1, 
				new String[] { "company_name" },
				new int[] { android.R.id.text1 },
				android.R.layout.simple_expandable_list_item_2, 
				new String[] { "number", "variant" },
				new int[] { android.R.id.text1, android.R.id.text2 }
			) {
			
			@Override
			protected Cursor getChildrenCursor(Cursor groupCursor) {
				String companyName = groupCursor.getString(0);
				Cursor cursor = database.query(
						"routes inner join company_names on routes.company = company_names.company", 
						new String[] { "number", "variant", "id as _id", "routes.company" }, 
						"company_name = ? and variant_index = 0",
						new String[] { companyName },
						null, 
						null,
						"cast(number as INT), number");
				return cursor;
			}
		};
		return adapter;
	}
	
	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		Cursor //group = (Cursor) adapter.getGroup(groupPosition),
				child = (Cursor) adapter.getChild(groupPosition, childPosition);
		Intent intent = new Intent(this, RealtimeBusActivity.class);
		intent.putExtra("provider", "OmniExpress");
		intent.putExtra("number", child.getString(0));
		intent.putExtra("routeId", child.getString(2));
		intent.putExtra("company", child.getString(3));
		startActivity(intent);
		
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.realtime_bus_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			refresh();
			return true;
		}
		return false;
	}

	private void refresh() {
		final ProgressDialog dialog = ProgressDialog.show(this, "", getString(R.string.refresh));
		Thread updater = new Thread() {
			public void run() {
				RealtimeRoutesOpenHelper helper = new RealtimeRoutesOpenHelper(RealtimePickRouteActivity.this);
				try {
					helper.fillOmniExpress();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							dialog.dismiss();
						}
					});
				} catch (IOException e) {
					lastException = e;
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							String exceptionText = String.format(getString(R.string.error),
									lastException);

							dialog.dismiss();

							Toast toast = Toast.makeText(getApplicationContext(),
									exceptionText, Toast.LENGTH_LONG);
							toast.show();
						}
					});
				}
			};
		};
		updater.start();
	}
}
