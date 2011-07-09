package net.lutzky.transportdroidil;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;

public class QueryView extends LinearLayout {
	private static final String SEPARATOR = "\n";
	private static final int MAX_HISTORY = 10000;
	
	final List<String> completionOptions = new LinkedList<String>();
	
	private OnSearchButtonClickListener onSearchButtonClickListener = null; 

	public void setOnSearchButtonClickListener(
			OnSearchButtonClickListener onSearchButtonClickListener) {
		this.onSearchButtonClickListener = onSearchButtonClickListener;
	}
	
	public QueryView(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.query, this);
		
		AutoCompleteTextView query = (AutoCompleteTextView) findViewById(R.id.query);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
				R.layout.list_item);
		query.setAdapter(adapter);

		Button submit_egged = (Button) findViewById(R.id.submit_egged);
		Button submit_busgovil = (Button) findViewById(R.id.submit_busgovil);
		
		submit_egged.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				addCompletionOptions();
				if (onSearchButtonClickListener != null)
					onSearchButtonClickListener.onSearchButtonClick(QueryView.this, R.id.submit_egged);
			}
		});

		submit_busgovil.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				addCompletionOptions();
				if (onSearchButtonClickListener != null)
					onSearchButtonClickListener.onSearchButtonClick(QueryView.this, R.id.submit_busgovil);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	private void addCompletionOptions() {
		AutoCompleteTextView queryView = getQueryTextView();
		String query = queryView.getText().toString();
		
		if (completionOptions.contains(query)) {
			// No duplicates.
			return;
		}

		// Add our completion to the actual active completions database
		ArrayAdapter<String> arrayAdapter = (ArrayAdapter<String>) (queryView
				.getAdapter());
		arrayAdapter.add(query);

		// Add our completion to our non-persistent storage
		completionOptions.add(0, query);
	}
	
	static <E> List<E> uniqueBoundedList(List<E> l, int bound) {
		List<E> result = new LinkedList<E>();

		int count = 0;

		for (E item : l) {
			if (!result.contains(item)) {
				result.add(item);
				count += 1;
			}

			if (count == bound) {
				return result;
			}
		}

		return result;
	}
	
	public void savePersistentState(SharedPreferences settings) {
		SharedPreferences.Editor editor = settings.edit();
		StringBuilder buffer = new StringBuilder(completionOptions.size());
		List<String> toSave = uniqueBoundedList(completionOptions, MAX_HISTORY);
		for (String s : toSave) {
			buffer.append(s);
			buffer.append(SEPARATOR);
		}
		try {
			buffer.deleteCharAt(buffer.length() - 1);
		} catch (Exception e) {
		}
		editor.putString("Queries", buffer.toString());
		editor.commit();
	}
	
	public void loadPersistentState(SharedPreferences settings) {
		String allQueries = settings.getString("Queries", "");
		for (String query : allQueries.split(SEPARATOR)) {
			completionOptions.add(query);
		}
		ArrayAdapter<String> arrayAdapter = (ArrayAdapter<String>) (getQueryTextView().getAdapter());
		arrayAdapter.clear();
		// no addAll until API 11
		for (String s : completionOptions)
			arrayAdapter.add(s);
	}

	public String getQueryString() {
		return getQueryTextView().getText() + "";
	}
	
	public void setButtonsEnabled(boolean enabled) {
		Button submit_egged = (Button) findViewById(R.id.submit_egged);
		Button submit_busgovil = (Button) findViewById(R.id.submit_busgovil);

		submit_egged.setEnabled(enabled);
		submit_busgovil.setEnabled(enabled);
	}
	
	AutoCompleteTextView getQueryTextView() {
		return (AutoCompleteTextView) findViewById(R.id.query);
	}
}

interface OnSearchButtonClickListener {
	void onSearchButtonClick(View source, int provider);
}
