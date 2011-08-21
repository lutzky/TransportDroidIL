package net.lutzky.transportdroidil;

import org.json.JSONException;
import org.json.JSONObject;

public class EggedGetter extends BusGetter {
	public static final String url = "http://mslworld.egged.co.il/eggedtimetable/WebForms/wsUnicell.asmx/getAnswer";

	@Override
	String getQueryJson(String query) throws JSONException {
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("str1", query);
		jsonQuery.put("strSession", "0");
		return jsonQuery.toString();
	}
	
	@Override
	String getQueryJson(int interactionIndex) throws JSONException {
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("str1", interactionIndex);
		jsonQuery.put("strSession", "0");
		return jsonQuery.toString();
	}

	@Override
	protected String getAppropriateReferer() {
		return "http://mslworld.egged.co.il/eggedtimetable/WebForms/wfrmMain.aspx?state=3&company=1&language=he&freelang=+&width=1024";
	}

	@Override
	String getUrl() {
		return url;
	}

	@Override
	String getSessionPrefix() {
		return "";
	}
}
