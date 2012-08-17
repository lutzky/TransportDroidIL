package net.lutzky.transportdroidil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xml.sax.XMLReader;

import android.text.Editable;
import android.text.Html;
import android.text.Html.TagHandler;
import android.text.Spannable;
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
	
	String getBusUrl(CharSequence bus) {
		return "http://www.bus.co.il/otobusim/Front2007/Lines.asp?LineName=" + bus;
	}

	private String rawResult = null;
	private String htmlResult = null;
	private Spanned filteredResult = null;
	
	private final DefaultHttpClient httpclient = new DefaultHttpClient();
	private boolean hasSessionCookies = false;
	
	public interface InteractiveLinkClicked {
		void onInteractiveLinkClicked(BusGetter bg, int index);
	}
	
	private InteractiveLinkClicked interactiveLinkedClicked = null;

	public void runQuery(String query) throws IOException, JSONException {
		runQuery(query, 0);
	}
	
	public void runQuery(String query, int interactionIndex) throws IOException, JSONException {
		if (!hasSessionCookies) {
			newSession();
			hasSessionCookies = true;
		}
		
		HttpPost httppost = new HttpPost(getUrl());

		String appropriateReferer = getAppropriateReferer();
		if (appropriateReferer != null) {
			httppost.setHeader("Referer", appropriateReferer);
		}

		String jsonQuery;
		if (interactionIndex == 0)
			jsonQuery = getQueryJson(query);
		else
			jsonQuery = getQueryJson(interactionIndex);
		StringEntity entity = new StringEntity(jsonQuery, "utf-8");
		entity.setContentType("application/json; charset=utf-8");
		httppost.setEntity(entity);
		Log.d(TAG, "Sending JSON query: " + jsonQuery);
		
		HttpResponse response = httpclient.execute(httppost);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new IOException("" + response.getStatusLine());
		}

		// We're only expecting one line of input anyway.
		rawResult = (new BufferedReader(new InputStreamReader(response.getEntity().getContent()))).readLine();
		htmlResult = null;
		filteredResult = null;
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
					, null, new TagHandler() {
						@Override
						public void handleTag(boolean opening, String tag, Editable output,
								XMLReader xmlReader) {
							// http://stackoverflow.com/questions/4044509/android-how-to-use-the-html-taghandler
							if (tag.equals("BUS"))
								processBus(opening, output);
							else if (opening) {
								if (tag.equals("li"))
									output.append("\n • ");
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
				if (interactionIndex == 0)
					continue;
				builder.removeSpan(link);
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

	private void processBus(boolean opening, Editable output) {
		int len = output.length();
        if (opening) {
            output.setSpan(new Object(), len, len, Spannable.SPAN_MARK_MARK);
        } else {
            Object obj = getLast(output, Object.class);
            int where = output.getSpanStart(obj);

            output.removeSpan(obj);

            if (where != len) {
            	CharSequence bus = output.subSequence(where, len);
            	String url = getBusUrl(bus);
            	Log.d(TAG, "Adding bus url: " + url);
                output.setSpan(new URLSpan(url), where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
	}

	private <T> T getLast(Editable text, Class<T> kind) {
        T[] objs = text.getSpans(0, text.length(), kind);

        if (objs.length == 0) {
            return null;
        } else {
            for(int i = objs.length;i>0;i--) {
                if(text.getSpanFlags(objs[i-1]) == Spannable.SPAN_MARK_MARK) {
                    return objs[i-1];
                }
            }
            return null;
        }
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
	
	private void newSession() throws IOException {
		HttpGet httpget = new HttpGet(getAppropriateReferer());
		HttpResponse response = httpclient.execute(httpget);

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new IOException("" + response.getStatusLine());
		}
		
		HttpEntity entity = response.getEntity();
		if (entity != null)
			entity.consumeContent();
	}
}