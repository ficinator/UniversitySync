package sk.mikme.universitysync.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

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
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, "notes", ROUTE_NOTES);
        sUriMatcher.addURI(AUTHORITY, "notes/*", ROUTE_NOTES_ID);
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
        switch (sUriMatcher.match(uri)) {
            case ROUTE_NOTES_ID:
                // Return a single note, by ID.
                String id = uri.getLastPathSegment();
                builder.where(Note._ID + "=?", id);
            case ROUTE_NOTES:
                // Return all known entries.
                builder.table(Note.TABLE_NAME)
                        .where(selection, selectionArgs);
                Cursor c = builder.query(db, projection, sortOrder);
                // Note: Notification URI must be manually set here for loaders to correctly
                // register ContentObservers.
                Context ctx = getContext();
                assert ctx != null;
                c.setNotificationUri(ctx.getContentResolver(), uri);
                return c;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case ROUTE_NOTES:
                return Note.TYPE;
            case ROUTE_NOTES_ID:
                return Note.ITEM_TYPE;
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
        switch (sUriMatcher.match(uri)) {
            case ROUTE_NOTES:
                long id = db.insertOrThrow(Note.TABLE_NAME, null, values);
                result = Uri.parse(Note.URI + "/" + id);
                break;
            case ROUTE_NOTES_ID:
                throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
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
        switch (match) {
            case ROUTE_NOTES:
                count = builder.table(Note.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_NOTES_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(Note.TABLE_NAME)
                        .where(Note._ID + "=?", id)
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
        switch (match) {
            case ROUTE_NOTES:
                count = builder.table(Note.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .update(db, values);
                break;
            case ROUTE_NOTES_ID:
                String id = uri.getLastPathSegment();
                count = builder.table(Note.TABLE_NAME)
                        .where(Note._ID + "=?", id)
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
        /** SQL statement to create "note" table. */
        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + Note.TABLE_NAME + " (" +
                        Note._ID + " INTEGER PRIMARY KEY," +
                        Note.COLUMN_NAME_NOTE_ID + TYPE_INTEGER + COMMA_SEP +
                        Note.COLUMN_NAME_USER_ID + TYPE_INTEGER + COMMA_SEP +
                        Note.COLUMN_NAME_GROUP_ID + TYPE_INTEGER + COMMA_SEP +
                        Note.COLUMN_NAME_LIKES + TYPE_INTEGER + COMMA_SEP +
                        Note.COLUMN_NAME_DATE + TYPE_INTEGER + ")";

        /** SQL statement to drop "note" table. */
        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + Note.TABLE_NAME;

        public Database(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_ENTRIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
    }
}
