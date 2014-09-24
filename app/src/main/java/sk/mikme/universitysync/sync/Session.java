package sk.mikme.universitysync.sync;

import android.content.Context;
import android.content.SharedPreferences;

import sk.mikme.universitysync.provider.User;

/**
 * Created by fic on 24.9.2014.
 */
public class Session {
    private static final String SHARED_PREFS = "LoginPreferences";
    private static final String AUTHTOKEN = "auth_token";

    private int mUserId;
    private String mUserEmail;
    private String mAuthToken;

    public Session(int userId, String email, String authToken) {
        mUserId = userId;
        mUserEmail = email;
        mAuthToken = authToken;
    }

    public static Session load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS, 0);
        int id = prefs.getInt(User.COLUMN_NAME_USER_ID, -1);
        String email = prefs.getString(User.COLUMN_NAME_EMAIL, "");
        String authToken = prefs.getString(AUTHTOKEN, "");
        return (id == -1 || email.equals("") || authToken.equals(""))
                ? null
                : new Session(id, email, authToken);
    }

    public void save(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SHARED_PREFS, 0).edit();
        editor.putInt(User.COLUMN_NAME_USER_ID, mUserId);
        editor.putString(User.COLUMN_NAME_EMAIL, mUserEmail);
        editor.putString(AUTHTOKEN, mAuthToken);
        editor.commit();
    };

    public int getUserId() {
        return mUserId;
    }

    public void setUserId(int mUserId) {
        this.mUserId = mUserId;
    }

    public String getUserEmail() {
        return mUserEmail;
    }

    public void setUserEmail(String mUserEmail) {
        this.mUserEmail = mUserEmail;
    }

    public String getAuthToken() {
        return mAuthToken;
    }

    public void setAuthToken(String mAuthToken) {
        this.mAuthToken = mAuthToken;
    }
}
