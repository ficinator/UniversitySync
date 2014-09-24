package sk.mikme.universitysync.drawer;

import sk.mikme.universitysync.provider.User;

/**
 * Created by fic on 21.9.2014.
 */
public class DrawerUserItem extends DrawerItem {
    public DrawerUserItem(User user) {
        super(user.getUserId(), null, user.getFullName(), user.getUniversity(), 0);
    }
}
