package sk.mikme.universitysync.sync;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import sk.mikme.universitysync.provider.Note;

/**
 * Created by fic on 18.9.2014.
 */
public class DataParser {

    public static HashMap<String, Note> parseNotes(JSONObject jsonObject) throws JSONException {
        JSONArray noteMetaArray = jsonObject.getJSONArray(Note.PATH);
        HashMap<String, Note> notes = new HashMap<String, Note>();
        for (int i = 0; i < noteMetaArray.length(); i++) {
            Note note = new Note(noteMetaArray.getJSONObject(i));
            //JSONObject details = downloadUrl(new URL(SERVER_URL + note.getPath()));
            //if (details != null)
            //    note.setDetails(details);
            notes.put(Integer.toString(note.getNoteId()), note);
        }
        return notes;
    }
}
