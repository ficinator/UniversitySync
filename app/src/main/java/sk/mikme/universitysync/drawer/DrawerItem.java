package sk.mikme.universitysync.drawer;

import android.graphics.drawable.Drawable;

/**
 * Created by fic on 21.9.2014.
 */
public abstract class DrawerItem {
    protected int mId;
    protected Drawable mIcon;
    protected String mTitle;
    protected String mSubTitle;
    protected int mCount;


    protected DrawerItem(int id, Drawable icon, String title, String subtitle, int count) {
        this.mId = id;
        this.mIcon = icon;
        this.mTitle = title;
        this.mSubTitle = subtitle;
        this.mCount = count;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getSubTitle() {
        return mSubTitle;
    }

    public void setSubTitle(String mSubTitle) {
        this.mSubTitle = mSubTitle;
    }

    public int getCount() {
        return mCount;
    }

    public void setCount(int mCount) {
        this.mCount = mCount;
    }

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public void setIcon(Drawable mThumb) {
        this.mIcon = mThumb;
    }
}
