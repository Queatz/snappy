package com.queatz.snappy.chat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jacob on 9/17/17.
 */

public class DefaultChatRooms {
    public static List<ChatRoom> get() {
        List<ChatRoom> list = new ArrayList<>();

        list.add(new ChatRoom().setName("Community"));
        list.add(new ChatRoom().setName("Singles"));
        list.add(new ChatRoom().setName("Dating"));
        list.add(new ChatRoom().setName("Lunch"));
        list.add(new ChatRoom().setName("Gigs"));
        list.add(new ChatRoom().setName("Music"));
        list.add(new ChatRoom().setName("Art"));
        list.add(new ChatRoom().setName("Recruiting"));
        list.add(new ChatRoom().setName("Food"));
        list.add(new ChatRoom().setName("Tavern"));
        list.add(new ChatRoom().setName("News"));
        list.add(new ChatRoom().setName("Photography"));
        list.add(new ChatRoom().setName("Gaming"));
        list.add(new ChatRoom().setName("Tech"));
        list.add(new ChatRoom().setName("Trade"));
        list.add(new ChatRoom().setName("Events"));
        list.add(new ChatRoom().setName("Language"));
        list.add(new ChatRoom().setName("Parties"));
        list.add(new ChatRoom().setName("Classes"));
        list.add(new ChatRoom().setName("Ride Sharing"));
        list.add(new ChatRoom().setName("Housework"));
        list.add(new ChatRoom().setName("Collab"));
        list.add(new ChatRoom().setName("Roommates"));
        list.add(new ChatRoom().setName("Anime"));
        list.add(new ChatRoom().setName("Modeling"));
        list.add(new ChatRoom().setName("Film"));

        return list;
    }
}
