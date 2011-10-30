package net.lutzky.transportdroidil;

import org.json.JSONException;
import org.json.JSONObject;

public class BusGovIlGetter extends BusGetter {
	public static final String url = "http://bus.gov.il/WebForms/wsUnicell.asmx/getAnswerMot";
	@Override
	String getQueryJson(String query) throws JSONException {
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("sParams", query);
		jsonQuery.put("strSession", "0");
		return jsonQuery.toString();
	}
	
	@Override
	String getQueryJson(int interactionIndex) throws JSONException {
		JSONObject jsonQuery = new JSONObject();
		jsonQuery.put("sParams", interactionIndex);
		jsonQuery.put("strSession", "0");
		return jsonQuery.toString();
	}

	@Override
	String getUrl() {
		return url;
	}

	@Override
	protected String getAppropriateReferer() {
		return "http://bus.gov.il/WebForms/wfrmMain.aspx?width=1024&company=1&language=he&state=";
	}

	@Override
	String getSessionPrefix() {
		return "mot_";
	}
}
