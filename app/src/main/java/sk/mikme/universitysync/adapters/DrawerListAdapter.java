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
import sk.mikme.universitysync.drawer.DrawerItem;
import sk.mikme.universitysync.drawer.DrawerUserItem;

/**
 * Created by fic on 21.9.2014.
 */
public class DrawerListAdapter extends ArrayAdapter<DrawerItem> {
    private static int DRAWER_ITEM_LAYOUT = R.layout.item_drawer;

    public DrawerListAdapter(Context context, List<DrawerItem> drawerItems) {
        super(context, DRAWER_ITEM_LAYOUT, drawerItems);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(DRAWER_ITEM_LAYOUT, null);
        }
        DrawerItem item = getItem(position);

        ImageView thumbView = (ImageView) convertView.findViewById(R.id.thumb);
        TextView titleView = (TextView) convertView.findViewById(R.id.title);
        TextView subtitleView = (TextView) convertView.findViewById(R.id.subtitle);

        titleView.setText(item.getTitle());
        subtitleView.setText(item.getSubTitle());

//        if (item instanceof DrawerCategoryItem) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
//                titleView.setAllCaps(true);
//            thumbView.setVisibility(View.GONE);
//            subtitleView.setVisibility(View.GONE);
//        }
        if (item instanceof DrawerUserItem) {
            //thumbView.setImageDrawable(item.getIcon());
            thumbView.setVisibility(View.VISIBLE);
            subtitleView.setVisibility(View.VISIBLE);
        }

        return convertView;
    }
}
