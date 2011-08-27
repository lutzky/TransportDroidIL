package net.lutzky.transportdroidil;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

/**
 * TextView that has persistent auto-complete.
 */
public class EnhancedTextView extends AutoCompleteTextView {
	private static final int MAX_HISTORY = 10000;
	private static final String SEPARATOR = "\n",
						 TAG = "EnhancedTextView",
						 XMLNS = "http://transportdroidil.lutzky.net/apk/res/custom";

	final String preferencesFieldName;
	
	public EnhancedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		preferencesFieldName = attrs.getAttributeValue(XMLNS, "preferences_field_name");
		if (preferencesFieldName == null)
			Log.w(TAG, "No preferences_field_name attribute, auto complete will not be persistent.");
	}
	
	/*
	 * Usually, for non-single-line TextEdit views, the IME action is
	 * disabled. However, single-line TextEdit views have a bug with many ROMs
	 * that causes them not to show Hebrew hints. This @Override causes the
	 * IME action to show up anyway, if it is selected.
	 */
	@Override
	public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
	    InputConnection connection = super.onCreateInputConnection(outAttrs);
	    int imeAction = outAttrs.imeOptions & EditorInfo.IME_MASK_ACTION;
	    if (imeAction != EditorInfo.IME_ACTION_NONE && imeAction != EditorInfo.IME_ACTION_UNSPECIFIED) {
	        outAttrs.imeOptions &= ~EditorInfo.IME_FLAG_NO_ENTER_ACTION;
	    }

	    return connection;
	}

	public void addCurrentValueAsCompletion() {
		String s = getText().toString();

		@SuppressWarnings("unchecked")
		ArrayAdapter<String> arrayAdapter = (ArrayAdapter<String>) getAdapter();

		Log.d(TAG, "Adding completion: " + s);

		// Remove the string so there are no duplicates.
		arrayAdapter.remove(s);

		// Insert at beginning of the list - this is a recent entry, we want
		// it to show up at the top.
		arrayAdapter.insert(s, 0);
	}

	public void clearCompletionOptions(SharedPreferences settings) {
		savePersistentState(settings);
		@SuppressWarnings("unchecked")
		ArrayAdapter<String> arrayAdapter = (ArrayAdapter<String>)getAdapter();
		arrayAdapter.clear();
	}

	public void savePersistentState(SharedPreferences settings) {
		if (preferencesFieldName == null)
			return;
		
		@SuppressWarnings("unchecked")
		ArrayAdapter<String> arrayAdapter = (ArrayAdapter<String>)getAdapter();

		SharedPreferences.Editor editor = settings.edit();
		StringBuilder buffer = new StringBuilder();

		for (int i = 0; i < Math.min(arrayAdapter.getCount(), MAX_HISTORY); ++i) {
			buffer.append(arrayAdapter.getItem(i));
			buffer.append(SEPARATOR);
		}
		try {
			buffer.deleteCharAt(buffer.length() - 1);
		} catch (Exception e) {
		}
		editor.putString(preferencesFieldName, buffer.toString());
		editor.commit();
	}
	
	public void loadPersistentState(SharedPreferences settings) {
		if (preferencesFieldName == null)
			return;

		@SuppressWarnings("unchecked")
		ArrayAdapter<String> arrayAdapter = (ArrayAdapter<String>) getAdapter();
		arrayAdapter.clear();

		String allQueries = settings.getString(preferencesFieldName, "");
		for (String query : allQueries.split(SEPARATOR)) {
			// No addAll until API 11
			arrayAdapter.add(query);
		}
	}
}
