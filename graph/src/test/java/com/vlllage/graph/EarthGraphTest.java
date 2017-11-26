package com.vlllage.graph;

import com.google.gson.JsonArray;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthQuery;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.shared.EarthJson;
import com.vlllage.graph.fields.AboutEarthGraphField;
import com.vlllage.graph.fields.AspectEarthGraphField;
import com.vlllage.graph.fields.ClubsEarthGraphField;
import com.vlllage.graph.fields.HiddenEarthGraphField;
import com.vlllage.graph.fields.IdEarthGraphField;
import com.vlllage.graph.fields.KindEarthGraphField;
import com.vlllage.graph.fields.MembersEarthGraphField;
import com.vlllage.graph.fields.NameEarthGraphField;
import com.vlllage.graph.fields.OwnerEarthGraphField;
import com.vlllage.graph.fields.PhotoEarthGraphField;
import com.vlllage.graph.fields.SourceEarthGraphField;
import com.vlllage.graph.fields.TargetEarthGraphField;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by jacob on 11/25/17.
 */
public class EarthGraphTest {

    @BeforeClass
    public void setup() {
        EarthGraph.register("id", new IdEarthGraphField());
        EarthGraph.register("kind", new KindEarthGraphField());
        EarthGraph.register("name", new NameEarthGraphField());
        EarthGraph.register("about", new AboutEarthGraphField());
        EarthGraph.register("members", new MembersEarthGraphField());
        EarthGraph.register("source", new SourceEarthGraphField());
        EarthGraph.register("target", new TargetEarthGraphField());
        EarthGraph.register("owner", new OwnerEarthGraphField());
        EarthGraph.register("clubs", new ClubsEarthGraphField());
        EarthGraph.register("photo", new PhotoEarthGraphField());
        EarthGraph.register("aspect", new AspectEarthGraphField());
        EarthGraph.register("hidden", new HiddenEarthGraphField());
    }

    @Test
    public void testQuery() throws Exception {
        EarthAs as = new EarthAs();
        EarthGraph earthGraph = new EarthGraph(as);

        JsonArray r = earthGraph.query(
                as.s((EarthQuery.class)).filter(EarthField.KIND, "'" + EarthKind.PERSON_KIND + "'"),
                "name,about,members,source,target,owner,photo,aspect,hidden,clubs"
        );

        assertEquals(as.s(EarthJson.class).toJson(r), "[{\"id\":\"121680\",\"kind\":\"person\",\"name\":null,\"about\":\"\",\"members\":[]},{\"id\":\"3272\",\"kind\":\"person\",\"name\":null,\"about\":\"\\\"Calligraphy we\\u0027ll do later.\\\" - Judy Yeo\",\"members\":[{\"id\":\"136727\",\"kind\":\"member\",\"source\":{\"id\":\"136708\",\"kind\":\"member\",\"name\":null,\"about\":null}},{\"id\":\"136708\",\"kind\":\"member\",\"source\":{\"id\":\"136538\",\"kind\":\"hub\",\"name\":\"Secret Gazebo\",\"about\":\"\"}}]}]");
    }
}
