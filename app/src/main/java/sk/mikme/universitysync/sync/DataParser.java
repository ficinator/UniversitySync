package sk.mikme.universitysync.sync;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import sk.mikme.universitysync.provider.Group;
import sk.mikme.universitysync.provider.Member;
import sk.mikme.universitysync.provider.Note;
import sk.mikme.universitysync.provider.User;

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

    public static HashMap<String, Group> parseGroups(JSONObject jsonObject) throws JSONException {
        JSONArray groupArray = jsonObject.getJSONArray(Group.PATH);
        HashMap<String, Group> groups = new HashMap<String, Group>();
        for (int i = 0; i < groupArray.length(); i++) {
            Group group = new Group(groupArray.getJSONObject(i));
            groups.put(Integer.toString(group.getGroupId()), group);
        }
        return groups;
    }

    public static HashMap<String, User> parseUsers(JSONObject jsonObject) throws JSONException {
        JSONArray userArray = jsonObject.getJSONArray(User.PATH);
        HashMap<String, User> users = new HashMap<String, User>();
        for (int i = 0; i < userArray.length(); i++) {
            User user = new User(userArray.getJSONObject(i));
            users.put(Integer.toString(user.getUserId()), user);
        }
        return users;
    }

    public static HashMap<String, Member> parseMembers(JSONObject jsonObject) throws JSONException {
        JSONArray memberArray = jsonObject.getJSONArray(Group.PATH);
        HashMap<String, Member> members = new HashMap<String, Member>();
        for (int i = 0; i < memberArray.length(); i++) {
            Member member = new Member(memberArray.getJSONObject(i));
            members.put(Integer.toString(member.getMemberId()), member);
        }
        return members;
    }
}
