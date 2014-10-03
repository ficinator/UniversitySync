package sk.mikme.universitysync.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.BaseColumns;

import java.util.ArrayList;

/**
 * Created by fic on 17.9.2014.
 */
public class Provider extends ContentProvider {
    private Database mDatabaseHelper;

    /**
     * Content provider authority.
     */
    public static final String AUTHORITY = "sk.mikme.universitysync";
    public static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);

    // The constants below represent individual URI routes, as IDs. Every URI pattern recognized by
    // this ContentProvider is defined using sUriMatcher.addURI(), and associated with one of these
    // IDs.
    //
    // When a incoming URI is run through sUriMatcher, it will be tested against the defined
    // URI patterns, and the corresponding route ID will be returned.
    /**
     * URI ID for route: /notes
     */
    public static final int ROUTE_NOTES = 1;
    /**
     * URI ID for route: /notes/{ID}
     */
    public static final int ROUTE_NOTES_ID = 2;
    /**
     * URI ID for route: /notes/users/{ID}
     */
    public static final int ROUTE_NOTES_USERS_ID = 3;
    /**
     * URI ID for route: /notes/groups/{ID}
     */
    public static final int ROUTE_NOTES_GROUPS_ID = 4;
    /**
     * URI ID for route: /groups
     */
    public static final int ROUTE_GROUPS = 5;
    /**
     * URI ID for route: /groups/{ID}
     */
    public static final int ROUTE_GROUPS_ID = 6;
    /**
     * URI ID for route: /groups/users/{ID}
     */
    public static final int ROUTE_GROUPS_USERS_ID = 7;
    /**
     * URI ID for route: /users
     */
    public static final int ROUTE_USERS = 8;
    /**
     * URI ID for route: /users/{ID}
     */
    public static final int ROUTE_USERS_ID = 9;
    /**
     * URI ID for route: /users/{ID}
     */
    public static final int ROUTE_USERS_GROUPS_ID = 10;
    /**
     * URI ID for route: /members
     */
    public static final int ROUTE_MEMBERS = 11;
    /**
     * URI ID for route: /members/{ID}
     */
    public static final int ROUTE_MEMBERS_ID = 12;
    /**
     * URI ID for route: /members/users/{ID}
     */
    public static final int ROUTE_MEMBERS_USERS_ID = 13;

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, "notes", ROUTE_NOTES);
        sUriMatcher.addURI(AUTHORITY, "notes/#", ROUTE_NOTES_ID);
        sUriMatcher.addURI(AUTHORITY, "notes/users/#", ROUTE_NOTES_USERS_ID);
        sUriMatcher.addURI(AUTHORITY, "groups", ROUTE_GROUPS);
        sUriMatcher.addURI(AUTHORITY, "groups/#", ROUTE_GROUPS_ID);
        sUriMatcher.addURI(AUTHORITY, "groups/users/#", ROUTE_GROUPS_USERS_ID);
        sUriMatcher.addURI(AUTHORITY, "users", ROUTE_USERS);
        sUriMatcher.addURI(AUTHORITY, "users/#", ROUTE_USERS_ID);
        sUriMatcher.addURI(AUTHORITY, "members", ROUTE_MEMBERS);
        sUriMatcher.addURI(AUTHORITY, "members/#", ROUTE_MEMBERS_ID);
        sUriMatcher.addURI(AUTHORITY, "members/users/#", ROUTE_MEMBERS_USERS_ID);
    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new Database(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri,
                        String[] projection,
                        String selection,
                        String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        SelectionBuilder builder = new SelectionBuilder();
        String id = uri.getLastPathSegment();
        Cursor c;
        Context ctx = getContext();

        // queried table
        switch (sUriMatcher.match(uri)) {
            case ROUTE_NOTES:
            case ROUTE_NOTES_ID:
            case ROUTE_NOTES_USERS_ID:
            case ROUTE_NOTES_GROUPS_ID:
                builder.table(Note.TABLE_NAME);
                break;
            case ROUTE_GROUPS:
            case ROUTE_GROUPS_ID:
                builder.table(Group.TABLE_NAME);
                break;
            case ROUTE_GROUPS_USERS_ID:
                builder.table(Member.TABLE_NAME + " JOIN " + Group.TABLE_NAME+ " ON "
                        + Member.TABLE_NAME + "." + Member.COLUMN_NAME_GROUP_ID + "="
                        + Group.TABLE_NAME + "." + Group.COLUMN_NAME_GROUP_ID);
                break;
            case ROUTE_USERS:
            case ROUTE_USERS_ID:
            case ROUTE_USERS_GROUPS_ID:
                builder.table(User.TABLE_NAME);
                break;
            case ROUTE_MEMBERS:
            case ROUTE_MEMBERS_ID:
            case ROUTE_MEMBERS_USERS_ID:
                builder.table(Member.TABLE_NAME);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        switch (sUriMatcher.match(uri)) {
            case ROUTE_NOTES_ID:
                builder.where(Note.COLUMN_NAME_NOTE_ID + "=?", id);
                break;
            case ROUTE_NOTES_USERS_ID:
                builder.where(Note.COLUMN_NAME_USER_ID + "=?", id);
                break;
            case ROUTE_GROUPS_ID:
                builder.where(Group._ID + "=?", id);
                break;
            case ROUTE_GROUPS_USERS_ID:
                builder.where(Member.COLUMN_NAME_USER_ID + "=?", id)
                        .mapToTable(Group._ID, Group.TABLE_NAME)
                        .mapToTable(Group.COLUMN_NAME_GROUP_ID, Group.TABLE_NAME);
                break;
            case ROUTE_USERS_ID:
                builder.where(User.COLUMN_NAME_USER_ID + "=?", id);
                break;
            case ROUTE_MEMBERS_ID:
                builder.where(Member.COLUMN_NAME_MEMBER_ID + "=?", id);
                break;
            case ROUTE_MEMBERS_USERS_ID:
                builder.where(Member.COLUMN_NAME_USER_ID + "=?", id);
                break;
        }

        builder.where(selection, selectionArgs);
        c = builder.query(db, projection, sortOrder);
        // Note: Notification URI must be manually set here for loaders to correctly
        // register ContentObservers.
        assert ctx != null;
        c.setNotificationUri(ctx.getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case ROUTE_NOTES:
                return Note.TYPE;
            case ROUTE_NOTES_ID:
                return Note.ITEM_TYPE;
            case ROUTE_GROUPS:
                return Group.TYPE;
            case ROUTE_GROUPS_ID:
                return Group.ITEM_TYPE;
            case ROUTE_USERS:
                return User.TYPE;
            case ROUTE_USERS_ID:
                return User.ITEM_TYPE;
            case ROUTE_MEMBERS:
                return Member.TYPE;
            case ROUTE_MEMBERS_ID:
                return Member.ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri,
                      ContentValues values) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        assert db != null;
        Uri result;
        long id;
        switch (sUriMatcher.match(uri)) {
            case ROUTE_NOTES:
                id = db.insertOrThrow(Note.TABLE_NAME, null, values);
                result = Uri.parse(Note.URI + "/" + id);
                break;
            case ROUTE_GROUPS:
                id = db.insertOrThrow(Group.TABLE_NAME, null, values);
                result = Uri.parse(Group.URI + "/" + id);
                break;
            case ROUTE_USERS:
                id = db.insertOrThrow(User.TABLE_NAME, null, values);
                result = Uri.parse(User.URI + "/" + id);
                break;
            case ROUTE_MEMBERS:
                id = db.insertOrThrow(Member.TABLE_NAME, null, values);
                result = Uri.parse(Member.URI + "/" + id);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return result;
    }

    @Override
    public int delete(Uri uri,
                      String selection,
                      String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        String id = uri.getLastPathSegment();
        switch (match) {
            case ROUTE_NOTES:
                count = builder.table(Note.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_NOTES_ID:
                count = builder.table(Note.TABLE_NAME)
                        .where(Note._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_GROUPS:
                count = builder.table(Group.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_GROUPS_ID:
                count = builder.table(Group.TABLE_NAME)
                        .where(Group._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_USERS:
                count = builder.table(User.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_USERS_ID:
                count = builder.table(User.TABLE_NAME)
                        .where(User._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_MEMBERS:
                count = builder.table(Member.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_MEMBERS_ID:
                count = builder.table(Member.TABLE_NAME)
                        .where(Member._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Send broadcast to registered ContentObservers, to refresh UI.
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    @Override
    public int update(Uri uri,
                      ContentValues values,
                      String selection,
                      String[] selectionArgs) {
        SelectionBuilder builder = new SelectionBuilder();
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int count;
        String id = uri.getLastPathSegment();
        switch (match) {
            case ROUTE_NOTES:
                count = builder.table(Note.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_NOTES_ID:
                count = builder.table(Note.TABLE_NAME)
                        .where(Note._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_GROUPS:
                count = builder.table(Group.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_GROUPS_ID:
                count = builder.table(Group.TABLE_NAME)
                        .where(Group._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_USERS:
                count = builder.table(User.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_USERS_ID:
                count = builder.table(User.TABLE_NAME)
                        .where(User._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_MEMBERS:
                count = builder.table(Member.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_MEMBERS_ID:
                count = builder.table(Member.TABLE_NAME)
                        .where(Member._ID + "=?", id)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        Context ctx = getContext();
        assert ctx != null;
        ctx.getContentResolver().notifyChange(uri, null, false);
        return count;
    }

    public static void insertOrUpdate(Context context, User user)
            throws RemoteException, OperationApplicationException {
        ContentResolver resolver = context.getContentResolver();
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        Uri uri = User.URI.buildUpon().appendPath(Integer.toString(user.getUserId())).build();
        Cursor c = resolver.query(uri, User.PROJECTION, null, null, null);
        if (c.moveToFirst()) {
            User match = new User(c);
            if (!match.equals(user)) {
                batch.add(ContentProviderOperation.newUpdate(uri)
                        .withValue(User.COLUMN_NAME_NAME, user.getName())
                        .withValue(User.COLUMN_NAME_SURNAME, user.getSurname())
                        .withValue(User.COLUMN_NAME_EMAIL, user.getEmail())
                        .withValue(User.COLUMN_NAME_UNIVERSITY, user.getUniversity())
                        .withValue(User.COLUMN_NAME_INFO, user.getInfo())
                        .withValue(User.COLUMN_NAME_RANK, user.getRank())
                        .build());
            }
        }
        else {
            batch.add(ContentProviderOperation.newInsert(User.URI)
                    .withValue(User.COLUMN_NAME_USER_ID, user.getUserId())
                    .withValue(User.COLUMN_NAME_NAME, user.getName())
                    .withValue(User.COLUMN_NAME_SURNAME, user.getSurname())
                    .withValue(User.COLUMN_NAME_EMAIL, user.getEmail())
                    .withValue(User.COLUMN_NAME_UNIVERSITY, user.getUniversity())
                    .withValue(User.COLUMN_NAME_INFO, user.getInfo())
                    .withValue(User.COLUMN_NAME_RANK, user.getRank())
                    .build());
        }
        resolver.applyBatch(Provider.AUTHORITY, batch);
        resolver.notifyChange(User.URI,  null, false);
    }

    /**
     * SQLite backend for @{link Provider}.
     *
     * Provides access to an disk-backed, SQLite datastore which is utilized by Provider. This
     * database should never be accessed by other parts of the application directly.
     */
    static class Database extends SQLiteOpenHelper {
        /** Schema version. */
        public static final int DATABASE_VERSION = 1;
        /** Filename for SQLite file. */
        public static final String DATABASE_NAME = "universitysync.db";

        private static final String TYPE_TEXT = " TEXT";
        private static final String TYPE_INTEGER = " INTEGER";
        private static final String COMMA_SEP = ",";

        /** SQL statement to create "user" table. */
        private static final String SQL_CREATE_USERS =
                "CREATE TABLE " + User.TABLE_NAME + " (" +
                        User._ID + " INTEGER PRIMARY KEY," +
                        User.COLUMN_NAME_USER_ID + TYPE_INTEGER + COMMA_SEP +
                        User.COLUMN_NAME_NAME + TYPE_TEXT + COMMA_SEP +
                        User.COLUMN_NAME_SURNAME + TYPE_TEXT + COMMA_SEP +
                        User.COLUMN_NAME_EMAIL + TYPE_TEXT + COMMA_SEP +
                        User.COLUMN_NAME_UNIVERSITY + TYPE_TEXT + COMMA_SEP +
                        User.COLUMN_NAME_INFO + TYPE_TEXT + COMMA_SEP +
                        User.COLUMN_NAME_RANK + TYPE_INTEGER + ")";

        /** SQL statement to drop "user" table. */
        private static final String SQL_DELETE_USERS =
                "DROP TABLE IF EXISTS " + User.TABLE_NAME;

        /** SQL statement to create "group" table. */
        private static final String SQL_CREATE_GROUPS =
                "CREATE TABLE " + Group.TABLE_NAME + " (" +
                        Group._ID + " INTEGER PRIMARY KEY," +
                        Group.COLUMN_NAME_GROUP_ID + TYPE_INTEGER + COMMA_SEP +
                        Group.COLUMN_NAME_NAME + TYPE_TEXT + COMMA_SEP +
                        Group.COLUMN_NAME_UNIVERSITY + TYPE_TEXT + COMMA_SEP +
                        Group.COLUMN_NAME_INFO + TYPE_TEXT + COMMA_SEP +
                        Group.COLUMN_NAME_PUBLIC + TYPE_INTEGER + COMMA_SEP +
                        Group.COLUMN_NAME_MEMBER_INFO + TYPE_TEXT + ")";

        /** SQL statement to drop "group" table. */
        private static final String SQL_DELETE_GROUPS =
                "DROP TABLE IF EXISTS " + Group.TABLE_NAME;

        /** SQL statement to create "member" table. */
        private static final String SQL_CREATE_MEMBERS =
                "CREATE TABLE " + Member.TABLE_NAME + " (" +
                        Member._ID + " INTEGER PRIMARY KEY," +
                        Member.COLUMN_NAME_MEMBER_ID + TYPE_INTEGER + COMMA_SEP +
                        Member.COLUMN_NAME_USER_ID + TYPE_INTEGER + COMMA_SEP +
                        Member.COLUMN_NAME_GROUP_ID + TYPE_INTEGER + COMMA_SEP +
                        Member.COLUMN_NAME_ADMIN + TYPE_INTEGER + ")";

        /** SQL statement to drop "member" table. */
        private static final String SQL_DELETE_MEMBERS =
                "DROP TABLE IF EXISTS " + Member.TABLE_NAME;

        /** SQL statement to create "note" table. */
        private static final String SQL_CREATE_NOTES =
                "CREATE TABLE " + Note.TABLE_NAME + " (" +
                        Note._ID + " INTEGER PRIMARY KEY," +
                        Note.COLUMN_NAME_NOTE_ID + TYPE_INTEGER + COMMA_SEP +
                        Note.COLUMN_NAME_USER_ID + TYPE_INTEGER + COMMA_SEP +
                        Note.COLUMN_NAME_GROUP_ID + TYPE_INTEGER + COMMA_SEP +
                        Note.COLUMN_NAME_LIKES + TYPE_INTEGER + COMMA_SEP +
                        Note.COLUMN_NAME_DATE + TYPE_INTEGER + ")";

        /** SQL statement to drop "note" table. */
        private static final String SQL_DELETE_NOTES =
                "DROP TABLE IF EXISTS " + Note.TABLE_NAME;

        public Database(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_USERS);
            db.execSQL(SQL_CREATE_GROUPS);
            db.execSQL(SQL_CREATE_MEMBERS);
            db.execSQL(SQL_CREATE_NOTES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_NOTES);
            db.execSQL(SQL_DELETE_MEMBERS);
            db.execSQL(SQL_DELETE_USERS);
            db.execSQL(SQL_DELETE_GROUPS);
            onCreate(db);
        }
    }
}
