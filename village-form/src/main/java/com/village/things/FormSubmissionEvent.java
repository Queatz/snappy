package com.village.things;

import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.queatz.snappy.as.EarthAs;
import com.queatz.earth.EarthField;
import com.queatz.snappy.shared.EarthJson;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthStore;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.events.Eventable;
import com.queatz.snappy.shared.Config;

/**
 * Created by jacob on 6/4/17.
 */

public class FormSubmissionEvent implements Eventable {
    EarthStore earthStore = new EarthStore(new EarthAs());

    EarthThing thing;

    // Serialization

    public FormSubmissionEvent() {}

    public FormSubmissionEvent fromData(String data) {
        thing = earthStore.get(data);
        return this;
    }

    public String toData() {
        return thing.key().name();
    }

    // End Serialization

    public FormSubmissionEvent(EarthThing thing) {
        this.thing = thing;
    }

    @Override
    public Object makePush() {
        return null;
    }

    @Override
    public String makeSubject() {
        EarthThing form = earthStore.get(thing.getKey(EarthField.TARGET));
        return "(Form Submission) " + form.getString(EarthField.NAME) + " #" + earthStore.count(EarthKind.MEMBER_KIND, EarthField.TARGET, form.key());
    }

    @Override
    public String makeEmail() {
        EarthThing form = earthStore.get(thing.getKey(EarthField.TARGET));

        String thingUrl = Config.VILLAGE_WEBSITE + form.getString(EarthField.KIND) + "s/" + form.key().name();

        return "<b>" + form.getString(EarthField.NAME) + "</b>" + answers() +
                "<br /><br /><span style=\"color: #757575;\">" +
                "View or edit your form at " + thingUrl;
    }

    private String answers() {
        StringBuilder builder = new StringBuilder();

        EarthJson json = new EarthJson();
        JsonArray jsonArray = json.fromJson(thing.getString(EarthField.DATA), JsonArray.class);
        Escaper escaper = HtmlEscapers.htmlEscaper();

        if (jsonArray != null) {
            for (JsonElement jsonElement : jsonArray) {
                if (!jsonElement.isJsonObject()) {
                    continue;
                }

                JsonObject obj = ((JsonObject) jsonElement);

                String type = obj.get("type").getAsString();

                if (type == null) {
                    continue;
                }

                switch (type) {
                    case "text":
                        builder.append("<br />");
                        builder.append(text());
                        builder.append(escaper.escape(obj.get("about").getAsString()));
                        break;
                    case "paragraph":
                        builder.append("<br />");
                        builder.append(question());
                        builder.append(escaper.escape(obj.get("name").getAsString()));
                        builder.append("<br />");
                        builder.append(answer());
                        if (obj.has("answer")) {
                            builder.append(escaper.escape(obj.get("answer").getAsString()));
                        } else {
                            builder.append(noAnswer());
                        }
                        break;
                    case "file":
                        builder.append("<br />");
                        builder.append(question());
                        builder.append(escaper.escape(obj.get("name").getAsString()));
                        builder.append("<br />");
                        builder.append(answer());
                        if (obj.has("answer")) {
                            builder.append(Config.BASE_URL + obj.get("answer").getAsString());
                        } else {
                            builder.append(noAnswer());
                        }
                        break;
                    case "photo":
                        builder.append("<br />");
                        builder.append(question());
                        builder.append(escaper.escape(obj.get("name").getAsString()));
                        builder.append("<br />");
                        builder.append(answer());

                        if (obj.has("answer")) {
                            String url = Config.BASE_URL + obj.get("answer").getAsString();
                            builder.append("<img style=\"border-radius: 4px;\" src=\"").append(url).append("\" /><br />");
                            builder.append(url);
                        } else {
                            builder.append(noAnswer());
                        }
                        break;
                    case "date":
                        builder.append("<br /><br />Date");

                        break;
                    case "checkbox":
                        builder.append("<br />");
                        builder.append(question());
                        builder.append(escaper.escape(obj.get("about").getAsString()));
                        builder.append("<br />");
                        builder.append(answer());
                        if (obj.has("answer")) {
                            builder.append(escaper.escape(obj.get("answer").getAsString()));
                        } else {
                            builder.append(noAnswer());
                        }

                        break;
                    case "choice":
                        builder.append("<br /><br />");
                        builder.append(question());
                        builder.append(escaper.escape(obj.get("name").getAsString()));

                        builder.append("<br /><br />");

                        if (obj.get("choices").isJsonArray()) {
                            JsonArray choices = obj.getAsJsonArray("choices");

                            for (int i = 0; i < choices.size(); i++) {
                                builder.append((i + 1) + ") " + choices.get(i).getAsJsonObject().get("about").getAsString());
                                builder.append("<br />");
                            }

                            builder.append(answer());
                            if (obj.has("answer")) {
                                builder.append(choices.get(Integer.valueOf(obj.get("answer").getAsString())).getAsJsonObject().get("about").getAsString());
                            } else {
                                builder.append(noAnswer());
                            }
                        } else {
                            builder.append("problem...");
                        }


                        break;
                }
            }
        }

        return builder.toString();
    }

    private String question() {
        return "<br /><span style=\"color: rgb(156, 39, 176); font-weight: bold; font-size: 80%;\">QUESTION</span><br />";
    }

    private String answer() {
        return "<br /><span style=\"color: #26a69a; font-weight: bold; font-size: 80%;\">ANSWER</span><br />";
    }

    private String text() {
        return "<br /><span style=\"color: #757575; font-weight: bold; font-size: 80%;\">TEXT</span><br />";
    }

    private String noAnswer() {
        return "<span style=\"color: #757575;\">No answer provided.</span>";
    }

    @Override
    public int emailDelay() {
        return 0;
    }
}
