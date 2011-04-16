package net.lutzky.transportdroidil;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TransportDroidIL extends Activity {
	private static final String SEPARATOR = "\n";

	private String lastResult;
	private Exception lastException;

	private static final int MAX_HISTORY = 10000;

	private void setButtonsEnabled(boolean enabled) {
		Button submit_egged = (Button) findViewById(R.id.submit_egged);
		Button submit_busgovil = (Button) findViewById(R.id.submit_busgovil);

		submit_egged.setEnabled(enabled);
		submit_busgovil.setEnabled(enabled);
	}

	private void updateResultText(String result) {
		SharedPreferences.Editor editor = getPreferences(0).edit();
		editor.putString("Result", result);
		editor.commit();
		TextView tv = (TextView)findViewById(R.id.query_result);
		tv.setText(result);
	}

	private void runQuery(final BusGetter bg) {
		final Handler mHandler = new Handler();
		final ProgressDialog dialog = ProgressDialog.show(this, "",
				getString(R.string.please_wait));

		final Runnable mUpdateResults = new Runnable() {
			@Override
			public void run() {
				updateResultText(lastResult);
				dialog.hide();
				setButtonsEnabled(true);
			}
		};

		final Runnable mShowError = new Runnable() {
			@Override
			public void run() {
				String exceptionText = String.format(getString(R.string.error),
						lastException);

				dialog.hide();

				Toast toast = Toast.makeText(getApplicationContext(),
						exceptionText, Toast.LENGTH_LONG);
				toast.show();

				setButtonsEnabled(true);
			}
		};

		EditText queryEditText = (EditText) findViewById(R.id.query);
		final String query = queryEditText.getText().toString();

		setButtonsEnabled(false);

		addCompletionOption(query);

		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					bg.runQuery(query);
					lastResult = bg.getFilteredResult();
					mHandler.post(mUpdateResults);
				} catch (Exception e) {
					lastException = e;
					mHandler.post(mShowError);
				}
			}
		};

		t.start();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Button submit_egged = (Button) findViewById(R.id.submit_egged);
		Button submit_busgovil = (Button) findViewById(R.id.submit_busgovil);

		submit_egged.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				runQuery(new EggedGetter());
			}
		});

		submit_busgovil.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				runQuery(new BusGovIlGetter());
			}
		});

		loadPreviousQueries();

		AutoCompleteTextView query = (AutoCompleteTextView) findViewById(R.id.query);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.list_item, this.completionOptions);
		query.setAdapter(adapter);

		TextView tvQueryResult = (TextView)findViewById(R.id.query_result);
		tvQueryResult.setText(getPreferences(0).getString("Result", ""));
	}

	private void loadPreviousQueries() {
		SharedPreferences settings = getPreferences(0);
		String allQueries = settings.getString("Queries", "");
		for (String query : allQueries.split(SEPARATOR)) {
			completionOptions.add(query);
		}
	}

	@SuppressWarnings("unchecked")
	void addCompletionOption(String query) {
		if (completionOptions.contains(query)) {
			// No duplicates.
			return;
		}

		// Add our completion to the actual active completions database
		AutoCompleteTextView queryView = (AutoCompleteTextView) findViewById(R.id.query);
		ArrayAdapter<String> arrayAdapter = (ArrayAdapter<String>) (queryView
				.getAdapter());
		arrayAdapter.add(query);

		// Add our completion to the persistent storage
		completionOptions.add(0, query);

		SharedPreferences settings = getPreferences(0);
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

	final List<String> completionOptions = new LinkedList<String>();
}