package com.queatz.snappy.chat.actions;

import com.queatz.chat.ChatKind;
import com.queatz.chat.ChatWorld;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.chat.ChatSession;
import com.queatz.snappy.shared.chat.AdAddChatMessage;
import com.village.things.PersonMine;

/**
 * Created by jacob on 8/9/17.
 */

public class AdAdd extends AdAddChatMessage implements ChatMessage {

    @Override
    public void got(ChatSession chat) {
        ChatWorld world = chat.getChat().getWorld();

        EarthThing source = new PersonMine(new EarthAs()).byToken(getToken());

        // Must have valid account to post ads
        if (source == null) {
            return;
        }

        world.add(world.stage(ChatKind.AD_KIND)
                .set(EarthField.GEO, chat.getLocation())
                .set(EarthField.SOURCE, source.key().name())
                .set(EarthField.NAME, getName())
                .set(EarthField.ABOUT, getDescription())
                .set(EarthField.TOPIC, getTopic()));

        chat.getChat().broadcast(chat, this);
    }
}
