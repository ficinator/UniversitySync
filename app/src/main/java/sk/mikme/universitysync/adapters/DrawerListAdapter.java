package sk.mikme.universitysync.adapters;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import sk.mikme.universitysync.R;
import sk.mikme.universitysync.drawer.DrawerGroupItem;
import sk.mikme.universitysync.drawer.DrawerItem;
import sk.mikme.universitysync.drawer.DrawerTitleItem;
import sk.mikme.universitysync.drawer.DrawerUserItem;

/**
 * Created by fic on 21.9.2014.
 */
public class DrawerListAdapter extends ArrayAdapter<DrawerItem> {

    public DrawerListAdapter(Context context, List<DrawerItem> drawerItems) {
        super(context, R.layout.item_user_drawer, drawerItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DrawerItem item = getItem(position);
        LayoutInflater inflater = (LayoutInflater) getContext().
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (item instanceof DrawerTitleItem)
            convertView = inflater.inflate(R.layout.item_title_drawer, null);
        else
            convertView = inflater.inflate(R.layout.item_user_drawer, null);

        TextView titleView = (TextView) convertView.findViewById(R.id.title);
        titleView.setText(item.getTitle());

        if (!(item instanceof  DrawerTitleItem)) {
            TextView subtitleView = (TextView) convertView.findViewById(R.id.subtitle);
            subtitleView.setText(item.getSubTitle());
        }

        return convertView;
    }
}
