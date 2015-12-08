package com.krp.android.recyclerwithviewpager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class SharedPrefs {

	/*
     * Authentication preference name.
     */
	public static String PREFS_AUTH = "authentication_details";

	public static String KEY_USER_ID = "userid";
	public static String KEY_APP = "app_key";

	public static String getString(Context ctx, String prefName, String key, String defaultValue) {

		SharedPreferences prefs = ctx.getSharedPreferences(prefName,
				Context.MODE_PRIVATE);

		String data = null;

		if (prefs != null) {

			data = prefs.getString(key, defaultValue);
		}

		return data;
	}

	public static void setString(Context ctx, String prefName, String key,
								 String value) {

		SharedPreferences prefs = ctx.getSharedPreferences(prefName,
				Context.MODE_PRIVATE);

		Editor edit = prefs.edit();

		if (prefs.contains(key))
			edit.remove(key);

		edit.putString(key, value);

		edit.commit();
	}

	public static boolean getBoolean(Context ctx, String prefName, String key, boolean defaultValue) {

		SharedPreferences prefs = ctx.getSharedPreferences(prefName,
				Context.MODE_PRIVATE);

		boolean data = false;

		if (prefs != null) {

			data = prefs.getBoolean(key, defaultValue);
		}

		return data;
	}

	public static void setBoolean(Context ctx, String prefName, String key,
								  boolean value) {

		SharedPreferences prefs = ctx.getSharedPreferences(prefName,
				Context.MODE_PRIVATE);

		Editor edit = prefs.edit();

		if (prefs.contains(key))
			edit.remove(key);

		edit.putBoolean(key, value);

		edit.commit();
	}

	public static int getInt(Context ctx, String prefName, String key, int defaultValue) {

		SharedPreferences prefs = ctx.getSharedPreferences(prefName,
				Context.MODE_PRIVATE);

		int data = 0;

		if (prefs != null) {

			data = prefs.getInt(key, defaultValue);
		}

		return data;
	}

	public static void setInt(Context ctx, String prefName, String key,int value) {

		SharedPreferences prefs = ctx.getSharedPreferences(prefName,
				Context.MODE_PRIVATE);

		Editor edit = prefs.edit();

		if (prefs.contains(key))
			edit.remove(key);

		edit.putInt(key, value);

		edit.commit();
	}

	public static void modifyKeyInCache(Context ctx,String key,boolean value)
	{
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(ctx);

		SharedPreferences.Editor editor = prefs.edit();

		if (prefs.contains(key))
			editor.remove(key);

		editor.putBoolean(key, value);

		editor.commit();
	}
	public static SharedPreferences giveDefaultSharedPreferences(Context ctx)
	{
		return PreferenceManager
				.getDefaultSharedPreferences(ctx);
	}

	public static void clearSharedPreferences(Context ctx,String prefName)
	{
		SharedPreferences prefs  = ctx.getSharedPreferences(prefName,
				Context.MODE_PRIVATE);
		Editor e = prefs.edit();
		e.clear();
		e.commit();
	}
}