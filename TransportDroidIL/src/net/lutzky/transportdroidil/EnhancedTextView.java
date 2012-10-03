package net.lutzky.transportdroidil;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
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

	/*
	 * completionOptions shouldn't really be necessary - we should've been
	 * able to read the data via the ArrayAdapter - but it always looks empty
	 * when you try to read it. Weird.
	 */
	List<String> completionOptions = new LinkedList<String>();
	final String preferencesFieldName;
	
	public EnhancedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		preferencesFieldName = attrs.getAttributeValue(XMLNS, "preferences_field_name");
		if (preferencesFieldName == null)
			Log.w(TAG, "No preferences_field_name attribute, auto complete will not be persistent.");
	}
	
	/**
	 * Set the list used to save completion options.
	 * Notice that the list is not copied but used as reference!
	 * 
	 * @param completionOptions the list to hold the options.
	 */
	public void setCompletionOptions(List<String> completionOptions) {
		this.completionOptions = completionOptions;
	}
	public List<String> getCompletionOptions() {
		return completionOptions;
	}
	
	public void addCurrentValueAsCompletion() {
		String s = getText().toString().trim();

		if (s.equals("") || s.equals(getHint())) {
			Log.d(TAG, "Ignoring empty completion value");
			return;
		}

		@SuppressWarnings("unchecked")
		ArrayAdapter<String> arrayAdapter = (ArrayAdapter<String>) getAdapter();

		// If the completion was already there, remove and re-add it. This way,
		// it gets moved to the front.
		int previousLocation = completionOptions.indexOf(s);
		if (previousLocation != -1) {
			completionOptions.remove(previousLocation);
			arrayAdapter.remove(s);
		}

		// Add our completion to the actual active completions database
		arrayAdapter.insert(s, 0);

		// Add our completion to our to-be-persistent storage
		completionOptions.add(0, s);

		Log.d(TAG, "Current completion options: " + completionOptions.toString());
	}

	public void clearCompletionOptions(SharedPreferences settings) {
		completionOptions.clear();
		savePersistentState(settings);
		@SuppressWarnings("unchecked")
		ArrayAdapter<String> arrayAdapter = (ArrayAdapter<String>)getAdapter();
		arrayAdapter.clear();
	}

	public void savePersistentState(SharedPreferences settings) {
		if (preferencesFieldName == null)
			return;
		
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
		editor.putString(preferencesFieldName, buffer.toString());

		Log.d(TAG, preferencesFieldName + ": Saving persistent state: " + buffer.toString());
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
	
	public void loadPersistentState(SharedPreferences settings) {
		if (preferencesFieldName == null)
			return;
		
		String allQueries = settings.getString(preferencesFieldName, "");
		for (String query : allQueries.split(SEPARATOR)) {
			completionOptions.add(query);
		}

		Log.d(TAG, "Loaded persistent queries for " + preferencesFieldName + ": " + completionOptions.toString());
		@SuppressWarnings("unchecked")
		ArrayAdapter<String> arrayAdapter = (ArrayAdapter<String>) getAdapter();
		arrayAdapter.clear();
		// no addAll until API 11
		for (String s : completionOptions)
			arrayAdapter.add(s);
	}

	public String getString() {
		String text = getText().toString();
		if (text.equals(getHint()))
			return "";
		else
			return text;
	}
}
