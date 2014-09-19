package sk.mikme.universitysync.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Parcel;
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
public class Note implements BaseColumns {
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
    public static final String COLUMN_NAME_LIKES = "likes";
    public static final String COLUMN_NAME_DATE = "date";

    /**
     * Projection for querying the content provider.
     */
    public static final String[] PROJECTION = new String[]{
            _ID,
            COLUMN_NAME_NOTE_ID,
            COLUMN_NAME_USER_ID,
            COLUMN_NAME_GROUP_ID,
            COLUMN_NAME_LIKES,
            COLUMN_NAME_DATE,
    };

    public static final int COLUMN_ID = 0;
    public static final int COLUMN_NOTE_ID = 1;
    public static final int COLUMN_USER_ID = 2;
    public static final int COLUMN_GROUP_ID = 3;
    public static final int COLUMN_LIKES = 4;
    public static final int COLUMN_DATE = 5;

    private int mNoteId;
    private int mUserId;
    private int mGroupId;
    //private int mFolderId;
    private int mLikes;
    private String mPath;
    private long mDate;
    //private String mCategory;
    private List<String> mKeywords;
    private List<String> mReferences;
    private String mContent;

    public Note(JSONObject object) throws JSONException {
        mNoteId = object.getInt("id");
        mUserId = object.getInt("id_user");
        mGroupId = object.getInt("id_group");
        //mFolderId = object.getInt("id_folder");
        mLikes = object.getInt("likes");
        mPath = "." + object.getString("path");
        try {
            mDate = new SimpleDateFormat(DATE_FORMAT).parse(object.getString("date")).getTime();
        } catch (ParseException e) {
            mDate = 0;
        }
        //mCategory = object.getString("category");
        mKeywords = new ArrayList<String>();
        mReferences = new ArrayList<String>();
        mContent = "";
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

//    public int getFolderId() {
//        return mFolderId;
//    }

//    public void setFolderId(int mFolderId) {
//        this.mFolderId = mFolderId;
//    }

    public int getNoteId() {
        return mNoteId;
    }

    public void setNoteId(int mNoteId) {
        this.mNoteId = mNoteId;
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

//    public String getCategory() { return mCategory; }

//    public void setCategory(String mCategory) { this.mCategory = mCategory; }

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

//    @Override
//    public void writeToParcel(Parcel out, int flags) {
//        super.writeToParcel(out, flags);
//        out.writeInt(mUserId);
//        out.writeInt(mGroupId);
//        out.writeInt(mFolderId);
//        out.writeInt(mLikes);
//        out.writeString(mPath);
//        out.writeLong(mDate);
//        out.writeString(mCategory);
//        out.writeStringList(mKeywords);
//        out.writeStringList(mReferences);
//        out.writeString(mContent);
//    }

    public void setDetails(JSONObject object) {
        try {
            JSONObject details = object.getJSONObject("Note");

            mKeywords.clear();
            if (details.get("KeyWords") instanceof JSONObject) {
                JSONObject keywords = details.getJSONObject("KeyWords");
                if (!keywords.isNull("KW")) {
                    if (keywords.get("KW") instanceof JSONArray) {
                        JSONArray kws = keywords.getJSONArray("KW");
                        for (int i = 0; i < kws.length(); i++)
                            mKeywords.add(kws.getString(i));
                    }
                    else if (keywords.get("KW") instanceof String)
                        mKeywords.add(keywords.getString("KW"));
                }
            }

            mReferences.clear();
            if (details.get("References") instanceof JSONObject) {
                JSONObject references = details.getJSONObject("References");
                if (!references.isNull("Ref")) {
                    if (references.get("Ref") instanceof JSONArray) {
                        JSONArray refs = references.getJSONArray("Ref");
                        for (int i = 0; i < refs.length(); i++)
                            mReferences.add(refs.getString(i));
                    }
                    else if (references.get("Ref") instanceof String)
                        mReferences.add(references.getString("Ref"));
                }
            }

            if (details.get("Content") instanceof String)
                mContent = details.getString("Content");

        } catch (JSONException e) {
            //TODO:
            e.printStackTrace();
        }
    }
}
