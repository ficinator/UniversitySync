package sk.mikme.universitysync.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by fic on 21.9.2014.
 */
public class User implements BaseColumns, Parcelable {
    /**
     * Path component for "user"-type resources..
     */
    public static final String PATH = "users";
    /**
     * MIME type for lists of users.
     */
    public static final String TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.universitysync.users";
    /**
     * MIME type for individual user.
     */
    public static final String ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.universitysync.user";
    /**
     * Fully qualified URI for "user" resources.
     */
    public static final Uri URI = Provider.BASE_URI.buildUpon().appendPath(PATH).build();

    public static final String TABLE_NAME = "user";
    public static final String COLUMN_NAME_USER_ID = "user_id";
    public static final String COLUMN_NAME_NAME = "name";
    public static final String COLUMN_NAME_SURNAME = "surname";
    public static final String COLUMN_NAME_EMAIL = "email";
    public static final String COLUMN_NAME_UNIVERSITY = "university";
    public static final String COLUMN_NAME_INFO = "info";
    public static final String COLUMN_NAME_RANK = "rank";

    /**
     * Projection for querying the content provider.
     */
    public static final String[] PROJECTION = new String[]{
            _ID,
            COLUMN_NAME_USER_ID,
            COLUMN_NAME_NAME,
            COLUMN_NAME_SURNAME,
            COLUMN_NAME_EMAIL,
            COLUMN_NAME_UNIVERSITY,
            COLUMN_NAME_INFO,
            COLUMN_NAME_RANK
    };

    public static final int COLUMN_ID = 0;
    public static final int COLUMN_USER_ID = 1;
    public static final int COLUMN_NAME = 2;
    public static final int COLUMN_SURNAME = 3;
    public static final int COLUMN_EMAIL = 4;
    public static final int COLUMN_UNIVERSITY = 5;
    public static final int COLUMN_INFO = 6;
    public static final int COLUMN_RANK = 7;

    private int mUserId;
    private String mName;
    private String mSurname;
    private String mEmail;
    private String mUniversity;
    private String mInfo;
    private int mRank;
    private String mAuthToken;
    //private Drawable mThumb;

    public User() {}

    public User(JSONObject object) throws JSONException {
        this.mUserId = object.getInt("id");
        this.mName = object.getString("name");
        this.mSurname = object.getString("surname");
        this.mEmail = object.getString("email");
        this.mUniversity = object.getString("university");
        this.mInfo = object.getString("info");
        this.mRank = object.getInt("rank");
    }

    public User(Cursor c) {
        mUserId = c.getInt(COLUMN_USER_ID);
        mName = c.getString(COLUMN_NAME);
        mSurname = c.getString(COLUMN_SURNAME);
        mEmail = c.getString(COLUMN_EMAIL);
        mUniversity = c.getString(COLUMN_UNIVERSITY);
        mInfo = c.getString(COLUMN_INFO);
        mRank = c.getInt(COLUMN_RANK);
    }

    public User(Parcel in){
        mUserId = in.readInt();
        mAuthToken = in.readString();
        mName = in.readString();
        mSurname = in.readString();
        mEmail = in.readString();
        mUniversity = in.readString();
        mInfo = in.readString();
        mRank = in.readInt();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof User) {
            User user = (User) o;
            return (getName().equals(user.getName()) &&
                    getSurname().equals(user.getSurname()) &&
                    getEmail().equals(user.getEmail()) &&
                    getUniversity().equals(user.getUniversity()) &&
                    getInfo().equals(user.getInfo()) &&
                    getRank() == user.getRank());
        }
        return false;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mUserId);
        out.writeString(mAuthToken);
        out.writeString(mName);
        out.writeString(mSurname);
        out.writeString(mEmail);
        out.writeString(mUniversity);
        out.writeString(mInfo);
        out.writeInt(mRank);
        //if (mThumb != null) {
        //    Bitmap bmp = ((BitmapDrawable) mThumb).getBitmap();
        //    out.writeParcelable(bmp, flags);
        //}
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public String getSurname() {
        return mSurname;
    }

    public void setSurname(String mSurname) {
        this.mSurname = mSurname;
    }

    public String getUniversity() {
        return mUniversity;
    }

    public void setUniversity(String mUniversity) {
        this.mUniversity = mUniversity;
    }

    public String getInfo() {
        return mInfo;
    }

    public void setInfo(String mInfo) {
        this.mInfo = mInfo;
    }

    public int getRank() {
        return mRank;
    }

    public void setRank(int mRank) {
        this.mRank = mRank;
    }

    public String getFullName() {
        return mName + " " + mSurname;
    }
    public String getThumbPath() { return "../users/" + mUserId + "/userPhoto.jpg"; }

    //public void setThumb(Drawable thumb) { mThumb = thumb; }

    //public Drawable getThumb() { return mThumb; }

    public int getUserId() {
        return mUserId;
    }

    public void setUserId(int mUserId) {
        this.mUserId = mUserId;
    }
    public String getAuthToken() {
        return mAuthToken;
    }

    public void setAuthToken(String mAuthToken) {
        this.mAuthToken = mAuthToken;
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
