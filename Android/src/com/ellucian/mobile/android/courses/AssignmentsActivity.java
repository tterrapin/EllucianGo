package com.ellucian.mobile.android.courses;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ellucian.mobile.android.R;
import com.ellucian.mobile.android.DataCache;
import com.ellucian.mobile.android.EllucianApplication;
import com.ellucian.mobile.android.UICustomizer;
import com.ellucian.mobile.android.auth.LoginUtil;

public class AssignmentsActivity extends ListActivity {
	private class AssignmentsAdapter extends BaseAdapter {
		private final List<Assignment> items;
		private final LayoutInflater mInflater;

		public AssignmentsAdapter(Context context, List<Assignment> items) {
			this.items = items;
			mInflater = LayoutInflater.from(context);
		}

		public int getCount() {
			return items.size();
		}

		public Object getItem(int position) {
			return items.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final Assignment item = items.get(position);
			convertView = mInflater
					.inflate(R.layout.assignments_list_row, null);
			final TextView title = (TextView) convertView
					.findViewById(R.id.assignmentTitle);
			final TextView dueDate = (TextView) convertView
					.findViewById(R.id.assignmentDueDate);
			final TextView description = (TextView) convertView
					.findViewById(R.id.assignmentTeaser);
			title.setText(item.getName());
			description.setText(item.getDescription());
			
			if(item.getDueDate() != null) {
				final Date date = item.getDueDate().getTime();
				final java.text.DateFormat dateFormat = android.text.format.DateFormat
					.getDateFormat(getApplicationContext());
				final java.text.DateFormat timeFormat = android.text.format.DateFormat
					.getTimeFormat(getApplicationContext());
				dueDate.setText(dateFormat.format(date) + " "
						+ timeFormat.format(date));
				
			}
			return convertView;
		}
	}

	private class UpdateAssignmentsTask extends
			AsyncTask<String, Void, List<Assignment>> {
		private final Context context;
		private boolean updateList = false;

		public UpdateAssignmentsTask(Context context) {
			this.context = context;
		}

		@Override
		protected List<Assignment> doInBackground(String... urls) {
			try {
				refreshInProgress = true;
				final HttpClient client = new DefaultHttpClient();
				final HttpGet request = new HttpGet();
				request.setURI(new URI(urls[0]));
				final String username = LoginUtil.getUsername(context);
				final String password = LoginUtil.getPassword(context);
				request.addHeader(BasicScheme.authenticate(
						new UsernamePasswordCredentials(username, password),
						"UTF-8", false));
				final HttpResponse response = client.execute(request);
				final int status = response.getStatusLine().getStatusCode();
				if (status == HttpStatus.SC_OK) {
					final BufferedReader in = new BufferedReader(
							new InputStreamReader(response.getEntity()
									.getContent(), "UTF-8"));
					final StringBuffer sb = new StringBuffer();
					String line = "";
					final String NL = System.getProperty("line.separator");
					while ((line = in.readLine()) != null) {
						sb.append(line + NL);
					}
					in.close();
					final String tempJsonAssignments = sb.toString();
					if (!tempJsonAssignments.equals(jsonAssignments)) {
						updateList = true;
					}
					jsonAssignments = tempJsonAssignments;
					assignments = AssignmentsParser.parse(jsonAssignments);
					((EllucianApplication) getApplication())
							.getDataCache()
							.putAuthCache(urls[0], jsonAssignments, assignments);
					return assignments;
				} else {
					throw new RuntimeException(response.getStatusLine()
							.toString());
				}
			} catch (final Exception e) {
				Log.e(EllucianApplication.TAG, "Assignments update failed = "
						+ e);
			}
			return null;
		}

		@Override
		protected void onPostExecute(List<Assignment> notifications) {
			if (updateList) {
				setList();
			}
			refreshInProgress = false;
			UICustomizer.setProgressBarVisible(AssignmentsActivity.this, false);
		}
	}


	private ListAdapter adapter;
	private List<Assignment> assignments;
	private String jsonAssignments;
	private boolean refreshInProgress;
	private AsyncTask<String, Void, List<Assignment>> task;
	private String url;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.assignments_list);
		final Intent intent = getIntent();
		url = intent.getStringExtra("assignmentsUrl");
		final Course course = intent.getParcelableExtra("course");
		setTitle(course.getName() + " " + getResources().getString(R.string.assignments));
		UICustomizer.style(this);
		if (course.getSectionTitle() == null) {
			findViewById(R.id.sectionTitle).setVisibility(View.GONE);
		} else {
			((TextView) findViewById(R.id.sectionTitle)).setText(course
					.getSectionTitle());
		}
			assignments = new ArrayList<Assignment>();
			final DataCache cache = ((EllucianApplication) getApplication())
					.getDataCache();
			final boolean current = cache.isCurrent(this, url);
			final String cachedContent = cache.getCache(this, url);
			if (cachedContent != null) {
				try {
					jsonAssignments = cachedContent;
					final Object o = cache.getCacheObject(url);
					if (o != null && o instanceof List) {
						assignments = (List<Assignment>) o;
					} else {
						assignments = AssignmentsParser.parse(jsonAssignments);
						cache.putCacheObject(url, assignments);
					}
					setList();
				} catch (final JSONException e) {
					Log.e(EllucianApplication.TAG,
							"Can't parse json in assignments");
				}
			}
			if (!current) {
				update();
			}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.assignments, menu);
		return true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (task != null && refreshInProgress) {
			task.cancel(true);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		final Intent intent = new Intent(AssignmentsActivity.this,
				AssignmentDetailActivity.class);
		final Assignment assignment = (Assignment) adapter.getItem(position);
		intent.putExtra("assignment", assignment);
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			update();
			break;
		}
		return false;
	}

	private void setList() {
		adapter = new AssignmentsAdapter(this, assignments);
		setListAdapter(adapter);
	}

	private void update() {
		UICustomizer.setProgressBarVisible(AssignmentsActivity.this, true);
		task = new UpdateAssignmentsTask(this);
		task.execute(url);
	}
}
