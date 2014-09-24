package sk.mikme.universitysync.sync;

import android.accounts.Account;
import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import sk.mikme.universitysync.provider.User;

/**
 * Created by fic on 20.9.2014.
 * Represents an asynchronous login/registration task used to authenticate
 * the user.
 */
public class AuthAccountTask extends AsyncTask<Void, Void, User> {

    private AuthAccountListener mListener;
    private String mEmail;
    private String mPassword;

    public AuthAccountTask(AuthAccountListener listener,
                           String email,
                           String password) {
        mListener = listener;
        mEmail = email;
        mPassword = password;
    }

    @Override
    protected void onPreExecute() {
        mListener.showProgressBar(true);
    }

    @Override
    protected User doInBackground(Void... params) {
        HttpURLConnection conn = null;
        DataOutputStream os = null;
        InputStream is = null;
        User user = null;
        try {
            URL url = new URL(SyncAdapter.SERVER_URL + SyncAdapter.AUTH_SCRIPT_PATH);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(SyncAdapter.NET_READ_TIMEOUT_MILLIS);
            conn.setConnectTimeout(SyncAdapter.NET_CONNECT_TIMEOUT_MILLIS);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes("email=" + URLEncoder.encode(mEmail, "UTF-8") +
                    "&password=" + URLEncoder.encode(mPassword, "UTF-8"));
            os.flush();
            is = conn.getInputStream();
            JSONObject jsonObject = new JSONObject(SyncAdapter.streamToString(is));
            if (jsonObject.isNull("user"))
                return null;
            user = new User(jsonObject.getJSONObject("user"));
            String cookie = conn.getHeaderField(SyncAdapter.SET_COOKIE);
            if (cookie != null)
                user.setAuthToken(cookie.split(";")[0].split("=")[1]);
        } catch (MalformedURLException e) {
            //TODO:
            e.printStackTrace();
        } catch (IOException e) {
            //TODO:
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (conn != null)
                conn.disconnect();
            try {
                if (os != null)
                    os.close();
                if (is != null)
                    is.close();
            } catch (IOException e) {
                //TODO:
                e.printStackTrace();
            }
        }
        return user;
    }

    @Override
    protected void onPostExecute(User user) {
        if (user == null)
            mListener.showProgressBar(false);
        mListener.onAccountAuth(user);
    }

    public interface AuthAccountListener extends AsyncTaskListener {
        void onAccountAuth(User user);
    }
}
