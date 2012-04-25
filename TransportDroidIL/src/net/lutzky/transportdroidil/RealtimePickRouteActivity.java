package net.lutzky.transportdroidil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;

public class RealtimePickRouteActivity extends ExpandableListActivity {
	private ExpandableListAdapter adapter;
	private SQLiteDatabase database;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
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
						new String[] { "number", "variant", "id as _id" }, 
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
		@SuppressWarnings("unchecked")
		Cursor child = (Cursor) adapter.getChild(groupPosition, childPosition);
		String routeId = child.getString(2);
		
		Intent intent = new Intent(this, RealtimeBusActivity.class);
		intent.putExtra("company", "OmniExpress");
		intent.putExtra("routeId", routeId);
		startActivity(intent);
		
		return true;
	}
}
