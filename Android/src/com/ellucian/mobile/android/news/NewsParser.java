package com.ellucian.mobile.android.news;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ellucian.mobile.android.Utils;

public class NewsParser {


	public static List<NewsFeed> parse(String jString) throws JSONException {

		final List<NewsFeed> feeds = new ArrayList<NewsFeed>();
		final JSONObject jObject = new JSONObject(jString);

		final JSONArray feedsObj = jObject.getJSONArray("Feeds");
		for (int i = 0; i < feedsObj.length(); i++) {

			final NewsFeed feed = new NewsFeed();
			feeds.add(feed);
			feed.setTitle(feedsObj.getJSONObject(i).getString("Title"));
			final JSONArray items = feedsObj.getJSONObject(i).getJSONArray(
					"Items");
			for (int j = 0; j < items.length(); j++) {
				final NewsItem item = new NewsItem();
				feed.add(item);

				final JSONObject itemObj = items.getJSONObject(j);
				if (!itemObj.isNull("Content")) {
					item.setContent(itemObj.getString("Content"));
				}
				if (!itemObj.isNull("Date")) {
					item.setDate(Utils.convertJsonDate(itemObj.getString("Date")));
				}
				if (!itemObj.isNull("Image")) {
					item.setImage(itemObj.getString("Image"));
				}
				if (!itemObj.isNull("Title")) {
					item.setTitle(itemObj.getString("Title"));
				}
				if (!itemObj.isNull("Website")) {
					item.setWebsite(itemObj.getString("Website"));
				}

			}

		}

		return feeds;

	}
}