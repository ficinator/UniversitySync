package sk.mikme.universitysync.provider;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by fic on 22.9.2014.
 */
public class Member implements BaseColumns {
    /**
     * Path component for "member"-type resources..
     */
    public static final String PATH = "members";
    /**
     * MIME type for lists of members.
     */
    public static final String TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.universitysync.members";
    /**
     * MIME type for individual group.
     */
    public static final String ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.universitysync.member";
    /**
     * Fully qualified URI for "member" resources.
     */
    public static final Uri URI = Provider.BASE_URI.buildUpon().appendPath(PATH).build();

    public static final String TABLE_NAME = "member";
    public static final String COLUMN_NAME_MEMBER_ID = "member_id";
    public static final String COLUMN_NAME_USER_ID = "user_id";
    public static final String COLUMN_NAME_GROUP_ID = "group_id";
    public static final String COLUMN_NAME_ADMIN = "admin";

    /**
     * Projection for querying the content provider.
     */
    public static final String[] PROJECTION = new String[]{
            _ID,
            COLUMN_NAME_MEMBER_ID,
            COLUMN_NAME_USER_ID,
            COLUMN_NAME_GROUP_ID,
            COLUMN_NAME_ADMIN
    };

    public static final int COLUMN_ID = 0;
    public static final int COLUMN_MEMBER_ID = 1;
    public static final int COLUMN_USER_ID = 2;
    public static final int COLUMN_GROUP_ID = 3;
    public static final int COLUMN_ADMIN = 4;

    private int mMemberId;
    private int mUserId;
    private int mGroupId;
    private boolean mIsAdmin;

    public Member(JSONObject object) throws JSONException {
        mMemberId = object.getInt("id_member");
        mUserId = object.getInt("id_user");
        mGroupId = object.getInt("id_group");
        mIsAdmin = object.getString("admin").equals("1") ? true : false;
    }

    public Member(Cursor c) {
        mMemberId = c.getInt(COLUMN_MEMBER_ID);
        mUserId = c.getInt(COLUMN_USER_ID);
        mGroupId = c.getInt(COLUMN_GROUP_ID);
        mIsAdmin = c.getInt(COLUMN_ADMIN) == 1;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Member) {
            Member member = (Member) o;
            return (getMemberId() == member.getMemberId() &&
                    getUserId() == member.getUserId() &&
                    getGroupId() == member.getGroupId() &&
                    isAdmin() == member.isAdmin());
        }
        return false;
    }

    public int getGroupId() {
        return mGroupId;
    }

    public void setGroupId(int mGroupId) {
        this.mGroupId = mGroupId;
    }

    public int getMemberId() {
        return mMemberId;
    }

    public void setMemberId(int mMemberId) {
        this.mMemberId = mMemberId;
    }

    public int getUserId() {
        return mUserId;
    }

    public void setUserId(int mUserId) {
        this.mUserId = mUserId;
    }

    public boolean isAdmin() {
        return mIsAdmin;
    }

    public void setAdmin(boolean mIsAdmin) {
        this.mIsAdmin = mIsAdmin;
    }
}
