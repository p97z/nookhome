/**
 * 
 */
package com.example.com.gvccracing.android.nookhome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * @author p97z
 * Class used to read all applications and then launch one
 */
public class LaunchApplication extends Activity {

	private HashMap<String, Drawable> launchIcons;
	private List<String> launchApplications ;
	private GridView launchGridView;
	private ApplicationLauncherAdapter adapter;

	/**
	 * 
	 * @author p97z
	 * view holder used to hold the icon and the text
	 */
	static class ViewHolder {
		TextView tv;
		ImageView iv;
	}

	/**
	 * 
	 * @author p97z
	 *
	 */
	class LaunchArrayAdapter extends ArrayAdapter<String> {
		LaunchArrayAdapter(Context context, int resource, List<String> data) {
			super(context, resource, data);
		}

		@Override
		public int getCount() {
			return launchApplications.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getApplicationContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.launch_item, null);
				holder = new ViewHolder();
				holder.tv = (TextView) v.findViewById(R.id.launch_item_name);
				holder.iv = (ImageView) v.findViewById(R.id.launch_item_icon);
				v.setTag(holder);
			} else
			{
				holder = (ViewHolder) v.getTag();
			}

			TextView tv = holder.tv;
			ImageView iv = holder.iv;

			String item = launchApplications.get(position);

			if (item != null) {
				String[] itemp = item.split("\\%");
				// last param is the app name
				tv.setText(itemp[2]);
				// grab the icon for this app
				iv.setImageDrawable(launchIcons.get(item));
			}
			return v;
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MainActivity.setFullScreen(this);
		setContentView(R.layout.launch_application);
		// Create application icons map
		launchIcons = createApplicationLaunchIconsList(getPackageManager());
		launchApplications = createApplicationLaunchList(getPackageManager());


		((ImageButton) findViewById(R.id.launch_back_btn))
				.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						finish();
					}
				});


		// set the title with the number of applications
		((TextView) findViewById(R.id.launch_title)).setText( "Launch Application ("
			+ launchApplications.size() + ")");

		adapter = new ApplicationLauncherAdapter(this, R.layout.launch_item, launchApplications);
		launchGridView = (GridView) findViewById(R.id.app_grid);
		launchGridView.setNumColumns(2);
		launchGridView.setAdapter(adapter);
		registerForContextMenu(launchGridView);

		launchGridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				String item = launchApplications.get(position);
				Intent launchIntent = createIntentByString(item);
				if (launchIntent == null)
					Toast.makeText(
							LaunchApplication.this,
							getResources().getString(
									R.string.launch_application_activity)
									+ " \""
									+ item
									+ "\" "
									+ getResources().getString(
											R.string.launch_application_not_found),
							Toast.LENGTH_LONG).show();
				else {
					try {
						launchIntent.setAction(Intent.ACTION_MAIN);
						launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
						startActivity(launchIntent);
					} 
					catch (ActivityNotFoundException e) {
						Toast.makeText(
								LaunchApplication.this,
								getResources().getString(
										R.string.launch_application_activity)
										+ " \""
										+ item
										+ "\" "
										+ getResources().getString(
												R.string.launch_application_not_found),
								Toast.LENGTH_LONG).show();
					}
				}
			}
		});
	} // on create

	/**
	 * Creates a list of application icons that exist on the system
	 * @param pm
	 * @return
	 */
	private HashMap<String, Drawable> createApplicationLaunchIconsList(PackageManager pm) {
		Drawable d = null;
		HashMap<String, Drawable> rc = new HashMap<String, Drawable>();
		Intent componentSearchIntent = new Intent();
		componentSearchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		componentSearchIntent.setAction(Intent.ACTION_MAIN);
		List<ResolveInfo> ril = pm.queryIntentActivities(componentSearchIntent,	0);
		String packageName = "";
		String activityName = "";
		String name = "";
		for (ResolveInfo ri : ril) {
			if (ri.activityInfo != null) {
				packageName = ri.activityInfo.packageName;
				activityName = ri.activityInfo.name;
				try {
					if (ri.activityInfo.labelRes != 0) {
						name = (String) ri.activityInfo.loadLabel(pm);
					} else {
						name = (String) ri.loadLabel(pm);
					}
					if (ri.activityInfo.icon != 0) {
						d = ri.activityInfo.loadIcon(pm);
					} else {
						d = ri.loadIcon(pm);
					}
				} catch (Exception e) {
				}
				if (d != null) {
					rc.put(packageName + "%" + activityName + "%" + name, d);
				}
			}
		}
		return rc;
	}

	/**
	 * creates an activity launch intent using a % delimited string
	 * @param paramString
	 * @return
	 */
	static public Intent createIntentByString(String paramString) {
		String[] launchParams = paramString.split("\\%");
		Intent i = new Intent();
		i.setComponent(new ComponentName(launchParams[0], launchParams[1]));
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		return i;
	}

	/**
	 * Class used as an adapter for the gridview
	 * @author p97z
	 * 
	 */
	class ApplicationLauncherAdapter extends ArrayAdapter<String> {
		ApplicationLauncherAdapter(Context context, int resource, List<String> data) {
			super(context, resource, data);
		}

		@Override
		public int getCount() {
			return launchApplications.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getApplicationContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.launch_item, null);
				holder = new ViewHolder();
				holder.tv = (TextView) v.findViewById(R.id.launch_item_name);
				holder.iv = (ImageView) v.findViewById(R.id.launch_item_icon);
				v.setTag(holder);
			} else
				holder = (ViewHolder) v.getTag();

			TextView tv = holder.tv;
			ImageView iv = holder.iv;

			String item = launchApplications.get(position);

			if (item != null) {
				String[] itemp = item.split("\\%");
				tv.setText(itemp[2]);
				iv.setImageDrawable(launchIcons.get(item));
			}
			return v;
		}
	}

	/**
	 * Used to sort all the applications in the grid
	 * @author p97z
	 *
	 */
	private static class AppComparator implements java.util.Comparator<String> {
		public int compare(String a, String b) {
			if (a == null && b == null) {
				return 0;
			}
			if (a == null && b != null) {
				return 1;
			}
			if (a != null && b == null) {
				return -1;
			}
			String[] ap = a.split("\\%");
			String[] bp = b.split("\\%");
			return ap[2].compareToIgnoreCase(bp[2]);
		}
	}

	/**
	 * Creates a list of all the applications on the system
	 * @param pm
	 * @return
	 */
	private List<String> createApplicationLaunchList(PackageManager pm) {
		List<String> rc = new ArrayList<String>();
		Intent componentSearchIntent = new Intent();
		componentSearchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		componentSearchIntent.setAction(Intent.ACTION_MAIN);
		
		// query the package manager for all the application on the system
		List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(componentSearchIntent,	0);
		String name = "";
		for (ResolveInfo rInfo : resolveInfoList) {
			if (rInfo.activityInfo != null) {

				try {
					if (rInfo.activityInfo.labelRes != 0) {
						name = (String) rInfo.activityInfo.loadLabel(pm);
					} else {
						name = (String) rInfo.loadLabel(pm);
					}
				} catch (Exception e) {
				}
				// build it with packagename%activity%name.. this will give us 
				// something to do a split on when sorting
				rc.add(rInfo.activityInfo.packageName + "%" + rInfo.activityInfo.name + "%" + name);
			}
		}
		Collections.sort(rc, new AppComparator());
		return rc;
	}
}