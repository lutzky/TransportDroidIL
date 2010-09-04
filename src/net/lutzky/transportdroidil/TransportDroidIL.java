package net.lutzky.transportdroidil;

import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TransportDroidIL extends Activity {
	/** Called when the activity is first created. */

	private String lastResult;
	private Exception lastException;

	private void setButtonsEnabled(boolean enabled) {
		Button submit_egged = (Button) findViewById(R.id.submit_egged);
		Button submit_busgovil = (Button) findViewById(R.id.submit_busgovil);

		submit_egged.setEnabled(enabled);
		submit_busgovil.setEnabled(enabled);
	}

	private void runQuery(final BusGetter bg) {
		final Handler mHandler = new Handler();
		final TextView queryResult = (TextView) findViewById(R.id.query_result);
		final ProgressDialog dialog = ProgressDialog.show(this, "",
				getString(R.string.please_wait));

		final Runnable mUpdateResults = new Runnable() {
			@Override
			public void run() {
				queryResult.setText(lastResult);
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
						exceptionText, Toast.LENGTH_SHORT);
				toast.show();

				setButtonsEnabled(true);
			}
		};

		EditText queryEditText = (EditText) findViewById(R.id.query);
		final String query = queryEditText.getText().toString();

		setButtonsEnabled(false);

		Thread t = new Thread() {
			@Override
			public void run() {
				try {
					bg.runQuery(query);
					lastResult = bg.getFilteredResult();
					mHandler.post(mUpdateResults);
				} catch (IOException e) {
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
	}
}