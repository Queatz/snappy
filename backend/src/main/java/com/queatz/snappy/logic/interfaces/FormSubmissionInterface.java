package com.queatz.snappy.logic.interfaces;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.image.SnappyImage;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.api.ApiUtil;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.router.Interfaceable;
import com.queatz.snappy.exceptions.NothingLogicResponse;
import com.queatz.snappy.files.SnappyFiles;
import com.queatz.snappy.events.EarthUpdate;
import com.queatz.snappy.view.EarthViewer;
import com.queatz.snappy.logic.editors.FormSubmissionEditor;
import com.village.things.MemberEditor;
import com.queatz.snappy.logic.eventables.FormSubmissionEvent;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.shared.EarthJson;
import com.queatz.snappy.shared.Shared;
import com.queatz.snappy.view.SuccessView;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by jacob on 6/4/17.
 */

public class FormSubmissionInterface implements Interfaceable {

    @Override
    public String get(EarthAs as) {
        switch (as.getRoute().size()) {
            case 0:
                throw new NothingLogicResponse("form-submission - empty route");
            case 1:
                EarthThing thing = new EarthStore(as).get(as.getRoute().get(0));

                return new EarthViewer(as).getViewForEntityOrThrow(thing).toJson();
            default:
                throw new NothingLogicResponse("form-submission - bad path");
        }
    }

    @Override
    public String post(EarthAs as) {
        EarthStore earthStore = new EarthStore(as);

        switch (as.getRoute().size()) {
            case 0: {
                return create(as);
            }
        }

        throw new NothingLogicResponse("form-submission - bad path");
    }

    public String create(EarthAs as) {
        String thingId = null, data = null;
        Map<String, String> photos = new HashMap<>();
        Map<String, String> files = new HashMap<>();

        try {
            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator iterator = upload.getItemIterator(as.getRequest());
            while (iterator.hasNext()) {
                FileItemStream item = iterator.next();
                InputStream stream = item.openStream();

                if (!item.isFormField() && item.getFieldName().startsWith(Config.PARAM_PHOTO + "---")) {
                    String answerId = item.getFieldName().split("---", 2)[1];
                    String photoName = "form-submission-" + Shared.randomToken() + Shared.randomToken();
                    ApiUtil.putPhotoRaw(photoName, item.getName(), as.s(SnappyImage.class), item);
                    photos.put(answerId, photoName);
                } else if (!item.isFormField() && item.getFieldName().startsWith(Config.PARAM_FILE + "---")) {
                    String answerId = item.getFieldName().split("---", 2)[1];
                    String fileName = ApiUtil.putFile(as.s(SnappyFiles.class), item);
                    files.put(answerId, fileName);
                } else if (Config.PARAM_DATA.equals(item.getFieldName())) {
                    data = Streams.asString(stream, "UTF-8");
                } else if (Config.PARAM_IN.equals(item.getFieldName())) {
                    thingId = Streams.asString(stream, "UTF-8");
                }
            }
        }
        catch (FileUploadException | IOException e) {
            Logger.getLogger(Config.NAME).severe(e.toString());
            throw new NothingLogicResponse("form-submission - couldn't upload because: " + e);
        }

        if (!files.isEmpty() || !photos.isEmpty()) {
            Logger.getAnonymousLogger().warning("(FORM SUBMISSION) FILES!");

            EarthJson json = new EarthJson();
            JsonArray jsonArray = json.fromJson(data, JsonArray.class);
            for (JsonElement jsonElement : jsonArray) {
                if (!jsonElement.isJsonObject()) {
                    Logger.getAnonymousLogger().warning("(FORM SUBMISSION) Not JsonObject!");
                    continue;
                }

                JsonObject jsonObject = (JsonObject) jsonElement;

                if (jsonObject.has("__id") && jsonObject.has("type")) {
                    Logger.getAnonymousLogger().warning("(FORM SUBMISSION) has prereqs");
                    String id = jsonObject.get("__id").getAsString();

                    switch (jsonObject.get("type").getAsString()) {
                        case "photo":
                            Logger.getAnonymousLogger().warning("(FORM SUBMISSION) PHOTO!");
                            if (photos.containsKey(id)) {
                                jsonObject.addProperty("answer", as.s(SnappyImage.class).getServingUrl(photos.get(id), 800));
                                Logger.getAnonymousLogger().warning("(FORM SUBMISSION) added answer: " + jsonObject.get("answer"));
                            }
                            break;
                        case "file":
                            Logger.getAnonymousLogger().warning("(FORM SUBMISSION) FILE!");
                            if (files.containsKey(id)) {
                                jsonObject.addProperty("answer", ApiUtil.fileUrl(files.get(id)));
                                Logger.getAnonymousLogger().warning("(FORM SUBMISSION) added answer: " + jsonObject.get("answer"));
                            }
                            break;
                        default:
                            Logger.getAnonymousLogger().warning("(FORM SUBMISSION) skipping " + jsonObject.get("type").getAsString());
                    }
                }
            }

            data = json.toJson(jsonArray);
        }

        if (data == null) {
            throw new NothingLogicResponse("form-submission - no data");
        }

        if (thingId == null) {
            throw new NothingLogicResponse("form-submission - no thing");
        }

        EarthStore earthStore = new EarthStore(as);
        EarthThing form = earthStore.get(thingId);

        if (form == null) {
            throw new NothingLogicResponse("form-submission - null form");
        }

        EarthThing formSubmission = new FormSubmissionEditor(new EarthAs()).newFormSubmission(form, data);

        EarthThing formOwner = earthStore.get(form.getString(EarthField.SOURCE));
        earthStore.addToClub(formSubmission, formOwner);
        earthStore.setOwner(formSubmission, formOwner);

        new MemberEditor(new EarthAs()).create(formSubmission, form, Config.MEMBER_STATUS_ACTIVE);
        new EarthUpdate(new EarthAs()).send(new FormSubmissionEvent(formSubmission)).to(formOwner);

        return new SuccessView(true).toJson();
    }
}
