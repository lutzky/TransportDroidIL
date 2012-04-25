package net.lutzky.transportdroidil;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RealtimeRoutesOpenHelper extends SQLiteOpenHelper {
	private static final int VERSION = 2;
	private static final String NAME = "realtime_routes";
	private static final String TAG = "RealtimeRoutesDB";
	
	public RealtimeRoutesOpenHelper(Context context) {
		super(context, NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(
			"create table routes (" +
				"company TEXT, " +
				"id TEXT, " +
				"variant TEXT, " +
				"variant_index INT, " +  
				"number TEXT" +
			");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 1 && newVersion >= 2)
			db.execSQL("alter table routes add column variant_index INT");
	}
	
	static private final String OMNI_URL = "http://213.8.94.157/nateev/route.jsp?rtNum=01501097&&com=omni&eshkol=97";
	public void fillOmniExpress() throws ClientProtocolException, IOException {
		SQLiteDatabase db = getWritableDatabase();
		try {
			db.beginTransaction();
			try {
				db.execSQL("delete from routes where company == 'OmniExpress';");
		
				BufferedReader reader = OmniParse.downloadStream(OMNI_URL);
				parseAlternatives(reader, db);
			
				db.setTransactionSuccessful();
			}
			finally {
				db.endTransaction();
			}
		}
		finally { 
			db.close();
		}
	}


	private static final Pattern CHALUFOT_BEGIN = Pattern.compile("^function loadChalufot\\(\\)");
	private static final Pattern CHALUFOT_END = Pattern.compile("^\\}");
	private static final Pattern ROUTE_ID_PATTERN = Pattern.compile("if\\(rtSelected == '(.*)'\\)\\{");
	private static final Pattern ROUTE_VARIANT_PATTERN = Pattern.compile("routeSelect\\.options\\[([0-9]+)\\] = new Option\\('(.*)','(.*)'\\);");
	private void parseAlternatives(BufferedReader reader, SQLiteDatabase db) throws IOException {
		ContentValues row = new ContentValues();
		String line = reader.readLine();
		boolean afterBegin = false;
		while (line != null) {
			if (!afterBegin) {
				Matcher m = CHALUFOT_BEGIN.matcher(line);
				if (!m.find()) {
					line = reader.readLine();
					continue;
				}
				afterBegin = true;
				line = reader.readLine();
				continue;
			}
			else {
				Matcher m = CHALUFOT_END.matcher(line);
				if (m.find())
					return;
				m = ROUTE_ID_PATTERN.matcher(line);
				if (m.find()) {
					row.put("id", m.group(1));
					line = reader.readLine();
					continue;
				}
				m = ROUTE_VARIANT_PATTERN.matcher(line);
				if (m.find()) {
					int index = Integer.parseInt(m.group(1));
					row.put("variant_index", index);
					String id;
					if (index != 0) {
						id = m.group(3);
						row.put("id", id);
					}
					else
						id = row.getAsString("id");
					if (id != null) {
						row.put("variant", OmniParse.getJSString(m.group(2)));
						row.put("company", id.substring(6));
						row.put("number", lineNumber(id));
						db.insert("routes", null, row);
						Log.d(TAG, "Inserted row: " + row);
					}
					line = reader.readLine();
					continue;
				}
				line = reader.readLine();
			}
		}
	}

	private String lineNumber(String id) {
		StringBuilder result = new StringBuilder(id.subSequence(0, 4));
		int firstNonZero, lastNonZero;
		for (firstNonZero = 0; firstNonZero < result.length() && result.charAt(firstNonZero) == '0'; ++firstNonZero)
			;
		result.delete(0, firstNonZero);
		for (lastNonZero = result.length() - 1; lastNonZero >= 0 && result.charAt(lastNonZero) == '0'; --lastNonZero)
			;
		result.delete(lastNonZero + 1, result.length());
		char lastChar = result.charAt(result.length()-1);
		if (!Character.isDigit(lastChar))
			// translate to Hebrew
			result.setCharAt(result.length() - 1, (char) (lastChar - 'A' + 'א'));
		return result.toString();
	}
}
