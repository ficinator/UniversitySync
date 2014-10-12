package sk.mikme.universitysync.provider;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;

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

    public int getId() {
        return mId;
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

    public static int update(Context context, Note note)
            throws RemoteException, OperationApplicationException {
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        ContentResolver contentResolver = context.getContentResolver();
        ContentProviderResult[] result;
        Cursor c = contentResolver.query(
                NoteKeyword.URI.buildUpon()
                        .appendPath(Note.PATH)
                        .appendPath(Integer.toString(note.getId()))
                        .build(),
                NoteKeyword.PROJECTION, null, null, null);
        SparseArray<String> oldKeywords = new SparseArray<String>();
        while (c.moveToNext()) {
            NoteKeyword noteKeyword = new NoteKeyword(c);
            boolean hasKeyword = false;
            for (Keyword keyword : note.getKeywords()) {
                if (keyword.getId() == noteKeyword.getKeyword().getId()) {
                    oldKeywords.put(keyword.getId(), keyword.getName());
                    hasKeyword = true;
                    break;
                }
            }
            if (!hasKeyword) {
                batch.add(ContentProviderOperation.newDelete(
                        NoteKeyword.URI.buildUpon()
                            .appendPath(Integer.toString(noteKeyword.getId()))
                            .build())
                        .build());
            }
        }
        c.close();

        for (Keyword keyword : note.getKeywords()) {
            if (oldKeywords.get(keyword.getId()) == null) {
                batch.add(ContentProviderOperation.newInsert(NoteKeyword.URI)
                        .withValue(NoteKeyword.COLUMN_NAME_NOTE_ID, note.getId())
                        .withValue(NoteKeyword.COLUMN_NAME_KEYWORD_ID, keyword.getId())
                        .build());
            }
        }
        result = contentResolver.applyBatch(Provider.AUTHORITY, batch);
        contentResolver.notifyChange(NoteKeyword.URI, null, false);
        return result.length;
    }
}
