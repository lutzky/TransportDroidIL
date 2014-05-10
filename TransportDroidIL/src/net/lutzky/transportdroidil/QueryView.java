package net.lutzky.transportdroidil;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class QueryView extends LinearLayout implements View.OnClickListener {
	private OnSearchButtonClickListener onSearchButtonClickListener = null;
	protected SharedPreferences settings = null;

	public void setOnSearchButtonClickListener(
			OnSearchButtonClickListener onSearchButtonClickListener) {
		this.onSearchButtonClickListener = onSearchButtonClickListener;
	}

	public QueryView(final Context context, AttributeSet attrs) {
		super(context, attrs);
		inflate(context, R.layout.query, this);
		
		settings = PreferenceManager.getDefaultSharedPreferences(context);

		ArrayAdapter<String> placesAdapter = new ArrayAdapter<String>(context,
				android.R.layout.select_dialog_item);
		ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(context,
				android.R.layout.select_dialog_item);
		getFromTextView().setAdapter(placesAdapter);
		getToTextView().setAdapter(placesAdapter);
		getTimeTextView().setAdapter(timeAdapter);

		// Share the list of completion options.
		getToTextView().setCompletionOptions(getFromTextView().getCompletionOptions());

		final Button submit = (Button) findViewById(R.id.submit);

		getTimeTextView().setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int action, KeyEvent event) {
				if (action == EditorInfo.IME_ACTION_SEARCH) {
					onClick(submit);
					return true;
				}
				return false;
			}
		});

		final AutolocationTextView queryFrom = (AutolocationTextView)findViewById(R.id.query_from);
		final EnhancedTextView queryTo = (EnhancedTextView)findViewById(R.id.query_to);

		ImageButton locateMe = (ImageButton)findViewById(R.id.locate_me);
		locateMe.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				queryFrom.startSearch();
			}
		});

		ImageButton reverse = (ImageButton)findViewById(R.id.reverse);
		reverse.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				CharSequence temp = queryTo.getText();
				queryTo.setText(queryFrom.getText());
				queryFrom.setText(temp);
				queryFrom.markAsCustom();
			}
		});

		submit.setOnClickListener(this);
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

	public void clearCompletionOptions(SharedPreferences settings) {
		getFromTextView().clearCompletionOptions(settings);
		// Not called for to because its shared with from.
		getTimeTextView().clearCompletionOptions(settings);
	}

	public String getQueryString() {
		final char Lamed = '\u05dc';
		String from = getFromTextView().getString();
		String to = getToTextView().getString();
		String time = getTimeTextView().getString();
		
		StringBuilder queryString = new StringBuilder();
		queryString.append(from);
		queryString.append(" ");
		queryString.append(Lamed);
		queryString.append(to);
		
		if (time.length() > 0) {
			queryString.append(" ");
			queryString.append(time);
		}
		
		queryString.append(" ");
	
		queryString.append(settings.getString("extra_info", ""));
		
		return queryString.toString();
	}

	public void setButtonsEnabled(boolean enabled) {
		Button submit = (Button) findViewById(R.id.submit);

		submit.setEnabled(enabled);
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

	public void setProvider(TransportDroidIL.Provider provider) {
		Button submitButton = (Button) findViewById(R.id.submit);
		int newIconId = 0;
		
		switch(provider) {
		case MOT:
			newIconId = R.drawable.mot;
			break;
		case EGGED:
			newIconId = R.drawable.egged;
			break;
		default:
			throw new IllegalArgumentException("Got invalid provider " + provider);
		}
		submitButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, newIconId, 0);
	}

	@Override
	public void onClick(View v) {
		final InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

		getFromTextView().addCurrentValueAsCompletion();
		getToTextView().addCurrentValueAsCompletion();
		getTimeTextView().addCurrentValueAsCompletion();

		if (onSearchButtonClickListener != null) {
			imm.hideSoftInputFromWindow(getFromTextView().getWindowToken(), 0);
			onSearchButtonClickListener.onSearchButtonClick(QueryView.this);
		}
	}
}

interface OnSearchButtonClickListener {
	void onSearchButtonClick(View source);
}
