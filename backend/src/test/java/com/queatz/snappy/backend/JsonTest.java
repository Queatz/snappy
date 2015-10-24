package com.queatz.snappy.backend;

import com.google.appengine.api.datastore.GeoPt;
import com.queatz.snappy.shared.Deep;
import com.queatz.snappy.shared.Hide;
import com.queatz.snappy.shared.Push;
import com.queatz.snappy.shared.Shallow;

import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Created by jacob on 10/21/15.
 */
public class JsonTest {

    class JsonShallowTestObject {
        public @Push String name;
        public JsonShallowTestObject normal;
        public @Shallow JsonShallowTestObject shallow;
        public @Deep JsonShallowTestObject deep;
        public @Deep List<JsonShallowTestObject> deepList;
        public @Shallow List<JsonShallowTestObject> shallowList;
        public @Push JsonShallowTestObject push;
        public @Hide GeoPt latlng;
        public @Push float latitude;
        public @Push float longitude;

        public JsonShallowTestObject() {}

        public JsonShallowTestObject(String name) {
            this.name = name;
        }
    }

    @Test
    public void push() {
        JsonShallowTestObject result = new JsonShallowTestObject("base");
        result.push = new JsonShallowTestObject("push");
        result.push.latlng = new GeoPt(5, 10);
        result.push.push = new JsonShallowTestObject("push");
        result.push.push.latlng = new GeoPt(15, 20);

        result = transform(result, Json.Compression.PUSH);

        assertNotNull(result.push);
        assertNotNull(result.push.name);
        assertEquals(result.push.latitude, 5f);
        assertEquals(result.push.longitude, 10f);
        assertNotNull(result.push.push);
        assertNotNull(result.push.push.name);
        assertEquals(result.push.push.latitude, 15f);
        assertEquals(result.push.push.longitude, 20f);
    }

    @Test
    public void testJsonDeep() {
        JsonShallowTestObject result = new JsonShallowTestObject("base");
        result.deep = new JsonShallowTestObject("deep");
        result.deep.deep = new JsonShallowTestObject("deep.deep");
        result.deep.shallow = new JsonShallowTestObject("deep.shallow");
        result.deep.deep.shallow = new JsonShallowTestObject("deep.deep.shallow");

        result = transform(result, Json.Compression.NONE);

        assertNotNull(result.deep);
        assertNotNull(result.deep.deep);
        assertNotNull(result.deep.shallow);
        assertNotNull(result.deep.deep.shallow);
    }

    @Test
    public void testJsonDeepList() {
        JsonShallowTestObject result = new JsonShallowTestObject("base");
        JsonShallowTestObject deepList = new JsonShallowTestObject("deepList");
        result.deepList = Collections.singletonList(deepList);
        deepList.shallow = new JsonShallowTestObject("deepList.shallow");

        result = transform(result, Json.Compression.NONE);

        assertNotNull(result.deepList);
        assertEquals(result.deepList.size(), 1);
        assertNotNull(result.deepList.get(0));
        assertEquals(result.deepList.get(0).name, "deepList");
        assertNotNull(result.deepList.get(0).shallow);
    }

    @Test
    public void testJsonShallowListDeepMap() {
        JsonShallowTestObject result = new JsonShallowTestObject("base");

        JsonShallowTestObject deepList = new JsonShallowTestObject("deepList");
        deepList.shallow = new JsonShallowTestObject("deepList.shallow");

        JsonShallowTestObject shallowList = new JsonShallowTestObject("deepList.shallowList");
        shallowList.normal = new JsonShallowTestObject("deepList.shallowList.normal");

        deepList.shallowList = Collections.singletonList(shallowList);
        deepList.normal = new JsonShallowTestObject("deepList.normal");
        result.deepList = Collections.singletonList(deepList);

        result = transform(result, Json.Compression.NONE);

        assertNotNull(result.deepList);
        assertNotNull(result.deepList.get(0));
        assertNotNull(result.deepList.get(0).shallowList);
        assertNotNull(result.deepList.get(0).shallowList.get(0));
        assertNotNull(result.deepList.get(0).shallowList.get(0).normal);
    }

    @Test
    public void testJsonShallow() {
        JsonShallowTestObject result = new JsonShallowTestObject("base");

        result.normal = new JsonShallowTestObject("normal");
        result.normal.shallow = new JsonShallowTestObject("normal.shallow");
        result.normal.normal = new JsonShallowTestObject("normal.normal");

        result.shallow = new JsonShallowTestObject("shallow");
        result.shallow.shallow = new JsonShallowTestObject("shallow.shallow");
        result.shallow.normal = new JsonShallowTestObject("shallow.normal");

        result = transform(result, Json.Compression.NONE);

        assertNotNull(result.name);
        assertNotNull(result.shallow);
        assertNotNull(result.normal);

        assertNotNull(result.normal.normal);
        assertNotNull(result.shallow.normal);

        assertNull(result.normal.shallow);
        assertNull(result.shallow.shallow);

        assertNotNull(result.shallow.name);
        assertNotNull(result.shallow.normal.name);
        assertNotNull(result.normal.name);
        assertNotNull(result.normal.normal.name);
    }

    private JsonShallowTestObject transform(JsonShallowTestObject jsonShallowTestObject, Json.Compression compression) {
        String json = Json.json(jsonShallowTestObject, compression);
        return Json.from(json, JsonShallowTestObject.class);
    }
}