package sk.mikme.universitysync.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
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

import java.security.Key;
import java.util.ArrayList;

import sk.mikme.universitysync.sync.SyncAdapter;

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
     * URI ID for route: /keywords
     */
    public static final int ROUTE_KEYWORDS = 14;
    /**
     * URI ID for route: /keywords/{NAME}
     */
    public static final int ROUTE_KEYWORDS_NAME = 15;
    /**
     * URI ID for route: /note_keywords
     */
    public static final int ROUTE_NOTE_KEYWORDS = 16;
    /**
     * URI ID for route: /note_keywords/{ID}
     */
    public static final int ROUTE_NOTE_KEYWORDS_ID = 17;
    /**
     * URI ID for route: /note_keywords/notes/{ID}
     */
    public static final int ROUTE_NOTE_KEYWORDS_NOTES_ID = 18;

    /**
     * UriMatcher, used to decode incoming URIs.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, "keywords", ROUTE_KEYWORDS);
        sUriMatcher.addURI(AUTHORITY, "keywords/*", ROUTE_KEYWORDS_NAME);
        sUriMatcher.addURI(AUTHORITY, "note_keywords", ROUTE_NOTE_KEYWORDS);
        sUriMatcher.addURI(AUTHORITY, "note_keywords/#", ROUTE_NOTE_KEYWORDS_ID);
        sUriMatcher.addURI(AUTHORITY, "note_keywords/notes/#", ROUTE_NOTE_KEYWORDS_NOTES_ID);
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
                        + Group.TABLE_NAME + "." + Group.COLUMN_NAME_GROUP_ID)
                        .mapToTable(Group._ID, Group.TABLE_NAME)
                        .mapToTable(Group.COLUMN_NAME_GROUP_ID, Group.TABLE_NAME);
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
            case ROUTE_KEYWORDS:
            case ROUTE_KEYWORDS_NAME:
                builder.table(Keyword.TABLE_NAME);
                break;
            case ROUTE_NOTE_KEYWORDS_ID:
                builder.table(NoteKeyword.TABLE_NAME);
                break;
            case ROUTE_NOTE_KEYWORDS:
            case ROUTE_NOTE_KEYWORDS_NOTES_ID:
                builder.table(NoteKeyword.TABLE_NAME + " JOIN " + Keyword.TABLE_NAME + " ON "
                        + NoteKeyword.TABLE_NAME + "." + NoteKeyword.COLUMN_NAME_KEYWORD_ID + "="
                        + Keyword.TABLE_NAME + "." + Keyword._ID)
                        .mapToTable(NoteKeyword._ID, NoteKeyword.TABLE_NAME);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        switch (sUriMatcher.match(uri)) {
            case ROUTE_NOTES_ID:
                builder.where(Note._ID + "=?", id);
                break;
            case ROUTE_NOTES_USERS_ID:
                builder.where(Note.COLUMN_NAME_USER_ID + "=?", id);
                break;
            case ROUTE_GROUPS_ID:
                builder.where(Group._ID + "=?", id);
                break;
            case ROUTE_GROUPS_USERS_ID:
                builder.where(Member.COLUMN_NAME_USER_ID + "=?", id);
                break;
            case ROUTE_USERS_ID:
                builder.where(User.COLUMN_NAME_USER_ID + "=?", id);
                break;
            case ROUTE_MEMBERS_ID:
                builder.where(Member._ID + "=?", id);
                break;
            case ROUTE_MEMBERS_USERS_ID:
                builder.where(Member.COLUMN_NAME_USER_ID + "=?", id);
                break;
            case ROUTE_KEYWORDS_NAME:
                builder.where(Keyword.COLUMN_NAME_NAME + "=?", id);
                break;
            case ROUTE_NOTE_KEYWORDS_ID:
                builder.where(NoteKeyword._ID + "=?", id);
                break;
            case ROUTE_NOTE_KEYWORDS_NOTES_ID:
                builder.where(Note.COLUMN_NAME_NOTE_ID + "=?", id);
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
            case ROUTE_KEYWORDS:
                return Keyword.TYPE;
            case ROUTE_KEYWORDS_NAME:
                return Keyword.ITEM_TYPE;
            case ROUTE_NOTE_KEYWORDS:
                return NoteKeyword.TYPE;
            case ROUTE_NOTE_KEYWORDS_ID:
                return NoteKeyword.ITEM_TYPE;
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
            case ROUTE_KEYWORDS:
                id = db.insertOrThrow(Keyword.TABLE_NAME, null, values);
                result = Uri.parse(Keyword.URI + "/" + id);
                break;
            case ROUTE_NOTE_KEYWORDS:
                id = db.insertOrThrow(NoteKeyword.TABLE_NAME, null, values);
                result = Uri.parse(NoteKeyword.URI + "/" + id);
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
        int count;
        String id = uri.getLastPathSegment();
        switch (sUriMatcher.match(uri)) {
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
            case ROUTE_KEYWORDS:
                count = builder.table(Keyword.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_KEYWORDS_NAME:
                count = builder.table(Keyword.TABLE_NAME)
                        .where(Keyword.COLUMN_NAME_NAME + "=?", id)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_NOTE_KEYWORDS:
                count = builder.table(NoteKeyword.TABLE_NAME)
                        .where(selection, selectionArgs)
                        .delete(db);
                break;
            case ROUTE_NOTE_KEYWORDS_ID:
                count = builder.table(NoteKeyword.TABLE_NAME)
                        .where(NoteKeyword._ID + "=?", id)
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
    public static class Database extends SQLiteOpenHelper {
        /** Schema version. */
        public static final int DATABASE_VERSION = 1;
        /** Filename for SQLite file. */
        public static final String DATABASE_NAME = "universitysync.db";

        private static final String SQL_ALLOW_FK = "PRAGMA foreign_keys=ON;";

        /** SQL statement to create "user" table. */
        private static final String SQL_CREATE_USERS =
                "CREATE TABLE " + User.TABLE_NAME + " (" +
                        User._ID + " INTEGER PRIMARY KEY," +
                        User.COLUMN_NAME_USER_ID + " INTEGER," +
                        User.COLUMN_NAME_NAME + " TEXT," +
                        User.COLUMN_NAME_SURNAME + " TEXT," +
                        User.COLUMN_NAME_EMAIL + " TEXT," +
                        User.COLUMN_NAME_UNIVERSITY + " TEXT," +
                        User.COLUMN_NAME_INFO + " TEXT," +
                        User.COLUMN_NAME_RANK + " INTEGER)";

        /** SQL statement to drop "user" table. */
        private static final String SQL_DELETE_USERS =
                "DROP TABLE IF EXISTS " + User.TABLE_NAME;

        /** SQL statement to create "group" table. */
        private static final String SQL_CREATE_GROUPS =
                "CREATE TABLE " + Group.TABLE_NAME + " (" +
                        Group._ID + " INTEGER PRIMARY KEY," +
                        Group.COLUMN_NAME_GROUP_ID + " INTEGER," +
                        Group.COLUMN_NAME_NAME + " TEXT," +
                        Group.COLUMN_NAME_UNIVERSITY + " TEXT," +
                        Group.COLUMN_NAME_INFO + " TEXT," +
                        Group.COLUMN_NAME_PUBLIC + " INTEGER," +
                        Group.COLUMN_NAME_MEMBER_INFO + " TEXT)";

        /** SQL statement to drop "group" table. */
        private static final String SQL_DELETE_GROUPS =
                "DROP TABLE IF EXISTS " + Group.TABLE_NAME;

        /** SQL statement to create "member" table. */
        private static final String SQL_CREATE_MEMBERS =
                "CREATE TABLE " + Member.TABLE_NAME + " (" +
                        Member._ID + " INTEGER PRIMARY KEY," +
                        Member.COLUMN_NAME_USER_ID + " INTEGER" +
                        " REFERENCES " + User.TABLE_NAME + "(" + User._ID + ") ON DELETE CASCADE," +
                        Member.COLUMN_NAME_GROUP_ID + " INTEGER" +
                        " REFERENCES " + Group.TABLE_NAME + "(" + Group._ID + ") ON DELETE CASCADE," +
                        Member.COLUMN_NAME_ADMIN + " INTEGER)";

        /** SQL statement to drop "member" table. */
        private static final String SQL_DELETE_MEMBERS =
                "DROP TABLE IF EXISTS " + Member.TABLE_NAME;

        /** SQL statement to create "note" table. */
        private static final String SQL_CREATE_NOTES =
                "CREATE TABLE " + Note.TABLE_NAME + " (" +
                        Note._ID + " INTEGER PRIMARY KEY," +
                        Note.COLUMN_NAME_NOTE_ID + " INTEGER," +
                        Note.COLUMN_NAME_USER_ID + " INTEGER" +
                        " REFERENCES " + User.TABLE_NAME + "(" + User._ID + ") ON DELETE SET NULL," +
                        Note.COLUMN_NAME_GROUP_ID + " INTEGER" +
                        " REFERENCES " + Group.TABLE_NAME + "(" + Group._ID + ") ON DELETE CASCADE," +
                        Note.COLUMN_NAME_TITLE + " TEXT," +
                        Note.COLUMN_NAME_LIKES + " INTEGER," +
                        Note.COLUMN_NAME_DATE + " INTEGER," +
                        Note.COLUMN_NAME_CONTENT + " TEXT)";

        /** SQL statement to drop "note" table. */
        private static final String SQL_DELETE_NOTES =
                "DROP TABLE IF EXISTS " + Note.TABLE_NAME;

        /** SQL statement to create "keyword" table. */
        private static final String SQL_CREATE_KEYWORDS =
                "CREATE TABLE " + Keyword.TABLE_NAME + " (" +
                        Keyword._ID + " INTEGER PRIMARY KEY," +
                        Keyword.COLUMN_NAME_NAME + " TEXT)";

        /** SQL statement to drop "keyword" table. */
        private static final String SQL_DELETE_KEYWORDS =
                "DROP TABLE IF EXISTS " + Keyword.TABLE_NAME;

        /** SQL statement to create "note_keyword" table. */
        private static final String SQL_CREATE_NOTE_KEYWORDS =
                "CREATE TABLE " + NoteKeyword.TABLE_NAME + " (" +
                        NoteKeyword._ID + " INTEGER PRIMARY KEY," +
                        NoteKeyword.COLUMN_NAME_NOTE_ID + " INTEGER" +
                        " REFERENCES " + Note.TABLE_NAME + "(" + Note._ID + ") ON DELETE CASCADE," +
                        NoteKeyword.COLUMN_NAME_KEYWORD_ID + " INTEGER" +
                        " REFERENCES " + Keyword.TABLE_NAME + "(" + Keyword._ID + ") ON DELETE CASCADE)";

        /** SQL statement to drop "keyword" table. */
        private static final String SQL_DELETE_NOTE_KEYWORDS =
                "DROP TABLE IF EXISTS " + NoteKeyword.TABLE_NAME;

        public Database(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_ALLOW_FK);
            db.execSQL(SQL_CREATE_USERS);
            db.execSQL(SQL_CREATE_GROUPS);
            db.execSQL(SQL_CREATE_MEMBERS);
            db.execSQL(SQL_CREATE_KEYWORDS);
            db.execSQL(SQL_CREATE_NOTES);
            db.execSQL(SQL_CREATE_NOTE_KEYWORDS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(SQL_DELETE_NOTE_KEYWORDS);
            db.execSQL(SQL_DELETE_NOTES);
            db.execSQL(SQL_DELETE_KEYWORDS);
            db.execSQL(SQL_DELETE_MEMBERS);
            db.execSQL(SQL_DELETE_GROUPS);
            db.execSQL(SQL_DELETE_USERS);
            onCreate(db);
        }
    }
}
