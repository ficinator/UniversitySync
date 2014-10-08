package sk.mikme.universitysync.sync;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Base64;
import android.util.SparseArray;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import sk.mikme.universitysync.activities.MainActivity;
import sk.mikme.universitysync.provider.Group;
import sk.mikme.universitysync.provider.Keyword;
import sk.mikme.universitysync.provider.Member;
import sk.mikme.universitysync.provider.Note;
import sk.mikme.universitysync.provider.NoteKeyword;
import sk.mikme.universitysync.provider.Provider;
import sk.mikme.universitysync.provider.User;

/**
 * Created by fic on 18.9.2014.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final long SYNC_FREQUENCY = 60 * 60;  // 1 hour (in seconds)
    private static final String PREF_SETUP_COMPLETE = "setup_complete";
    public static final String AUTHTOKEN = "authToken";

    public static final String SERVER_URL = "http://www.universitysync.sk/android/";
    public static final String AUTH_SCRIPT_PATH = "auth.php";
    public static final String DATA_SCRIPT_PATH = "getData.php";
    public static final int NET_READ_TIMEOUT_MILLIS = 10000;
    public static final int NET_CONNECT_TIMEOUT_MILLIS = 15000;
    public static final String ARG_DATA_TYPE = "data_type";
    private static final String ARGS_LENGTH = "args_length";
    private static final String ARG = "arg";
    public static final String SET_COOKIE = "Set-Cookie";
    public static final String ACTION_FINISHED_SYNC = "sk.mikme.universitysync.ACTION_FINISHED_SYNC";
    public static IntentFilter SYNC_INTENT_FILTER = new IntentFilter(ACTION_FINISHED_SYNC);
    private static boolean mIsSynced;
    /**
     * Content resolver, for performing database operations.
     */
    private final ContentResolver mContentResolver;

    private static Session mSession;
    private static Account mAccount;

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
        mIsSynced = false;
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
    }

    public static void setSession(Session session) {
        mSession = session;
    }

    public static Session getSession() {
        return mSession;
    }

    @Override
    public void onPerformSync(Account account,
                              Bundle bundle,
                              String s,
                              ContentProviderClient contentProviderClient,
                              SyncResult syncResult) {

        //System.out.print("Sync");

        List<Argument> args = new ArrayList<Argument>();
        for (int i = 0; i < bundle.getInt(ARGS_LENGTH); i++) {
            Argument arg = new Argument(bundle.getString(ARG + i));
            args.add(arg);

            //System.out.print(" " + arg.toString());
        }

        //System.out.println();

        try {
            URL location = getLocation(args);
            JSONObject data = downloadUrl(location);
            if (data != null) {
                updateLocalData(data, args, syncResult);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Account getAccount() {
        return mAccount;
    }

    public static void setAccount(Account account) {
        mAccount = account;
    }

    private void updateLocalData(JSONObject jsonObject, List<Argument> args, SyncResult syncResult)
            throws JSONException, RemoteException, OperationApplicationException {

        ContentResolver contentResolver = getContext().getContentResolver();
        Intent intent = new Intent(ACTION_FINISHED_SYNC);

        // update users
        if (!jsonObject.isNull(User.PATH)) {
            Uri uri = User.URI;
            for (Argument arg : args) {
                if (arg.getName().equals(User.COLUMN_NAME_USER_ID)) {
                    uri = uri.buildUpon().appendPath(arg.getValue()).build();
                }
            }
            // parse users from JSON object
            HashMap<String, User> users = DataParser.parseUsers(jsonObject);
            // get notes prom database
            Cursor c = contentResolver.query(
                    uri,
                    User.PROJECTION,
                    null, null, null);
            // find new items
            updateUsers(users, c, syncResult);
            intent.putExtra(ARG_DATA_TYPE, User.TABLE_NAME);
        }
        // update notes
        else if (!jsonObject.isNull(Note.PATH)) {
            Uri notesUri = Note.URI;
            for (Argument arg : args) {
                if (arg.getName().equals(User.COLUMN_NAME_USER_ID)) {
                    notesUri = notesUri.buildUpon()
                            .appendPath(User.PATH)
                            .appendPath(arg.getValue()).build();
                }
                else if (arg.getName().equals(Group.COLUMN_NAME_GROUP_ID)) {
                    notesUri = notesUri.buildUpon()
                            .appendPath(Group.PATH)
                            .appendPath(arg.getValue()).build();
                }
            }
            // parse notes from JSON object
            HashMap<String, Note> notes = DataParser.parseNotes(jsonObject);
            // get note details
            for (Note note : notes.values()) {
                JSONObject details = null;
                try {
                    details = downloadUrl(new URL(SERVER_URL + note.getPath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (details != null)
                    note.setDetails(details);
            }
            // get keywords from database
            Cursor cKeywords = mContentResolver.query(Keyword.URI, Keyword.PROJECTION, null, null, null);
            // get notes from database
            Cursor cNotes = contentResolver.query(notesUri, Note.PROJECTION, null, null, null);
            // find new items
            syncResult = updateKeywords(notes, cKeywords, syncResult);
            syncResult = updateNotes(notes, cNotes, syncResult);

            syncResult = updateNoteKeywords(notes, syncResult);

            intent.putExtra(ARG_DATA_TYPE, Note.TABLE_NAME);
        }
        else if (!jsonObject.isNull(Group.PATH)) {
            Uri uriGroup = Group.URI;
            Uri uriMember = Member.URI;
            for (Argument arg : args) {
                if (arg.getName().equals(User.COLUMN_NAME_USER_ID)) {
                    uriMember = uriMember.buildUpon()
                            .appendPath(User.PATH)
                            .appendPath(arg.getValue()).build();
                    uriGroup = uriGroup.buildUpon()
                            .appendPath(User.PATH)
                            .appendPath(arg.getValue()).build();
                }
            }
            // parse groups from JSON object
            HashMap<String, Group> groups = DataParser.parseGroups(jsonObject);
            HashMap<String, Member> members = DataParser.parseMembers(jsonObject);
            // get groups prom database
            Cursor cMember = contentResolver.query(
                    uriMember,
                    Member.PROJECTION,
                    null, null, null);
            Cursor cGroup = contentResolver.query(
                    uriGroup,
                    Group.PROJECTION,
                    null, null, null);
            // find new items
            syncResult = updateMembers(members, cMember, syncResult);
            syncResult = updateGroups(groups, cGroup, syncResult);
            intent.putExtra(ARG_DATA_TYPE, Group.TABLE_NAME);
        }

        // inform activity about finished sync
        getContext().sendBroadcast(intent);
    }

    private SyncResult updateUsers(HashMap<String, User> users, Cursor c, SyncResult syncResult)
            throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        while (c.moveToNext()) {
            int id = c.getInt(User.COLUMN_ID);
            User user = new User(c);
            User match = users.get(Integer.toString(user.getUserId()));
            if (match != null) {
                users.remove(Integer.toString(user.getUserId()));
                Uri existingUri = User.URI.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                // if remote version is newer than local one
                if (!match.equals(user)) {
                    // update existing record
                    batch.add(ContentProviderOperation.newUpdate(existingUri)
                            .withValue(User.COLUMN_NAME_NAME, match.getName())
                            .withValue(User.COLUMN_NAME_SURNAME, match.getSurname())
                            .withValue(User.COLUMN_NAME_EMAIL, match.getEmail())
                            .withValue(User.COLUMN_NAME_UNIVERSITY, match.getUniversity())
                            .withValue(User.COLUMN_NAME_INFO, match.getInfo())
                            .withValue(User.COLUMN_NAME_RANK, match.getRank())
                            .build());
                    syncResult.stats.numUpdates++;
                }
            }
            syncResult.stats.numEntries++;
        }
        c.close();

        // Add new notes
        for (User user : users.values()) {
            batch.add(ContentProviderOperation.newInsert(User.URI)
                    .withValue(User.COLUMN_NAME_USER_ID, user.getUserId())
                    .withValue(User.COLUMN_NAME_NAME, user.getName())
                    .withValue(User.COLUMN_NAME_SURNAME, user.getSurname())
                    .withValue(User.COLUMN_NAME_EMAIL, user.getEmail())
                    .withValue(User.COLUMN_NAME_UNIVERSITY, user.getUniversity())
                    .withValue(User.COLUMN_NAME_INFO, user.getInfo())
                    .withValue(User.COLUMN_NAME_RANK, user.getRank())
                    .build());
            syncResult.stats.numInserts++;
        }
        mContentResolver.applyBatch(Provider.AUTHORITY, batch);
        mContentResolver.notifyChange(
                User.URI,
                null,
                false);                         // IMPORTANT: Do not sync to network
        // This sample doesn't support uploads, but if *your* code does, make sure you set
        // syncToNetwork=false in the line above to prevent duplicate syncs.
        return syncResult;
    }

    private SyncResult updateNotes(HashMap<String, Note> notes, Cursor c, SyncResult syncResult)
            throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        SparseArray<Note> oldNotes = new SparseArray<Note>();
        while (c.moveToNext()) {
            int id = c.getInt(Note.COLUMN_ID);
            Note note = new Note(c);
            syncResult.stats.numEntries++;
            Note match = notes.get(Integer.toString(note.getNoteId()));
            if (match != null) {
                oldNotes.put(note.getNoteId(), note);
                Uri existingUri = Note.URI.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                // if remote version is newer than local one
                if (match.getDate() > note.getDate()) {
                    // update existing record
                    batch.add(ContentProviderOperation.newUpdate(existingUri)
                            .withValue(Note.COLUMN_NAME_NOTE_ID, match.getNoteId())
                            .withValue(Note.COLUMN_NAME_USER_ID, match.getUserId())
                            .withValue(Note.COLUMN_NAME_GROUP_ID, match.getGroupId())
                            .withValue(Note.COLUMN_NAME_TITLE, match.getTitle())
                            .withValue(Note.COLUMN_NAME_LIKES, match.getLikes())
                            .withValue(Note.COLUMN_NAME_DATE, match.getDate())
                            .withValue(Note.COLUMN_NAME_CONTENT, match.getContent())
                            .build());
                    syncResult.stats.numUpdates++;
                }
            }
        }
        c.close();

        // Add new notes
        for (Note note : notes.values()) {
            if (oldNotes.get(note.getNoteId()) == null) {
                batch.add(ContentProviderOperation.newInsert(Note.URI)
                        .withValue(Note.COLUMN_NAME_NOTE_ID, note.getNoteId())
                        .withValue(Note.COLUMN_NAME_USER_ID, note.getUserId())
                        .withValue(Note.COLUMN_NAME_GROUP_ID, note.getGroupId())
                        .withValue(Note.COLUMN_NAME_TITLE, note.getTitle())
                        .withValue(Note.COLUMN_NAME_LIKES, note.getLikes())
                        .withValue(Note.COLUMN_NAME_DATE, note.getDate())
                        .withValue(Note.COLUMN_NAME_CONTENT, note.getContent())
                        .build());
                syncResult.stats.numInserts++;
            }
        }
        mContentResolver.applyBatch(Provider.AUTHORITY, batch);
        mContentResolver.notifyChange(Note.URI, null, false);
        return syncResult;
    }

    private SyncResult updateKeywords(HashMap<String, Note> notes, Cursor c, SyncResult syncResult)
        throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        HashMap<String, String> newKeywords = new HashMap<String, String>();

        for (Note note : notes.values()) {
            for (String keyword : note.getKeywords()) {
                boolean newKeyword = true;
                c.moveToPosition(-1);
                while (c.moveToNext() && newKeyword) {
                    Keyword oldKeyword = new Keyword(c);
                    if (oldKeyword.getName().equals(keyword))
                        newKeyword = false;
                }
                if (newKeyword) {
                    if (!newKeywords.containsKey(keyword))
                        newKeywords.put(keyword, keyword);
                }
            }
        }
        c.close();

        for (String keyword : newKeywords.values()) {
            batch.add(ContentProviderOperation.newInsert(Keyword.URI)
                    .withValue(Keyword.COLUMN_NAME_NAME, keyword).build());
        }

        mContentResolver.applyBatch(Provider.AUTHORITY, batch);
        mContentResolver.notifyChange(Keyword.URI, null, false);

        return syncResult;
    }

    private SyncResult updateNoteKeywords(HashMap<String, Note> notes, SyncResult syncResult)
            throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        for (Note note : notes.values()) {
            Cursor cNoteKeywords = mContentResolver.query(
                    NoteKeyword.URI.buildUpon()
                            .appendPath(Note.PATH)
                            .appendPath(Integer.toString(note.getNoteId()))
                            .build(),
                    NoteKeyword.PROJECTION, null, null, null);
            HashMap<String, String> oldKeywords = new HashMap<String, String>();
            while (cNoteKeywords.moveToNext()) {
                NoteKeyword noteKeyword = new NoteKeyword(cNoteKeywords);
                boolean hasKeyword = false;
                for (String keyword : note.getKeywords()) {
                    if (keyword.equals(noteKeyword.getKeyword().getName())) {
                        oldKeywords.put(keyword, keyword);
                        hasKeyword = true;
                    }
                }
                if (!hasKeyword) {
                    // delete this record from db
                }
            }
            cNoteKeywords.close();

            for (String keyword : note.getKeywords()) {
                if (!oldKeywords.containsKey(keyword)) {
                    Cursor cKeyword = mContentResolver.query(
                            Keyword.URI.buildUpon().appendPath(keyword).build(),
                            Keyword.PROJECTION, null, null, null);
                    if (cKeyword.moveToFirst()) {
                        Keyword kwMatch = new Keyword(cKeyword);
                        batch.add(ContentProviderOperation.newInsert(NoteKeyword.URI)
                                .withValue(NoteKeyword.COLUMN_NAME_NOTE_ID, note.getNoteId())
                                .withValue(NoteKeyword.COLUMN_NAME_KEYWORD_ID, kwMatch.getId())
                                .build());
                    }
                    cKeyword.close();
                }
            }
        }

        mContentResolver.applyBatch(Provider.AUTHORITY, batch);
        mContentResolver.notifyChange(NoteKeyword.URI, null, false);

        return syncResult;
    }

    private SyncResult updateMembers(HashMap<String, Member> members, Cursor c, SyncResult syncResult)
            throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        while (c.moveToNext()) {
            Member member = new Member(c);
            String id = Integer.toString(member.getUserId()) + "," + Integer.toString(member.getGroupId());
            syncResult.stats.numEntries++;
            Member match = members.get(id);
            if (match != null) {
                members.remove(id);
                Uri existingUri = Member.URI.buildUpon()
                        .appendPath(Integer.toString(member.getId())).build();
                // if remote version is newer than local one
                if (!match.equals(member)) {
                    // update existing record
                    batch.add(ContentProviderOperation.newUpdate(existingUri)
                            .withValue(Member.COLUMN_NAME_USER_ID, match.getUserId())
                            .withValue(Member.COLUMN_NAME_GROUP_ID, match.getGroupId())
                            .withValue(Member.COLUMN_NAME_ADMIN, match.isAdmin() ? 1 : 0)
                            .build());
                    syncResult.stats.numUpdates++;
                }
            }
        }
        c.close();

        // Add new members
        for (Member member : members.values()) {
            batch.add(ContentProviderOperation.newInsert(Member.URI)
                    .withValue(Member.COLUMN_NAME_USER_ID, member.getUserId())
                    .withValue(Member.COLUMN_NAME_GROUP_ID, member.getGroupId())
                    .withValue(Member.COLUMN_NAME_ADMIN, member.isAdmin() ? 1 : 0)
                    .build());
            syncResult.stats.numInserts++;
        }
        mContentResolver.applyBatch(Provider.AUTHORITY, batch);
        mContentResolver.notifyChange(
                Member.URI,
                null,
                false);                         // IMPORTANT: Do not sync to network
        // This sample doesn't support uploads, but if *your* code does, make sure you set
        // syncToNetwork=false in the line above to prevent duplicate syncs.
        return syncResult;
    }

    private SyncResult updateGroups(HashMap<String, Group> groups, Cursor c, SyncResult syncResult)
            throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        while (c.moveToNext()) {
            int id = c.getInt(Group.COLUMN_ID);
            Group group = new Group(c);
            syncResult.stats.numEntries++;
            Group match = groups.get(Integer.toString(group.getGroupId()));
            if (match != null) {
                groups.remove(Integer.toString(group.getGroupId()));
                Uri existingUri = Group.URI.buildUpon()
                        .appendPath(Integer.toString(id)).build();
                // if remote version is newer than local one
                if (!match.equals(group)) {
                    // update existing record
                    batch.add(ContentProviderOperation.newUpdate(existingUri)
                            .withValue(Group.COLUMN_NAME_GROUP_ID, match.getGroupId())
                            .withValue(Group.COLUMN_NAME_NAME, match.getName())
                            .withValue(Group.COLUMN_NAME_UNIVERSITY, match.getUniversity())
                            .withValue(Group.COLUMN_NAME_INFO, match.getInfo())
                            .withValue(Group.COLUMN_NAME_PUBLIC, match.isPublic() ? 1 : 0)
                            .withValue(Group.COLUMN_NAME_MEMBER_INFO, match.getMemberInfo())
                            .build());
                    syncResult.stats.numUpdates++;
                }
            }
        }
        c.close();

        // Add new notes
        for (Group group : groups.values()) {
            batch.add(ContentProviderOperation.newInsert(Group.URI)
                    .withValue(Group.COLUMN_NAME_GROUP_ID, group.getGroupId())
                    .withValue(Group.COLUMN_NAME_NAME, group.getName())
                    .withValue(Group.COLUMN_NAME_UNIVERSITY, group.getUniversity())
                    .withValue(Group.COLUMN_NAME_INFO, group.getInfo())
                    .withValue(Group.COLUMN_NAME_PUBLIC, group.isPublic() ? 1 : 0)
                    .withValue(Group.COLUMN_NAME_MEMBER_INFO, group.getMemberInfo())
                    .build());
            syncResult.stats.numInserts++;
        }
        mContentResolver.applyBatch(Provider.AUTHORITY, batch);
        mContentResolver.notifyChange(
                Group.URI,
                null,
                false);                         // IMPORTANT: Do not sync to network
        // This sample doesn't support uploads, but if *your* code does, make sure you set
        // syncToNetwork=false in the line above to prevent duplicate syncs.
        return syncResult;
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
    public static void triggerRefresh(ArrayList<Argument> args) {
        Bundle b = new Bundle();
        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        b.putInt(ARGS_LENGTH, args.size());
        for (int i = 0; i < args.size(); i++)
            b.putString(ARG + i, args.get(i).toString());
        ContentResolver.requestSync(mAccount, Provider.AUTHORITY, b);
    }

    public URL getLocation(List<Argument> args)
            throws MalformedURLException, UnsupportedEncodingException {
        String urlString = SyncAdapter.SERVER_URL + SyncAdapter.DATA_SCRIPT_PATH + "?";
        for (Argument arg : args) {
            urlString += arg.getName() + "=" + URLEncoder.encode(arg.getValue(), "UTF-8") + "&";
        }
        return  new URL(urlString);
    }

    public JSONObject downloadUrl(URL url) throws IOException {
        HttpURLConnection conn = null;
        InputStream stream = null;
        JSONObject jsonObject= null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(SyncAdapter.NET_READ_TIMEOUT_MILLIS);
            conn.setConnectTimeout(SyncAdapter.NET_CONNECT_TIMEOUT_MILLIS);
            conn.setRequestMethod("GET");
            conn.setRequestProperty(SET_COOKIE,
                    "PHPSESSID=" + mSession.getAuthToken() + "; path=/");
            conn.setDoInput(true);
            stream = conn.getInputStream();
            String resultString = streamToString(stream);
            jsonObject = new JSONObject(resultString);
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (stream != null)
                stream.close();
            if (conn != null)
                conn.disconnect();
        }
        return jsonObject;
    }

    public static String streamToString(InputStream inputStream) throws IOException {
        String line;
        StringBuilder str = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
        while ((line = rd.readLine()) != null) {
            str.append(line);
        }
        return str.toString();
    }

    public static void syncCurrentUserData() {
        if (!mIsSynced) {
            syncCurrentUser();
            syncCurrentUserGroups();
            syncCurrentUserNotes();
            mIsSynced = true;
        }
    }

    public static void syncCurrentUser() {
        ArrayList<Argument> args = new ArrayList<Argument>();
        args.add(new Argument(User.COLUMN_NAME_USER_ID, Integer.toString(mSession.getUserId())));
        args.add(new Argument(ARG_DATA_TYPE, User.TABLE_NAME));
        triggerRefresh(args);
    }

    public static void syncCurrentUserGroups() {
        ArrayList<Argument> args = new ArrayList<Argument>();
        args.add(new Argument(User.COLUMN_NAME_USER_ID, Integer.toString(mSession.getUserId())));
        args.add(new Argument(ARG_DATA_TYPE, Group.TABLE_NAME));
        triggerRefresh(args);
    }

    public static void syncCurrentUserNotes() {
        ArrayList<Argument> args = new ArrayList<Argument>();
        args.add(new Argument(User.COLUMN_NAME_USER_ID, Integer.toString(mSession.getUserId())));
        args.add(new Argument(ARG_DATA_TYPE, Note.TABLE_NAME));
        triggerRefresh(args);
    }
}
