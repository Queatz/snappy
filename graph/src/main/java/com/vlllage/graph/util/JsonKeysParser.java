package com.vlllage.graph.util;

import com.google.gson.JsonArray;

import java.text.ParseException;
import java.util.Stack;

/**
 * Created by jacob on 11/25/17.
 */

/**
 * @description
 * Converts JSON Keys string into a JsonArray
 *
 * @example
 * new JsonKeysParser().parse('one,two(three,four)')
 * ['one', 'two', ['three', 'four']]
 */
public class JsonKeysParser {

    private final char startToken;
    private final char endToken;
    private final char separator;

    public JsonKeysParser() {
        this('(', ')', ',');
    }

    public JsonKeysParser(char startToken, char endToken, char separator) {
        this.startToken = startToken;
        this.endToken = endToken;
        this.separator = separator;
    }

    public JsonArray parse(String keysString) throws ParseException {
        JsonArray result = new JsonArray();

        if (keysString == null) {
            return result;
        }

        int len = keysString.length();
        int currentAtomStartPosition = 0;
        Stack<JsonArray> jsonArrayCursor = new Stack<>();

        jsonArrayCursor.push(result);

        for (int currentCharIndex = 0; currentCharIndex < len; currentCharIndex++) {
            char currentChar = keysString.charAt(currentCharIndex);
            JsonArray currentJsonArray = jsonArrayCursor.peek();

            boolean chop = false;
            JsonArray jsonArray = null;

            if (currentChar == startToken) {
                chop = true;

                jsonArray = new JsonArray();
                jsonArrayCursor.push(jsonArray);
            } else if (currentChar == endToken) {
                chop = true;

                if (jsonArrayCursor.size() <= 1) {
                    throw new ParseException("Mismatched ending token", currentCharIndex);
                }

                jsonArrayCursor.pop();
            } else if (currentChar == separator) {
                chop = true;
            }

            if (chop) {
                if (currentAtomStartPosition < currentCharIndex) {
                    currentJsonArray.add(keysString.substring(currentAtomStartPosition, currentCharIndex));
                }

                currentAtomStartPosition = currentCharIndex + 1;
            }

            if (jsonArray != null) {
                currentJsonArray.add(jsonArray);
            }
        }

        if (jsonArrayCursor.size() != 1) {
            throw new ParseException("Mismatched starting token", len);
        }

        if (currentAtomStartPosition < len) {
            result.add(keysString.substring(currentAtomStartPosition, len));
        }

        return result;
    }
}
