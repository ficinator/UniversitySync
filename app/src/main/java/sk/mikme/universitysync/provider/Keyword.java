package sk.mikme.universitysync.provider;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.HashMap;

import sk.mikme.universitysync.sync.SyncAdapter;

/**
 * Created by fic on 4.10.2014.
 */
public class Keyword implements BaseColumns, Parcelable {
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

    public Keyword(Parcel in) {
        mId = in.readInt();
        mName = in.readString();
    }

    public boolean equals(Object o) {
        if (o instanceof Keyword)
            return mName.equals(((Keyword) o).getName());
        return false;
    }

    public Keyword(String name) {
        mId = -1;
        mName = name;
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

    public void setId(int id) {
        mId = id;
    }

    public int insert(Context context) throws RemoteException, OperationApplicationException {
        ContentResolver contentResolver = context.getContentResolver();
        ContentProviderResult[] result;
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        // get keywords from database
        Cursor c = contentResolver.query(Keyword.URI, Keyword.PROJECTION, null, null, null);

        int keywordId = -1;
        c.moveToPosition(-1);
        while (c.moveToNext() && keywordId == -1) {
            Keyword oldKeyword = new Keyword(c);
            if (oldKeyword.getName().equals(getName()))
                keywordId = oldKeyword.getId();
        }
        c.close();
        if (keywordId == -1) {
            batch.add(ContentProviderOperation.newInsert(Keyword.URI)
                    .withValue(Keyword.COLUMN_NAME_NAME, getName()).build());
            result = contentResolver.applyBatch(Provider.AUTHORITY, batch);
            if (result.length > 0) {
                contentResolver.notifyChange(Keyword.URI, null, false);
                return Integer.parseInt(result[0].uri.getLastPathSegment());
            }
            return -1;
        }
        return keywordId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(mId);
        parcel.writeString(mName);
    }

    public static final Parcelable.Creator<Keyword> CREATOR = new Parcelable.Creator<Keyword>() {
        public Keyword createFromParcel(Parcel in) {
            return new Keyword(in);
        }

        public Keyword[] newArray(int size) {
            return new Keyword[size];
        }
    };
}
