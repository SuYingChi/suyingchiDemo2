package com.ihs.inputmethod.api;

import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.ihs.app.framework.activity.HSActivity;
import com.ihs.inputmethod.uimodules.R;

/**
 * Created by wenbinduan on 2016/11/7.
 */

public class HSDeepLinkActivity extends HSActivity {

	private GoogleApiClient mClient;
	private Uri mUrl;
	private String mTitle;
	private String mDescription;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mClient = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
		mUrl = Uri.parse(getString(R.string.deeplink_site_full));
		mTitle =getString(R.string.deeplink_key_word);
		mDescription = getString(R.string.deeplink_description);
	}

	public Action getAction() {
		Thing object = new Thing.Builder()
				.setName(mTitle)
				.setDescription(mDescription)
				.setUrl(mUrl)
				.build();

		return new Action.Builder(Action.TYPE_VIEW)
				.setObject(object)
				.setActionStatus(Action.STATUS_TYPE_COMPLETED)
				.build();
	}

	@Override
	public void onStart() {
		super.onStart();
		mClient.connect();
		AppIndex.AppIndexApi.start(mClient, getAction());
	}

	@Override
	protected void onStop() {
		AppIndex.AppIndexApi.end(mClient, getAction());
		mClient.disconnect();
		super.onStop();
	}
}
