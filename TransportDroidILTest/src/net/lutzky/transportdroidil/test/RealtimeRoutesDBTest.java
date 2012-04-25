package net.lutzky.transportdroidil.test;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;
import net.lutzky.transportdroidil.RealtimeRoutesOpenHelper;

public class RealtimeRoutesDBTest extends AndroidTestCase {
	private RealtimeRoutesOpenHelper helper;

	@Override
	protected void setUp() throws Exception {
		helper = new RealtimeRoutesOpenHelper(getContext());
	}
	
	public void testDB() {
		SQLiteDatabase db = helper.getWritableDatabase();
		db.execSQL("delete from routes where company == 'mock';");
		db.execSQL("insert into routes (company, id, variant, number) values ('mock', '1', '2', '3');");
		db.close();
		
		db = helper.getReadableDatabase();
		Cursor cursor = db.query(
				"routes", 
				new String[] { "company", "id", "variant", "number" }, 
				"company = ?", 
				new String[] { "mock" }, 
				null, 
				null, 
				null);
		assertTrue(cursor.moveToFirst());
		assertEquals("mock", cursor.getString(0));
		assertEquals("1", cursor.getString(1));
		assertEquals("2", cursor.getString(2));
		assertEquals("3", cursor.getString(3));
		cursor.close();
		db.close();
		
		db = helper.getWritableDatabase();
		db.execSQL("delete from routes where company == 'mock';");
		cursor = db.query(
				"routes", 
				new String[] { "company", "id", "variant", "number" }, 
				"company = ?", 
				new String[] { "mock" }, 
				null, 
				null, 
				null);
		assertFalse(cursor.moveToFirst());
		cursor.close();
		db.close();
	}
	
	public void testFillOmniExpress() throws ClientProtocolException, IOException {
		helper.fillOmniExpress();
		SQLiteDatabase db = helper.getReadableDatabase();
		//Cursor cursor = db.query("routes", new String[] { "company", "number", "variant_index", "variant", "id" }, null, null, null, null, "company, number, variant_index");
		Cursor cursor = db.query(
				"routes", 
				new String[] { "company", "number", "variant_index", "variant", "id" }, 
				null, 
				null, 
				null, 
				null, 
				null);
		StringBuilder builder = new StringBuilder();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			builder.setLength(0);
			for (int i = 0; i < 5; ++i) {
				builder.append(cursor.getString(i));
				if (i != 4) builder.append(", ");
			}
			Log.d("Test", builder.toString());
			cursor.moveToNext();
		}
	}
}
