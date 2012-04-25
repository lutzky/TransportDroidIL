package net.lutzky.transportdroidil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ExpandableListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;

public class RealtimePickRouteActivity extends ExpandableListActivity {
	private static final String CITY = "CITY";
	private static final String ROUTE_NUM = "ROUTE_NUM";
	private static final String ROUTE_ID = "ROUTE_ID";
	
	private ExpandableListAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		adapter = createListAdapter();
		setListAdapter(adapter);
	}

	private ExpandableListAdapter createListAdapter() {
		// TODO generate the data from the web.
		List<Map<String, String>> groupData = new ArrayList<Map<String, String>>();
		List<List<Map<String, String>>> childData = new ArrayList<List<Map<String, String>>>();
		String[] cities = new String[] { "יקנעם", "טבעון", "דאליית אל כרמל", "עוספיה", "אום אל פחם", "עפולה", "נצרת עילית", "חיפה" };
		String[][][] children = new String[][][] {
				// יקנעם
				{
					{ "01501097", "15" },
					{ "01601097", "16" },
					{ "01701097", "17" },
					{ "02501097", "25" },
					{ "02501297", "25" },
					{ "18001097", "180" },
					{ "18101097", "181" },
					{ "18401097", "184" },
					{ "18801197", "188" },
					{ "18801297", "188" },
				},
				// טבעון
				{
					{ "07501097", "75" },
					{ "07501197", "75" },
					{ "07501297", "75" },
					{ "07501397", "75" },
					{ "07501497", "75" },
					{ "07501597", "75" },
				},
				// דאליית אל כרמל
				{},
				// עוספיה
				{},
				// אום אל פחם
				{},
				// עפולה
				{},
				// נצרת עילית
				{},
				// חיפה
				{}
		};
		for (String city : cities) {
			Map<String, String> map = new HashMap<String, String>();
			map.put(CITY, city);
			groupData.add(map);
		}
		for (String[][] childs : children) {
			List<Map<String, String>> childList = new ArrayList<Map<String, String>>();
			for (String[] child : childs) {
				Map<String, String> map = new HashMap<String, String>();
				map.put(ROUTE_ID, child[0]);
				map.put(ROUTE_NUM, child[1]);
				childList.add(map);
			}
			childData.add(childList);
		}
		
		ExpandableListAdapter adapter = new SimpleExpandableListAdapter(
				this, 
				groupData, 
				android.R.layout.simple_expandable_list_item_1,
				new String[] { CITY },
				new int[] { android.R.id.text1 },
				childData,
				android.R.layout.simple_expandable_list_item_2,
				new String[] { ROUTE_NUM },
				new int[] { android.R.id.text1 });
		return adapter;
	}
	
	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		@SuppressWarnings("unchecked")
		Map<String, String> child = (Map<String, String>) adapter.getChild(groupPosition, childPosition);
		String routeId = child.get(ROUTE_ID);
		
		Intent intent = new Intent(this, RealtimeBusActivity.class);
		intent.putExtra("company", "OmniExpress");
		intent.putExtra("routeId", routeId);
		startActivity(intent);
		
		return true;
	}
}
