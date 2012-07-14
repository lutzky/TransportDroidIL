package net.lutzky.transportdroidil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.XMLReader;

import android.text.Editable;
import android.text.Html;
import android.text.Html.TagHandler;
import android.util.Log;

public class OmniExpBusUpdater implements RealtimeBusUpdater {
	protected static final String TAG = "OmniExpBusUpdater";

	private final String id;
	private Date lastUpdateTime;
	private boolean serviceActive = true;
	private String routeNumber;
	private String routeTitle;
	private List<Stop> stops;
	private List<Eta> etas;
	private List<Bus> buses;
	
	public OmniExpBusUpdater(String id) {
		this.id = id;
	}

	@Override
	public Date getLastUpdateTime() {
		return lastUpdateTime;
	}
	
	@Override
	public boolean isServiceActive() {
		return serviceActive;
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
	public List<Bus> getBuses() {
		return buses;
	}
	
	@Override
	public void update() throws ClientProtocolException, IOException {
		String html = OmniParse.downloadUrl(getUrl()).toString();
		Log.d(TAG, "html: " + html);
		CharSequence scriptTag = getResponseScriptTag(html);
		parseScriptTag(scriptTag);
	}

	private static final Pattern commandPattern = Pattern.compile("parent\\.frames\\[1\\]\\.(\\w+)\\s*?\\((.*?)\\)\\s*?");
	private void parseScriptTag(CharSequence scriptTag) {
		lastUpdateTime = null;
		routeNumber = routeTitle = null;
		serviceActive = true;
		stops = new ArrayList<Stop>(stops != null ? stops.size() : 10);
		etas = new ArrayList<Eta>(etas != null ? etas.size() : 10);
		buses = new ArrayList<Bus>(buses != null ? buses.size() : 1);
		
		Matcher m = commandPattern.matcher(scriptTag);
		while (m.find()) {
			String cmd = m.group(1);
			String[] args = OmniParse.parseArgs(m.group(2));
			Log.d(TAG, cmd);
			Log.d(TAG, m.group(2));
			float position;
			boolean direction;
			
			if (cmd.equals("drawCaption") && args.length == 5 && OmniParse.getJSBool(args[0])) {
				routeNumber = OmniParse.getJSString(args[4]);
				routeTitle = OmniParse.getJSString(args[1]) + " - " + OmniParse.getJSString(args[2]);
			}
			else if (cmd.equals("drawStop") && args.length == 3) {
				stops.add(new Stop(OmniParse.getJSString(args[0]), OmniParse.getJSFloat(args[1])));
			}
			else if (cmd.equals("drawBus") && args.length == 3) {
				position = OmniParse.getJSFloat(args[0]);
				direction = OmniParse.getJSBool(args[1]);
				buses.add(new Bus(direction, position));
			}
			else if (cmd.equals("setNextBus") && args.length == 2) {
//				TODO put the next bus info somewhere interesting.
//				direction = OmniParse.getJSBool(args[0]);
//				Date when = OmniParse.getJSTime(args[1]);
//				etas.add(new Eta(direction, direction ? -1 : 2, when));
			}
			else if (cmd.equals("setTime") && args.length == 1) {
				lastUpdateTime = OmniParse.getJSTime(args[0]);
			}
			else if (cmd.equals("setEta") && args.length == 3) {
				direction = OmniParse.getJSBool(args[0]);
				position = OmniParse.getJSFloat(args[1]);
				Date eta = OmniParse.getJSTime(args[2]);
				etas.add(new Eta(direction, position, eta));				
			}
			else if (cmd.equals("endRoute") && args.length == 1) {
				// args[0] == direction
				serviceActive = false;
			}
		}
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
