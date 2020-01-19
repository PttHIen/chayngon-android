package com.vantinviet.foodies.android.Utils;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Helper class to apply custom font from assets to all text views in the specified root
 * view.
 * 
 * @author Alexander Naberezhnov
 */
public class FontHelper {
	// -----------------------------------------------------------------------
	//
	// Constants
	//
	// -----------------------------------------------------------------------
	private static final String TAG = FontHelper.class.getSimpleName();

	// -----------------------------------------------------------------------
	//
	// Properties
	//
	// -----------------------------------------------------------------------
	/**
	 * Apply specified font for all text views (including nested ones) in the specified
	 * root view.
	 * 
	 * @param context
	 *            Context to fetch font asset.
	 * @param root
	 *            Root view that should have specified font for all it's nested text
	 *            views.
	 * @param fontPath
	 *            Font path related to the assets folder (e.g. "fonts/YourFontName.ttf").
	 */
	public static void applyFont(final Context context, final View root, final String fontPath) {
		try {
			if (root instanceof ViewGroup) {
				ViewGroup viewGroup = (ViewGroup) root;
				int childCount = viewGroup.getChildCount();
				for (int i = 0; i < childCount; i++)
					applyFont(context, viewGroup.getChildAt(i), fontPath);
			} else if (root instanceof TextView)
				((TextView) root).setTypeface(Typeface.createFromAsset(context.getAssets(), fontPath));
		} catch (Exception e) {
			Log.e(TAG, String.format("Error occured when trying to apply %s font for %s view", fontPath, root));
			e.printStackTrace();
		}
	}
	public static String md5(String s)
	{
		MessageDigest digest;
		try
		{
			digest = MessageDigest.getInstance("MD5");
			digest.update(s.getBytes(Charset.forName("US-ASCII")),0,s.length());
			byte[] magnitude = digest.digest();
			BigInteger bi = new BigInteger(1, magnitude);
			String hash = String.format("%0" + (magnitude.length << 1) + "x", bi);
			return hash;
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		return "";
	}
	static public String getTokenFirebase(int chars) {
		String CharSet = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNOPQRSTUVWXYZ234567890";
		String Token = "";
		for (int a = 1; a <= chars; a++) {
			Token += CharSet.charAt(new Random().nextInt(CharSet.length()));
		}
		return Token;
	}
	static public String getToken(int chars) {
		String CharSet = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNOPQRSTUVWXYZ234567890!@#$";
		String Token = "";
		for (int a = 1; a <= chars; a++) {
			Token += CharSet.charAt(new Random().nextInt(CharSet.length()));
		}
		return Token;
	}
}
