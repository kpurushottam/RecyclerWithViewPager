package com.krp.android.recyclerwithviewpager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Network {
    /*
     * Check if network is available.
     */
    public static boolean isNetworkAvailable(Context ctx) {

	ConnectivityManager cm = (ConnectivityManager) ctx
		.getSystemService(Context.CONNECTIVITY_SERVICE);

	if (cm != null) {

	    NetworkInfo netInfo = cm.getActiveNetworkInfo();

	    if (netInfo != null && netInfo.isConnected()) {

		return true;
	    }
	}
	return false;
    }
}
