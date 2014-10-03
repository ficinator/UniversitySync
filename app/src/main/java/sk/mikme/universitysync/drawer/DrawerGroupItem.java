package sk.mikme.universitysync.drawer;

import sk.mikme.universitysync.provider.Group;

/**
 * Created by fic on 3.10.2014.
 */
public class DrawerGroupItem extends DrawerItem {
    public DrawerGroupItem(Group group) {
        super(group.getGroupId(), null, group.getName(), group.getUniversity(), 0);
    }
}
