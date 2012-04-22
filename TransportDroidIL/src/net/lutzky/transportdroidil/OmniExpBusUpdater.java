package net.lutzky.transportdroidil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.XMLReader;

import android.text.Editable;
import android.text.Html;
import android.text.Html.TagHandler;
import android.util.Log;

public class OmniExpBusUpdater extends AbstractRealtimeBusUpdater {
	protected static final String TAG = "OmniExpBusUpdater";

	private final String id;
	private Date lastUpdateTime;
	private String routeNumber;
	private String routeTitle;
	private List<Stop> stops;
	private List<Eta> etas;
	private Float busPosition;
	private Date nextBus;
	
	private final DefaultHttpClient httpclient = new DefaultHttpClient();
	
	public OmniExpBusUpdater(String id) {
		this.id = id;
	}

	@Override
	public Date getLastUpdateTime() {
		return lastUpdateTime;
	}

	@Override
	public String getRouteNumber() {
		return routeNumber;
	}

	@Override
	public String getRouteTitle() {
		return routeTitle;
	}

	@Override
	public List<Stop> getStops() {
		return stops;
	}

	@Override
	public List<Eta> getEtas() {
		return etas;
	}

	@Override
	public Float getBusPosition() {
		return busPosition;
	}
	
	@Override
	public Date getNextBus() {
		return nextBus;
	}


	@Override
	public void update() throws ClientProtocolException, IOException {
		HttpGet request = new HttpGet(getUrl());
		HttpResponse response = httpclient.execute(request);
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new ClientProtocolException("" + response.getStatusLine());
		}
		String html = new String();
		BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = reader.readLine();
		while (line != null) {
			html += line;
			line = reader.readLine();
		}
		Log.d(TAG, "html: " + html);
		CharSequence scriptTag = getResponseScriptTag(html);
		parseScriptTag(scriptTag);
		notifyListeners();
	}

	private static final Pattern commandPattern = Pattern.compile("parent\\.frames\\[1\\]\\.(\\w+)\\s*?\\((.*?)\\)\\s*?");
	private void parseScriptTag(CharSequence scriptTag) {
		routeNumber = routeTitle = null;
		stops = new ArrayList<Stop>(stops != null ? stops.size() : 10);
		etas = null;
		busPosition = null;
		nextBus = null;
		
		Matcher m = commandPattern.matcher(scriptTag);
		while (m.find()) {
			String cmd = m.group(1);
			String[] args = parseArgs(m.group(2));
			Log.d(TAG, cmd);
			Log.d(TAG, m.group(2));
			if (cmd.equals("drawCaption") && args.length == 5 && getJSBool(args[0])) {
				routeNumber = getJSString(args[4]);
				routeTitle = getJSString(args[1]) + " - " + getJSString(args[2]);
			}
			else if (cmd.equals("drawStop") && args.length == 3) {
				stops.add(new Stop(getJSString(args[0]), getJSFloat(args[1])));
			}
			else if (cmd.equals("drawBus") && args.length == 3) {
				busPosition = getJSFloat(args[0]);
			}
			else if (cmd.equals("setNextBus") && args.length == 2) {
				nextBus = getJSTime(args[1]);
			}
		}
	}

	private Date getJSTime(String string) {
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

	private float getJSFloat(String string) {
		// Remove quotes if necessary.
		string = getJSString(string);
		return Float.parseFloat(string);
	}

	private String getJSString(String string) {
		string = parseUnicode(string);
		if (string.startsWith("'") && string.endsWith("'"))
			return string.substring(1, string.length() - 1);
		else if (string.startsWith("\"") && string.endsWith("\""))
			return string.substring(1, string.length() - 1);
		else
			return string;
	}
	
	private static final Pattern unicodeCharEscape = Pattern.compile("\\\\u([0-9a-fA-F]{4})");
	private String parseUnicode(String string) {
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

	private boolean getJSBool(String string) {
		return string != null && string.equals("true");
	}

	private String[] parseArgs(String args) {
		String[] res = args.split(",");
		if (res.length == 1 && res[0].length() == 0)
			return new String[0];
		else
			return res;
	}

	private CharSequence getResponseScriptTag(String html) {
		final StringBuilder result = new StringBuilder(); 
		Html.fromHtml(html, null, new TagHandler() {
			@Override
			public void handleTag(boolean opening, String tag, Editable output,
					XMLReader xmlReader) {
				if (tag.equals("script")) {
					if (!opening)
						result.append(output);
				}
			}
		});
		return result;
	}

	private String getUrl() {
		return "http://213.8.94.157/nateev/servlet/com.isrfleettrack.RouteDataServlet?selectedCompany=3&firstTime=false&selectedRoute=" + id;
	}
}
