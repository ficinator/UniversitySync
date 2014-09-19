package sk.mikme.universitysync.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by fic on 19.9.2014.
 */
public class Group implements BaseColumns {
    /**
     * Path component for "group"-type resources..
     */
    public static final String PATH = "groups";
    /**
     * MIME type for lists of groups.
     */
    public static final String TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.universitysync.groups";
    /**
     * MIME type for individual group.
     */
    public static final String ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.universitysync.group";
    /**
     * Fully qualified URI for "group" resources.
     */
    public static final Uri URI = Provider.BASE_URI.buildUpon().appendPath(PATH).build();

    public static final String TABLE_NAME = "unisync_group";
    public static final String COLUMN_NAME_GROUP_ID = "group_id";
    public static final String COLUMN_NAME_NAME = "name";
    public static final String COLUMN_NAME_UNIVERSITY = "university";
    public static final String COLUMN_NAME_INFO = "info";
    public static final String COLUMN_NAME_PUBLIC = "public";
    public static final String COLUMN_NAME_MEMBER_INFO = "member_info";

    /**
     * Projection for querying the content provider.
     */
    public static final String[] PROJECTION = new String[]{
            _ID,
            COLUMN_NAME_GROUP_ID,
            COLUMN_NAME_NAME,
            COLUMN_NAME_UNIVERSITY,
            COLUMN_NAME_INFO,
            COLUMN_NAME_PUBLIC,
            COLUMN_NAME_MEMBER_INFO
    };

    public static final int COLUMN_ID = 0;
    public static final int COLUMN_GROUP_ID = 1;
    public static final int COLUMN_NAME = 2;
    public static final int COLUMN_UNIVERSITY = 3;
    public static final int COLUMN_INFO = 4;
    public static final int COLUMN_PUBLIC = 5;
    public static final int COLUMN_MEMBER_INFO = 6;

    private int mGroupId;
    private String mName;
    private String mUniversity;
    private String mInfo;
    private boolean mIsPublic;
    private String mMemberInfo;
    //private Drawable mThumb;

    public Group(JSONObject object) throws JSONException {
        this.mGroupId = object.getInt("id");
        this.mName = object.getString("name");
        this.mUniversity = object.getString("university");
        this.mInfo = object.getString("info");
        this.mIsPublic = object.getBoolean("public");
        this.mMemberInfo = object.getString("member_info");
    }

    public int getGroupId() {
        return mGroupId;
    }

    public void setGroupId(int mGroupId) {
        this.mGroupId = mGroupId;
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

    public boolean isPublic() {
        return mIsPublic;
    }

    public void setPublic(boolean mIsPublic) {
        this.mIsPublic = mIsPublic;
    }

    public String getMemberInfo() {
        return mMemberInfo;
    }

    public void setMemberInfo(String mMemberInfo) {
        this.mMemberInfo = mMemberInfo;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getThumbPath() { return "../groups/" + mGroupId + "/groupPhoto.jpg"; }

    //public void setThumb(Drawable thumb) { mThumb = thumb; }

    //public Drawable getThumb() { return mThumb; }

//    @Override
//    public void writeToParcel(Parcel out, int flags) {
//        super.writeToParcel(out, flags);
//        out.writeString(mName);
//        out.writeString(mUniversity);
//        out.writeString(mInfo);
//        out.writeInt(mIsPublic);
//        out.writeString(mMemberInfo);
//        if (mThumb != null) {
//            Bitmap bmp = ((BitmapDrawable) mThumb).getBitmap();
//            out.writeParcelable(bmp, flags);
//        }
//    }
}
