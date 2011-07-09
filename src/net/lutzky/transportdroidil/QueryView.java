package net.lutzky.transportdroidil;

import java.util.Arrays;
import java.util.Collection;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
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
		
		ArrayAdapter<String> placesAdapter = new ArrayAdapter<String>(context,
				R.layout.list_item);
		ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(context,
				R.layout.list_item);
		getFromTextView().setAdapter(placesAdapter);
		getToTextView().setAdapter(placesAdapter);
		getTimeTextView().setAdapter(timeAdapter);
		
		// Share the list of completion options.
		getToTextView().setCompletionOptions(getFromTextView().getCompletionOptions());

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
		getFromTextView().savePersistentState(settings);
		// Not called for to because its shared with from.
		getTimeTextView().savePersistentState(settings);
	}
	
	public void loadPersistentState(SharedPreferences settings) {
		getFromTextView().loadPersistentState(settings);
		// Not called for to because its shared with from.
		getTimeTextView().loadPersistentState(settings);
	}

	public String getQueryString() {
		return getFromTextView().getText() + " ל" + getToTextView().getText() + " " + 
				getTimeTextView().getText();
	}
	
	public void setButtonsEnabled(boolean enabled) {
		Button submit_egged = (Button) findViewById(R.id.submit_egged);
		Button submit_busgovil = (Button) findViewById(R.id.submit_busgovil);

		submit_egged.setEnabled(enabled);
		submit_busgovil.setEnabled(enabled);
	}
	
	EnhancedTextView getFromTextView() {
		return (EnhancedTextView) findViewById(R.id.query_from);
	}
	
	EnhancedTextView getToTextView() {
		return (EnhancedTextView) findViewById(R.id.query_to);
	}
	
	EnhancedTextView getTimeTextView() {
		return (EnhancedTextView) findViewById(R.id.query_time);
	}
}

interface OnSearchButtonClickListener {
	void onSearchButtonClick(View source, int provider);
}
