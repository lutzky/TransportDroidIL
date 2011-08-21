package net.lutzky.transportdroidil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xml.sax.XMLReader;

import android.text.Editable;
import android.text.Html;
import android.text.Html.TagHandler;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.text.Spanned;
import android.util.Log;
import android.view.View;

public abstract class BusGetter {
	protected static final String TAG = "BusGetter";

	abstract String getUrl();

	abstract String getQueryJson(String query) throws JSONException;
	abstract String getQueryJson(int interactionIndex) throws JSONException;
	abstract String getSessionPrefix();

	private String rawResult = null;
	private String htmlResult = null;
	private Spanned filteredResult = null;
	
	public BusGetter() {
		newSession();
	}
	
	public interface InteractiveLinkClicked {
		void onInteractiveLinkClicked(BusGetter bg, int index);
	}
	
	private InteractiveLinkClicked interactiveLinkedClicked = null;
	protected String session = "", cookie = "";

	public void runQuery(String query) throws IOException, JSONException {
		runQuery(query, 0);
	}
	
	public void runQuery(String query, int interactionIndex) throws IOException, JSONException {
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
		hurl.setRequestProperty("Cookie", cookie);

		OutputStreamWriter wr = new OutputStreamWriter(hurl.getOutputStream());

		String jsonQuery;
		if (interactionIndex == 0)
			jsonQuery = getQueryJson(query);
		else
			jsonQuery = getQueryJson(interactionIndex);
		wr.write(jsonQuery);
		Log.d(TAG, "Sending JSON query: " + jsonQuery);

		wr.flush();

		if (hurl.getResponseCode() != 200) {
			throw new IOException("" + hurl.getResponseCode() + " " + hurl.getResponseMessage());
		}

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

	public Spanned getFilteredResult() throws JSONException {
		if (filteredResult == null) {
			Log.d(TAG, "Filtering: " + getHtmlResult());
			Spanned temp = Html.fromHtml(getHtmlResult()
//				.replace("<br>", "\n")
//				.replace("<p>", "\n")
//				.replace("<li>", "\n * ")
//				.replace("&nbsp", " ")
//				.replace("<BUS>", "")
//				.replace("</BUS>", "")
//				.replace("<COMPANY>", "")
//				.replace("</COMPANY>", "")
//				.replaceAll(" *(,? ) *", "$1")
//				.replaceAll("^\n+", "")
//				.replaceAll("(\n ?)+","\n");
					, null, new TagHandler() {
						@Override
						public void handleTag(boolean opening, String tag, Editable output,
								XMLReader xmlReader) {
							if (opening) {
								if (tag.equals("li"))
									output.append("\n * ");
								else
									Log.d(TAG, "Found tag: " + tag);
							}
						}
					});
			
			Log.d(TAG, "Output: " + temp);
			
			SpannableStringBuilder builder = new SpannableStringBuilder(temp);
			URLSpan[] links = builder.getSpans(0, temp.length(), URLSpan.class);
			for (URLSpan link : links) {
				final int start = builder.getSpanStart(link),
						  end   = builder.getSpanEnd(link),
						  flags = builder.getSpanFlags(link),
						  interactionIndex = getInteractionIndex(link.getURL()); 
				builder.removeSpan(link);
				if (interactionIndex == 0)
					continue;
				ClickableSpan newLink = new ClickableSpan() {
					@Override
					public void onClick(View widget) {
						if (interactiveLinkedClicked != null)
							interactiveLinkedClicked.onInteractiveLinkClicked(BusGetter.this, interactionIndex);
					}
				};
				builder.setSpan(newLink, start, end, flags);
			}
			
			filteredResult = builder;
		}

		return filteredResult;
	}

	private int getInteractionIndex(String url) {
		Pattern re = Pattern.compile("javascript:UnicellInteraction\\((\\d+)\\)");
		Matcher m = re.matcher(url);
		if (m.find())
			return Integer.parseInt(m.group(1));
		else
			return 0;
	}

	abstract protected String getAppropriateReferer();

	public void setInteractiveLinkedClicked(InteractiveLinkClicked interactiveLinkedClicked) {
		this.interactiveLinkedClicked = interactiveLinkedClicked;
	}

	public InteractiveLinkClicked getInteractiveLinkedClicked() {
		return interactiveLinkedClicked;
	}

	private String randomSession() {
		StringBuilder b = new StringBuilder(getSessionPrefix());
		Random r = new Random();
		for (int i = 0; i < 25; ++i) {
			int randomChar = r.nextInt(36);
			char c;
			if (randomChar < 10)
				c = (char) ('0' + randomChar);
			else
				c = (char) ('a' + randomChar - 10);
			b.append(c);
		}
		
		return b.toString();
	}
	
	private void newSession() {
		try {
			URL url = new URL(getAppropriateReferer());
	
			HttpURLConnection hurl = (HttpURLConnection) url.openConnection();
			hurl.setRequestMethod("GET");
	
			hurl.setRequestProperty("Accept-Charset", "utf-8");
			hurl.setRequestProperty("Content-Type",
					"text/html; charset=utf-8");
			
			if (hurl.getResponseCode() != 200) {
				throw new IOException("" + hurl.getResponseCode() + " " + hurl.getResponseMessage());
			}
			
			Scanner scanner = new Scanner(hurl.getInputStream());
			scanner.findWithinHorizon("<input name=\"hidSessioId\" type=\"hidden\" id=\"hidSessioId\" value=\"(.*)\" />", 0);
			MatchResult m = scanner.match();
			session = m.group(1);
			cookie = hurl.getHeaderField("Set-Cookie");
			Log.d(TAG, "Session: " + session);
			Log.d(TAG, "Cookie: " + cookie);
		}
		catch (IOException e) {
			Log.d(TAG, "Exception trying to create a session.");
		}
	}
}