package com.queatz.snappy.chat;

import com.google.common.primitives.Bytes;
import com.image.SnappyImage;
import com.queatz.chat.ChatKind;
import com.queatz.chat.ChatWorld;
import com.queatz.snappy.chat.actions.ChatMessage;
import com.queatz.snappy.chat.actions.MessageSend;
import com.queatz.snappy.as.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.snappy.shared.EarthJson;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.logic.EarthUpdate;
import com.queatz.snappy.logic.eventables.ChatEvent;
import com.queatz.snappy.shared.Shared;
import com.queatz.snappy.shared.chat.BasicChatMessage;
import com.queatz.snappy.shared.earth.EarthGeo;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.websocket.Session;

/**
 * Created by jacob on 8/9/17.
 */

public class ChatSession {

    private Session session;
    private ChatLogic chat;
    private EarthJson json;
    private EarthGeo location;

    public ChatSession(Session session, ChatLogic chat) {
        this.session = session;
        this.chat = chat;
        this.json = new EarthJson();
        this.location = new EarthGeo();
    }

    public void join() {
        chat.join(this);
    }

    public void leave() {
        chat.leave(this);
    }

    public void got(String message) {
        ChatMessage chatMessage = ChatMessageConverter.convert(json.fromJson(message, BasicChatMessage.class));

        if (chatMessage == null) {
            Logger.getAnonymousLogger().warning("CHAT INVALID ACTION: " + message);
            return;
        }

        chatMessage.got(this);
    }

    public void got(byte[] data) {
        int idx = Bytes.indexOf(data, (byte) 0);

        if (idx < 0) {
            return;
        }

        String string = new String(Arrays.copyOfRange(data, 0, idx), Charset.forName("UTF-8"));
        MessageSend chatMessage = json.fromJson(string, MessageSend.class);

        String topic = chatMessage.getTopic();
        String avatar = chatMessage.getAvatar();


        SnappyImage snappyImage = new SnappyImage();
        String name = "chat/" + Shared.randomToken();
        OutputStream outputChannel = snappyImage.openOutputStream(name, null);

        if (outputChannel == null) {
            return;
        }

        try {
            // Skip null-byte and write rest of bytes to image
            outputChannel.write(data, idx + 1, data.length - (idx + 1));
            outputChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ChatWorld world = chat.getWorld();

        EarthThing chatThing = world.add(world.stage(ChatKind.MESSAGE_KIND)
                .set(EarthField.GEO, getLocation())
                .set(EarthField.PHOTO, snappyImage.getServingUrl(name, 600))
                .set(EarthField.IMAGE_URL, avatar)
                .set(EarthField.TOPIC, topic));

        new EarthUpdate(new EarthAs())
                .send(new ChatEvent(topic))
                .toLocation(getLocation());

        MessageSend send = (MessageSend) new MessageSend()
                .setTopic(topic)
                .setAvatar(avatar)
                .setPhoto(snappyImage.getServingUrl(name, 600));

        chat.broadcast(this, send);

        send(send);
    }

    public Session getSession() {
        return session;
    }

    public ChatLogic getChat() {
        return chat;
    }

    public void send(ChatMessage message) {
        try {
            session.getBasicRemote().sendText(json.toJson(ChatMessageConverter.convert(message)));
        } catch (IOException e) {
            e.printStackTrace();
            Logger.getAnonymousLogger().warning("CHAT SEND ERROR: " + e.getMessage());
        }
    }

    public void setLocation(EarthGeo location) {
        this.location = location;
    }

    public EarthGeo getLocation() {
        return location;
    }
}
