package net.lutzky.transportdroidil;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class QueryView extends LinearLayout implements View.OnClickListener {
	private OnSearchButtonClickListener onSearchButtonClickListener = null; 

	public void setOnSearchButtonClickListener(
			OnSearchButtonClickListener onSearchButtonClickListener) {
		this.onSearchButtonClickListener = onSearchButtonClickListener;
	}

	public QueryView(final Context context, AttributeSet attrs) {
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

		final Button submit_egged = (Button) findViewById(R.id.submit_egged);
		final Button submit_busgovil = (Button) findViewById(R.id.submit_busgovil);
		
		getTimeTextView().setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int action, KeyEvent event) {
				if (action == EditorInfo.IME_ACTION_SEARCH) {
					if (submit_busgovil.getVisibility() == VISIBLE)
						onClick(submit_busgovil);
					else if (submit_egged.getVisibility() == VISIBLE)
						onClick(submit_egged);
					return true;
				}
				return false;
			}
		});
		
		submit_egged.setOnClickListener(this);
		submit_busgovil.setOnClickListener(this);
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
		String from = getFromTextView().getString();
		String to = getToTextView().getString();
		String time = getTimeTextView().getString();
		
		return from + " ל" + to + " " + time;
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

	public void setProvider(String provider) {
		Button motButton = (Button) findViewById(R.id.submit_busgovil), 
			   eggedButton = (Button) findViewById(R.id.submit_egged);  
		if (provider.equals("mot")) {
			motButton.setVisibility(VISIBLE);
			eggedButton.setVisibility(GONE);
		}
		else if (provider.equals("egged")) {
			motButton.setVisibility(GONE);
			eggedButton.setVisibility(VISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		final InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

		if (onSearchButtonClickListener != null) {
			imm.hideSoftInputFromWindow(getFromTextView().getWindowToken(), 0);
			onSearchButtonClickListener.onSearchButtonClick(QueryView.this, v.getId());
		}
	}
}

interface OnSearchButtonClickListener {
	void onSearchButtonClick(View source, int provider);
}
