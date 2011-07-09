package net.lutzky.transportdroidil;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class QueryView extends LinearLayout {
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

		Button submit_egged = (Button) findViewById(R.id.submit_egged);
		Button submit_busgovil = (Button) findViewById(R.id.submit_busgovil);
		final InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
		
		getTimeTextView().setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int action, KeyEvent event) {
				if (action == EditorInfo.IME_ACTION_GO) {
					// TODO click on the preferred provider.
					imm.hideSoftInputFromWindow(getFromTextView().getWindowToken(), 0);
					onSearchButtonClickListener.onSearchButtonClick(QueryView.this, R.id.submit_busgovil);
					return true;
				}
				return false;
			}
		});
		
		
		
		submit_egged.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (onSearchButtonClickListener != null)
					imm.hideSoftInputFromWindow(getFromTextView().getWindowToken(), 0);
					onSearchButtonClickListener.onSearchButtonClick(QueryView.this, R.id.submit_egged);
			}
		});

		submit_busgovil.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (onSearchButtonClickListener != null) {
					imm.hideSoftInputFromWindow(getFromTextView().getWindowToken(), 0);
					onSearchButtonClickListener.onSearchButtonClick(QueryView.this, R.id.submit_busgovil);
				}
			}
		});
	}
	
	@Override
	public boolean onKeyShortcut(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyShortcut(keyCode, event);
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
