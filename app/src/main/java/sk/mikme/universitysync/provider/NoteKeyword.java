package sk.mikme.universitysync.provider;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by fic on 7.10.2014.
 */
public class NoteKeyword implements BaseColumns {
    public static final String PATH = "note_keywords";
    public static final String TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.universitysync.note_keywords";
    public static final String ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.universitysync.note_keyword";
    public static final Uri URI = Provider.BASE_URI.buildUpon().appendPath(PATH).build();
    public static final String TABLE_NAME = "note_keyword";
    public static final String COLUMN_NAME_NOTE_ID = "note_id";
    public static final String COLUMN_NAME_KEYWORD_ID = "keyword_id";
    public static final String[] PROJECTION = new String[] {
            _ID,
            COLUMN_NAME_NOTE_ID,
            COLUMN_NAME_KEYWORD_ID,
            Keyword.COLUMN_NAME_NAME
    };

    private static final int COLUMN_ID = 0;
    private static final int COLUMN_NOTE_ID = 1;
    private static final int COLUMN_KEYWORD_ID = 2;
    private static final int COLUMN_KEYWORD_NAME = 3;

    private int mId;
    private int mNoteId;
    private Keyword mKeyword;

    public NoteKeyword(Cursor c) {
        mId = c.getInt(COLUMN_ID);
        mNoteId = c.getInt(COLUMN_NOTE_ID);
        mKeyword = new Keyword(c.getInt(COLUMN_KEYWORD_ID), c.getString(COLUMN_KEYWORD_NAME));
    }

    public int getNoteId() {
        return mNoteId;
    }

    public void setNoteId(int mNoteId) {
        this.mNoteId = mNoteId;
    }

    public Keyword getKeyword() {
        return mKeyword;
    }

    public void setKeyword(Keyword mKeyword) {
        this.mKeyword = mKeyword;
    }
}
