package sk.mikme.universitysync.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProvider;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sk.mikme.universitysync.provider.Note;
import sk.mikme.universitysync.provider.Provider;

/**
 * Created by fic on 18.9.2014.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final long SYNC_FREQUENCY = 60 * 60;  // 1 hour (in seconds)
    private static final String PREF_SETUP_COMPLETE = "setup_complete";
    public static final String PREF_DATA_TYPE = "data_type";

    public static final String SERVER_URL = "http://www.universitysync.sk/android/";
    public static final String AUTH_SCRIPT_PATH = "getAuthToken.php";
    public static final String DATA_SCRIPT_PATH = "getData.php";
    public static final int NET_READ_TIMEOUT_MILLIS = 10000;
    public static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;
    /**
     * Content resolver, for performing database operations.
     */
    private final ContentResolver mContentResolver;

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account,
                              Bundle bundle,
                              String s,
                              ContentProviderClient contentProviderClient,
                              SyncResult syncResult) {

        String dataType = bundle.getString(PREF_DATA_TYPE);

        try {
            URL location = getLocation(dataType);
            JSONObject data = downloadUrl(location);
            updateLocalData(data, syncResult);
            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        }
    }

    private void updateLocalData(JSONObject jsonObject, SyncResult syncResult)
            throws IOException, JSONException, RemoteException, OperationApplicationException {

        ContentResolver contentResolver = getContext().getContentResolver();

        // update notes
        if (!jsonObject.isNull(Note.PATH)) {
            // parse notes from JSON object
            HashMap<String, Note> notes = DataParser.parseNotes(jsonObject);
            // get notes prom database
            Cursor c = contentResolver.query(
                    Note.URI,
                    Note.PROJECTION,
                    null, null, null);
            // find new items
            updateNotes(notes, c, syncResult);
        }
    }

    private SyncResult updateNotes(HashMap<String, Note> notes, Cursor c, SyncResult syncResult) throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        int id;
        String noteId;
        //int userId = c.getInt(ProviderConstants.Note.COLUMN_USER_ID);
        //int groupId = c.getInt(ProviderConstants.Note.COLUMN_GROUP_ID);
        //int likes = c.getInt(ProviderConstants.Note.COLUMN_LIKES);
        long date;
        while (c.moveToNext()) {
            id = c.getInt(Note.COLUMN_ID);
            noteId = Integer.toString(c.getInt(Note.COLUMN_NOTE_ID));
            date = c.getLong(Note.COLUMN_DATE);
            syncResult.stats.numEntries++;
            Note match = notes.get(noteId);
            if (match != null) {
                notes.remove(noteId);
                Uri existingUri = Note.URI.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                // if remote version is newer than local one
                if (match.getDate() > date) {
                    // update existing record
                    batch.add(ContentProviderOperation.newUpdate(existingUri)
                            .withValue(Note.COLUMN_NAME_NOTE_ID, match.getNoteId())
                            .withValue(Note.COLUMN_NAME_USER_ID, match.getUserId())
                            .withValue(Note.COLUMN_NAME_GROUP_ID, match.getGroupId())
                            .withValue(Note.COLUMN_NAME_LIKES, match.getLikes())
                            .withValue(Note.COLUMN_NAME_DATE, match.getDate())
                            .build());
                    syncResult.stats.numUpdates++;
                }
            }
        }
        c.close();

        // Add new notes
        for (Note note : notes.values()) {
            batch.add(ContentProviderOperation.newInsert(Note.URI)
                    .withValue(Note.COLUMN_NAME_NOTE_ID, note.getNoteId())
                    .withValue(Note.COLUMN_NAME_USER_ID, note.getUserId())
                    .withValue(Note.COLUMN_NAME_GROUP_ID, note.getGroupId())
                    .withValue(Note.COLUMN_NAME_LIKES, note.getLikes())
                    .withValue(Note.COLUMN_NAME_DATE, note.getDate())
                    .build());
            syncResult.stats.numInserts++;
        }
        mContentResolver.applyBatch(Provider.AUTHORITY, batch);
        mContentResolver.notifyChange(
                Note.URI,
                null,
                false);                         // IMPORTANT: Do not sync to network
        // This sample doesn't support uploads, but if *your* code does, make sure you set
        // syncToNetwork=false in the line above to prevent duplicate syncs.
        return syncResult;
    }

//    public static JSONObject authenticate(String email, String password) throws IOException {
//        URL url = new URL(SyncAdapter.SERVER_URL + SyncAdapter.AUTH_SCRIPT_PATH);
//        JSONObject jsonObject= null;
//        InputStream stream = null;
//        try {
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            conn.setReadTimeout(SyncAdapter.NET_READ_TIMEOUT_MILLIS /* milliseconds */);
//            conn.setConnectTimeout(SyncAdapter.NET_CONNECT_TIMEOUT_MILLIS /* milliseconds */);
//            conn.setRequestMethod("POST");
//            conn.setDoInput(true);
//            conn.setDoOutput(true);
//            DataOutputStream os = new DataOutputStream (conn.getOutputStream ());
//            os.writeBytes ("email=" + URLEncoder.encode(email, "UTF-8") +
//                    "&password=" + URLEncoder.encode(password, "UTF-8"));
//            os.flush ();
//            os.close ();
//            stream = conn.getInputStream();
//            String resultString = streamToString(stream);
//            jsonObject = new JSONObject(resultString);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        } finally {
//            if (stream != null)
//                stream.close();
//        }
//        return jsonObject;
//    }

    /**
     * Create an entry for this application in the system account list, if it isn't already there.
     *
     * @param context Context
     */
    @TargetApi(Build.VERSION_CODES.FROYO)
    public static void createSyncAccount(Context context) {
        boolean newAccount = false;
        boolean setupComplete = PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean(PREF_SETUP_COMPLETE, false);

        Account account = AccountService.getAccount();
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        if (accountManager.addAccountExplicitly(account, null, null)) {
            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable(account, Provider.AUTHORITY, 1);
            // Inform the system that this account is eligible for auto sync when the network is up
            ContentResolver.setSyncAutomatically(account, Provider.AUTHORITY, true);
            // Recommend a schedule for automatic synchronization. The system may modify this based
            // on other scheduled syncs and network utilization.
            ContentResolver.addPeriodicSync(
                    account, Provider.AUTHORITY, new Bundle(),SYNC_FREQUENCY);
            newAccount = true;
        }

        // Schedule an initial sync if we detect problems with either our account or our local
        // data has been deleted. (Note that it's possible to clear app data WITHOUT affecting
        // the account list, so wee need to check both.)
        if (newAccount || !setupComplete) {
            triggerRefresh(Note.TABLE_NAME);
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(PREF_SETUP_COMPLETE, true).commit();
        }
    }

    /**
     * Helper method to trigger an immediate sync ("refresh").
     *
     * <p>This should only be used when we need to preempt the normal sync schedule. Typically, this
     * means the user has pressed the "refresh" button.
     *
     * Note that SYNC_EXTRAS_MANUAL will cause an immediate sync, without any optimization to
     * preserve battery life. If you know new data is available (perhaps via a GCM notification),
     * but the user is not actively waiting for that data, you should omit this flag; this will give
     * the OS additional freedom in scheduling your sync request.
     */
    public static void triggerRefresh(String dataType) {
        Bundle b = new Bundle();
        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        b.putString(PREF_DATA_TYPE, dataType);
        ContentResolver.requestSync(
                AccountService.getAccount(),
                Provider.AUTHORITY,
                b);
    }

    public URL getLocation(String dataType) throws MalformedURLException {
        return new URL(SyncAdapter.SERVER_URL + SyncAdapter.DATA_SCRIPT_PATH + "?" +
                PREF_DATA_TYPE + "=" + dataType);
    }

    public JSONObject downloadUrl(URL url) throws IOException {
        JSONObject jsonObject= null;
        InputStream stream = null;
        try {
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(SyncAdapter.NET_READ_TIMEOUT_MILLIS /* milliseconds */);
            conn.setConnectTimeout(SyncAdapter.NET_CONNECT_TIMEOUT_MILLIS /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            stream = conn.getInputStream();
            String resultString = streamToString(stream);
            if (resultString.startsWith("<?xml"))
                jsonObject = XML.toJSONObject(resultString);
            else
                jsonObject = new JSONObject(resultString);
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (stream != null)
                stream.close();
        }
        return jsonObject;
    }

    private String streamToString(InputStream inputStream) throws IOException {
        String line;
        StringBuilder str = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
        while ((line = rd.readLine()) != null) {
            str.append(line);
        }
        return str.toString();
    }
}
