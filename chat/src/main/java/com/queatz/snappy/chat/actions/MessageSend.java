package com.queatz.snappy.chat.actions;

import com.queatz.chat.ChatKind;
import com.queatz.chat.ChatWorld;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.chat.ChatEvent;
import com.queatz.snappy.chat.ChatSession;
import com.queatz.snappy.events.EarthUpdate;
import com.queatz.snappy.shared.chat.MessageSendChatMessage;

/**
 * Created by jacob on 8/9/17.
 */

public class MessageSend extends MessageSendChatMessage implements ChatMessage {

    @Override
    public void got(ChatSession chat) {
        ChatWorld world = chat.getChat().getWorld();

        EarthThing chatThing = world.add(world.stage(ChatKind.MESSAGE_KIND)
                .set(EarthField.GEO, chat.getLocation())
                .set(EarthField.MESSAGE, getMessage())
                .set(EarthField.IMAGE_URL, getAvatar())
                .set(EarthField.TOPIC, getTopic()));

        new EarthUpdate(new EarthAs())
                .send(new ChatEvent(getTopic()))
                .toLocation(chat.getLocation());

        chat.getChat().broadcast(chat, this);
    }
}
