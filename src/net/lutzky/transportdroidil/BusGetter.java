package net.lutzky.transportdroidil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public abstract class BusGetter {
	abstract String getUrl();

	abstract String getQueryJson(String query);

	private String rawResult = null;
	private String htmlResult = null;
	private String filteredResult = null;

	public void runQuery(String query) throws IOException {
		URL url = new URL(getUrl());

		HttpURLConnection hurl = (HttpURLConnection) url.openConnection();
		hurl.setRequestMethod("POST");
		hurl.setDoOutput(true);

		String appropriateReferer = getAppropriateReferer();
		if (appropriateReferer != null) {
			hurl.setRequestProperty("Referer", appropriateReferer);
		}

		hurl.setRequestProperty("Accept-Charset", "utf-8");
		hurl.setRequestProperty("Content-Type",
				"application/json; charset=utf-8");

		OutputStreamWriter wr = new OutputStreamWriter(hurl.getOutputStream());

		wr.write(getQueryJson(query));

		wr.flush();

		// We're only expecting one line of input anyway.
		rawResult = (new BufferedReader(new InputStreamReader(hurl
				.getInputStream()))).readLine();
	}

	public String getRawResult() {
		return rawResult;
	}

	public String getHtmlResult() throws JSONException {
		if (htmlResult == null) {
			JSONObject result = (JSONObject) new JSONTokener(rawResult).nextValue();
			htmlResult = result.getString("d");
		}

		return htmlResult;
	}

	public String getFilteredResult() throws JSONException {
		if (filteredResult == null) {
			filteredResult = getHtmlResult()
				.replace("<br>", "\n")
				.replace("<p>", "\n")
				.replace("<li>", "\n * ")
				.replace("&nbsp", " ")
				.replace("<BUS>", "")
				.replace("</BUS>", "")
				.replace("<COMPANY>", "")
				.replace("</COMPANY>", "")
				.replaceAll(" *(,? ) *", "$1")
				.replaceAll("^\n+", "")
				.replaceAll("(\n ?)+","\n");
		}

		return filteredResult;
	}

	abstract protected String getAppropriateReferer();
}