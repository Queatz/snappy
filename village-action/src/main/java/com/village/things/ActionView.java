package com.village.things;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthThing;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.shared.EarthJson;
import com.queatz.snappy.view.EarthView;

/**
 * Created by jacob on 10/11/17.
 */

public class ActionView extends LinkView {

    private final String role;
    private final JsonElement data;
    private final String token;
    private final String value;
    private final String type;

    public ActionView(EarthAs as, EarthThing action) {
        this(as, action, EarthView.DEEP);
    }

    public ActionView(EarthAs as, EarthThing action, EarthView view) {
        super(as, action, view);

        this.role = action.getString(EarthField.ROLE);
        this.value = action.getString(EarthField.MESSAGE);
        this.type = action.getString(EarthField.TYPE);

        boolean owner = as.hasUser() && action.getString(EarthField.SOURCE).equals(as.getUser().key().name());

        // Only the action's owner gets to see the full action configuration
        if (owner) {
            this.token = action.getString(EarthField.TOKEN);
            this.data = use(EarthJson.class).fromJson(action.getString(EarthField.DATA), JsonElement.class);
        } else {
            this.token = null;
            JsonObject data = use(EarthJson.class).fromJson(action.getString(EarthField.DATA), JsonObject.class);
            JsonObject json = new JsonObject();
            json.add(ActionConfig.DATA_FIELD_CONFIG, data.get(ActionConfig.DATA_FIELD_CONFIG));
            this.data = json;
        }
    }
}
