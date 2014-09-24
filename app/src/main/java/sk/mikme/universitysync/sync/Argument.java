package sk.mikme.universitysync.sync;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.http.message.BasicNameValuePair;

/**
 * Created by fic on 21.9.2014.
 */
public class Argument extends BasicNameValuePair implements Parcelable {

    public Argument(String name, String value) {
        super(name, value);
    }

    @Override
    public String toString() {
        return getName() + "=" + getValue();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(getName());
        parcel.writeString(getValue());
    }
}
