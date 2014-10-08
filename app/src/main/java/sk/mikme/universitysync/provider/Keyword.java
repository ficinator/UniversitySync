package sk.mikme.universitysync.provider;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import sk.mikme.universitysync.sync.SyncAdapter;

/**
 * Created by fic on 4.10.2014.
 */
public class Keyword implements BaseColumns {
    public static final String PATH = "keywords";
    public static final Uri URI = Provider.BASE_URI.buildUpon().appendPath(PATH).build();
    /**
     * MIME type for lists of members.
     */
    public static final String TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.universitysync.keywords";
    /**
     * MIME type for individual group.
     */
    public static final String ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.universitysync.keyword" +
                    "";
    public static final String TABLE_NAME = "keyword";
    public static final String COLUMN_NAME_NAME = "name";

    /**
     * Projection for querying the content provider.
     */
    public static final String[] PROJECTION = new String[]{
            _ID,
            COLUMN_NAME_NAME,
    };

    public static final int COLUMN_ID = 0;
    public static final int COLUMN_NAME = 1;

    private int mId;
    private String mName;

    public Keyword(int id, String name) {
        mId = id;
        mName = name;
    }

    public boolean equals(Object o) {
        if (o instanceof Keyword)
            return mName.equals(((Keyword) o).getName());
        return false;
    }


    public Keyword(String mName) {
        this.mName = mName;
    }

    public Keyword(Cursor c) {
        mId = c.getInt(COLUMN_ID);
        mName = c.getString(COLUMN_NAME);
    }

    public String getName() {
        return mName;
    }

    public int getId() {
        return mId;
    }
}
