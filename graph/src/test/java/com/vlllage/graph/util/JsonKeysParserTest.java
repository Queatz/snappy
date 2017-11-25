package com.vlllage.graph.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.text.ParseException;

import static org.testng.Assert.assertEquals;

/**
 * Created by jacob on 11/25/17.
 */
public class JsonKeysParserTest {

    private JsonKeysParser jsonKeysParser;
    private Gson gson;

    @BeforeClass
    public void setup() {
        jsonKeysParser = new JsonKeysParser('(', ')', ',');
        gson = new Gson();
    }

    @Test
    public void testParseEmptyString() throws Exception {
        JsonArray result = jsonKeysParser.parse("");

        assertEquals(result.size(), 0);
    }

    @Test
    public void testParseSingleString() throws Exception {
        JsonArray result = jsonKeysParser.parse("one");

        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getAsString(), "one");
    }

    @Test
    public void testParseTwoStrings() throws Exception {
        JsonArray result = jsonKeysParser.parse("one,two");

        assertEquals(result.size(), 2);
        assertEquals(result.get(0).getAsString(), "one");
        assertEquals(result.get(1).getAsString(), "two");
    }

    @Test
    public void testParseThreeStrings() throws Exception {
        JsonArray result = jsonKeysParser.parse("one,two,three");

        assertEquals(result.size(), 3);
        assertEquals(result.get(0).getAsString(), "one");
        assertEquals(result.get(1).getAsString(), "two");
        assertEquals(result.get(2).getAsString(), "three");
    }

    @Test
    public void testParseStringOneNestedOne() throws Exception {
        expectJson(
                jsonKeysParser.parse("one(two)"),
                "['one', ['two']]"
        );
    }

    @Test
    public void testParseStringOneNestedTwo() throws Exception {
        expectJson(
                jsonKeysParser.parse("one(two,three)"),
                "['one', ['two', 'three']]"
        );
    }

    @Test
    public void testParseStringTwoNestedTwo() throws Exception {
        expectJson(
                jsonKeysParser.parse("one,two(three,four)"),
                "['one', 'two', ['three', 'four']]"
        );
    }

    @Test
    public void testParseComplexOne() throws Exception {
        expectJson(
                jsonKeysParser.parse("one(one),two(one,two),three(one(one(one),two)),four"),
                "['one', ['one'], 'two', ['one', 'two'], 'three', ['one', ['one', ['one'], 'two']], 'four']"
        );
    }

    @Test
    public void testParseComplexTwo() throws Exception {
        expectJson(
                jsonKeysParser.parse("one(one),two(one,two),three(one(one(one),two)),four(one)"),
                "['one', ['one'], 'two', ['one', 'two'], 'three', ['one', ['one', ['one'], 'two']], 'four', ['one']]"
        );
    }

    @Test(expectedExceptions = ParseException.class)
    public void testParseInvalidOne() throws Exception {
        jsonKeysParser.parse("one(one)),two");
    }

    @Test(expectedExceptions = ParseException.class)
    public void testParseInvalidTwo() throws Exception {
        jsonKeysParser.parse("one((one),two");
    }

    private void expectJson(JsonArray actual, String expected) {
        assertEquals(actual.toString(), gson.fromJson(expected, JsonArray.class).toString());
    }
}