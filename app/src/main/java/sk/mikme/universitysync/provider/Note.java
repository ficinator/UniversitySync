package sk.mikme.universitysync.provider;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import sk.mikme.universitysync.provider.Provider;

/**
 * Created by fic on 18.9.2014.
 */
public class Note implements BaseColumns, Parcelable {
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    /**
     * Path component for "note"-type resources..
     */
    public static final String PATH = "notes";
    /**
     * MIME type for lists of notes.
     */
    public static final String TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.universitysync.notes";
    /**
     * MIME type for individual note.
     */
    public static final String ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.universitysync.note";
    /**
     * Fully qualified URI for "note" resources.
     */
    public static final Uri URI = Provider.BASE_URI.buildUpon().appendPath(PATH).build();

    public static final String TABLE_NAME = "note";
    public static final String COLUMN_NAME_NOTE_ID = "note_id";
    public static final String COLUMN_NAME_USER_ID = "user_id";
    public static final String COLUMN_NAME_GROUP_ID = "group_id";
    public static final String COLUMN_NAME_TITLE= "title";
    public static final String COLUMN_NAME_LIKES = "likes";
    public static final String COLUMN_NAME_DATE = "date";
    public static final String COLUMN_NAME_CONTENT = "content";

    /**
     * Projection for querying the content provider.
     */
    public static final String[] PROJECTION = new String[]{
            _ID,
            COLUMN_NAME_NOTE_ID,
            COLUMN_NAME_USER_ID,
            COLUMN_NAME_GROUP_ID,
            COLUMN_NAME_TITLE,
            COLUMN_NAME_LIKES,
            COLUMN_NAME_DATE,
            COLUMN_NAME_CONTENT,
    };

    public static final int COLUMN_ID = 0;
    public static final int COLUMN_NOTE_ID = 1;
    public static final int COLUMN_USER_ID = 2;
    public static final int COLUMN_GROUP_ID = 3;
    public static final int COLUMN_TITLE = 4;
    public static final int COLUMN_LIKES = 5;
    public static final int COLUMN_DATE = 6;
    public static final int COLUMN_CONTENT = 7;

    private int mNoteId;
    private int mUserId;
    private int mGroupId;
    private String mTitle;
    private int mLikes;
    private String mPath;
    private long mDate;
    private List<String> mKeywords;
    private List<String> mReferences;
    private String mContent;

    public Note(JSONObject object) throws JSONException {
        mNoteId = object.getInt("id");
        mUserId = object.getInt("id_user");
        mGroupId = object.getInt("id_group");
        mTitle = "";
        mLikes = object.getInt("likes");
        mPath = "." + object.getString("path");
        try {
            mDate = new SimpleDateFormat(DATE_FORMAT).parse(object.getString("date")).getTime();
        } catch (ParseException e) {
            mDate = new Date().getTime();
        }
        mKeywords = new ArrayList<String>();
        mReferences = new ArrayList<String>();
        mContent = "";
    }

    public Note(Parcel in) {
        mKeywords = new ArrayList<String>();
        mReferences = new ArrayList<String>();
        mNoteId = in.readInt();
        mUserId = in.readInt();
        mGroupId = in.readInt();
        mTitle = in.readString();
        mLikes = in.readInt();
        mPath = in.readString();
        mDate = in.readLong();
        in.readStringList(mKeywords);
        in.readStringList(mReferences);
        mContent = in.readString();
    }

    public Note(Cursor c) {
        mNoteId = c.getInt(COLUMN_NOTE_ID);
        mUserId = c.getInt(COLUMN_USER_ID);
        mGroupId = c.getInt(COLUMN_GROUP_ID);
        mTitle = c.getString(COLUMN_TITLE);
        mLikes = c.getInt(COLUMN_LIKES);
        mDate = c.getLong(COLUMN_DATE);
        mKeywords = new ArrayList<String>();
        mReferences = new ArrayList<String>();
        mContent = c.getString(COLUMN_CONTENT);
    }

    public int getUserId() {
        return mUserId;
    }

    public void setUserId(int mUserId) {
        this.mUserId = mUserId;
    }

    public int getGroupId() {
        return mGroupId;
    }

    public void setGroupId(int mGroupId) {
        this.mGroupId = mGroupId;
    }

    public int getNoteId() {
        return mNoteId;
    }

    public void setNoteId(int mNoteId) {
        this.mNoteId = mNoteId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public int getLikes() {
        return mLikes;
    }

    public void setLikes(int mLikes) {
        this.mLikes = mLikes;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String mPath) {
        this.mPath = mPath;
    }

    public long getDate() {
        return mDate;
    }

    public Date getDateTime() { return new Date(mDate); }

    public void setDate(long date) {
        mDate = date;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        this.mPath = content;
    }

    public List<String> getKeywords() { return mKeywords; }

    public void setKeywords(List<String> keywords) { mKeywords = keywords; }

    public List<String> getReferences() { return mReferences; }

    public void setReferences(List<String> references) { mReferences = references; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mNoteId);
        out.writeInt(mUserId);
        out.writeInt(mGroupId);
        //out.writeInt(mFolderId);
        out.writeString(mTitle);
        out.writeInt(mLikes);
        out.writeString(mPath);
        out.writeLong(mDate);
        //out.writeString(mCategory);
        out.writeStringList(mKeywords);
        out.writeStringList(mReferences);
        out.writeString(mContent);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    public void setDetails(JSONObject object) {
        try {
            mKeywords.clear();
            JSONArray keywords = object.getJSONArray("KeyWords");
            for (int i = 0; i < keywords.length(); i++)
                mKeywords.add(keywords.getString(i));

            mReferences.clear();
            JSONArray references = object.getJSONArray("References");
            for (int i = 0; i < references.length(); i++)
                mReferences.add(references.getString(i));

            mContent = object.getString("Content");
            mTitle = object.getString("Title");
        } catch (JSONException e) {
            //TODO:
            e.printStackTrace();
        }
    }

    public String getKeywordsString() {
        if (mKeywords.isEmpty())
            return "";
        String keywords = "";
        for (String keyword : mKeywords)
            keywords += keyword + ", ";
        return keywords.substring(0, keywords.length() - 2);

    }
}
