package net.lutzky.transportdroidil;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;

public class QueryView extends LinearLayout {
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
				if (onSearchButtonClickListener != null)
					onSearchButtonClickListener.onSearchButtonClick(QueryView.this, R.id.submit_egged);
			}
		});

		submit_busgovil.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (onSearchButtonClickListener != null)
					onSearchButtonClickListener.onSearchButtonClick(QueryView.this, R.id.submit_busgovil);
			}
		});
	}
	
	public void savePersistentState(SharedPreferences settings) {
		getQueryTextView().savePersistentState(settings);
	}
	
	public void loadPersistentState(SharedPreferences settings) {
		getQueryTextView().loadPersistentState(settings);
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
	
	EnhancedTextView getQueryTextView() {
		return (EnhancedTextView) findViewById(R.id.query);
	}
}

interface OnSearchButtonClickListener {
	void onSearchButtonClick(View source, int provider);
}
