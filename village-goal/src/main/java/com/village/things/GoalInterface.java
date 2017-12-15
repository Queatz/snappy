package com.village.things;

import com.google.gson.JsonArray;
import com.image.SnappyImage;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.api.ApiUtil;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.exceptions.NothingLogicResponse;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.EarthJson;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

public class GoalInterface extends CommonThingInterface {

    @Override
    public EarthThing createThing(EarthAs as) {
        as.requireUser();

        String name = extract(as.getParameters().get(EarthField.NAME));

        if (name == null) {
            throw new NothingLogicResponse("goal - name parameter is expected");
        }

        EarthThing goal = as.s(GoalEditor.class).newGoal(name);

        return goal;
    }

    @Override
    public String postThing(EarthAs as, EarthThing thing) {

        switch (as.getRoute().size()) {
            case 2:
                if (Config.PATH_COMPLETE.equals(as.getRoute().get(1))) {
                    return completeGoal(as, thing);
                }
        }

        return null;
    }

    private String completeGoal(EarthAs as, EarthThing goal) {

        // Create new completed_goal update
        EarthThing update = as.s(UpdateEditor.class).newUpdate(as.getUser(), Config.UPDATE_ACTION_COMPLETED_GOAL, goal);

        update = updateFromRequest(as, update);

        // Create new member (update -> goal)
        as.s(MemberEditor.class).create(update, goal, Config.MEMBER_STATUS_ACTIVE);
        as.s(MemberEditor.class).create(update, as.getUser(), Config.MEMBER_STATUS_ACTIVE);

        // Remove all members (goal -> me)
        EarthThing member = as.s(MemberMine.class).byThingToThing(goal, as.getUser());

        if (member != null) {
            as.s(EarthStore.class).conclude(member);
        }

        return returnIfGraph(as, update);
    }

    private EarthThing updateFromRequest(EarthAs as, EarthThing update) {
        String message = null;
        boolean photoUploaded = false;

        JsonArray with = null;

        try {
            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator iterator = upload.getItemIterator(as.getRequest());
            while (iterator.hasNext()) {
                FileItemStream item = iterator.next();
                InputStream stream = item.openStream();

                if (!item.isFormField() && Config.PARAM_PHOTO.equals(item.getFieldName())) {
                    ApiUtil.putPhoto(update.key().name(), item.getName(), as.s(SnappyImage.class), item);
                    photoUploaded = true;
                }
                else if (Config.PARAM_MESSAGE.equals(item.getFieldName())) {
                    message = Streams.asString(stream, "UTF-8");
                }
                else if (Config.PARAM_WITH.equals(item.getFieldName())) {
                    with = as.s(EarthJson.class).fromJson(Streams.asString(stream, "UTF-8"), JsonArray.class);
                }

                stream.close();
            }
        }
        catch (FileUploadException | IOException e) {
            as.s(EarthStore.class).conclude(update);
            Logger.getLogger(Config.NAME).severe(e.toString());
            throw new NothingLogicResponse("post photo - couldn't upload because: " + e);
        }

        // XXX TODO Notify goal completed to goal followers
        // XXX This should probably be done by auto-backing goals you follow

        return as.s(UpdateEditor.class).updateWith(
                update,
                message,
                photoUploaded,
                with
        );
    }

    @Override
    public EarthThing editThing(EarthAs as, EarthThing goal) {
        if (Boolean.toString(true).equals(extract(as.getParameters().get(Config.PARAM_JOIN)))) {
            return join(as, goal);
        } else if (Boolean.toString(false).equals(extract(as.getParameters().get(Config.PARAM_JOIN)))) {
            return leave(as, goal);
        }

        throw new NothingLogicResponse("goal - immutable");
    }

    private EarthThing join(EarthAs as, EarthThing goal) {
        as.requireUser();

        return as.s(JoinEditor.class).newJoin(as.getUser(), goal);
    }

    private EarthThing leave(EarthAs as, EarthThing goal) {
        as.requireUser();

        EarthThing join = as.s(JoinMine.class).byPersonAndParty(as.getUser(), goal);

        return as.s(JoinEditor.class).setStatus(join, Config.JOIN_STATUS_WITHDRAWN);
    }
}
