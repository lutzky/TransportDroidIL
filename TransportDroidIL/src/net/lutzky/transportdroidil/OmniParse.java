package net.lutzky.transportdroidil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Log;

public class OmniParse {
	private static final String TAG = "OmniParse";
	
	public static Date getJSTime(String string) {
		// Remove quotes if necessary.
		string = getJSString(string);
		DateFormat timeFormat = new SimpleDateFormat("HH:mm");
		try {
			return timeFormat.parse(string);
		} catch (ParseException e) {
			Log.d(TAG, "Unable to parse time: " + string);
			return null;
		}
	}

	public  static float getJSFloat(String string) {
		// Remove quotes if necessary.
		string = getJSString(string);
		return Float.parseFloat(string);
	}

	public static String getJSString(String string) {
		string = parseUnicode(string);
		if (string.startsWith("'") && string.endsWith("'"))
			return string.substring(1, string.length() - 1);
		else if (string.startsWith("\"") && string.endsWith("\""))
			return string.substring(1, string.length() - 1);
		else
			return string;
	}
	
	private static final Pattern unicodeCharEscape = Pattern.compile("\\\\u([0-9a-fA-F]{4})");
	public static String parseUnicode(String string) {
		StringBuilder result = new StringBuilder();
		Matcher m = unicodeCharEscape.matcher(string);
		int prev = 0;
		while (m.find()) {
			result.append(string.substring(prev, m.start()));
			char chr = Character.toChars(Integer.parseInt(m.group(1), 16))[0];
			result.append(chr);
			prev = m.end();
		}
		result.append(string.substring(prev));
		return result.toString();
	}

	public static boolean getJSBool(String string) {
		return string != null && string.equals("true");
	}

	public static String[] parseArgs(String args) {
		String[] res = args.split(",");
		if (res.length == 1 && res[0].length() == 0)
			return new String[0];
		else
			return res;
	}
	
	public static CharSequence downloadUrl(String url) throws ClientProtocolException, IOException {
		BufferedReader reader = downloadStream(url);
		StringBuilder result = new StringBuilder();
		String line = reader.readLine();
		while (line != null) {
			result.append(line);
			result.append('\n');
			line = reader.readLine();
		}
		return result;
	}

	public static BufferedReader downloadStream(String url)
			throws IOException, ClientProtocolException {
		HttpGet request = new HttpGet(url);
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpResponse response = httpclient.execute(request);
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new ClientProtocolException("" + response.getStatusLine());
		}
		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		return reader;
	}
}
