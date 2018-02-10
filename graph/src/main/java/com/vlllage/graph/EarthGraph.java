package com.vlllage.graph;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.queatz.earth.EarthField;
import com.queatz.earth.EarthQuery;
import com.queatz.earth.EarthStore;
import com.queatz.snappy.as.EarthAs;
import com.queatz.snappy.as.EarthControl;
import com.queatz.snappy.exceptions.NothingLogicResponse;
import com.queatz.snappy.shared.EarthJson;
import com.vlllage.graph.fields.EarthGraphField;
import com.vlllage.graph.util.JsonKeysParser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EarthGraph extends EarthControl {

    private static final Map<String, EarthGraphField> fields = new HashMap<>();

    public EarthGraph(@NotNull EarthAs as) {
        super(as);
    }

    public static void register(@NotNull String field, @NotNull EarthGraphField earthGraphField) {
        if (fields.containsKey(field)) {
            throw new IllegalStateException("Cannot re-register field: " + field);
        }

        fields.put(field, earthGraphField);
    }

    public JsonArray query(@NotNull EarthQuery earthQuery, @NotNull String selectJsonKeys) {
        return query(earthQuery, selectJsonKeys, null);
    }

    public JsonArray query(@NotNull EarthQuery earthQuery, @NotNull String selectJsonKeys, @Nullable Map<String, Object> vars) {
        JsonArray select;

        if (vars == null) {
            vars = ImmutableMap.of();
        }

        try {
            select = use(JsonKeysParser.class).parse(selectJsonKeys);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

        GraphQuery query = new GraphQuery(earthQuery);
        explode(query, select);

        JsonArray result = new JsonArray();

        use(EarthStore.class)
                .queryRawAs(query.aql(), vars, List.class)
                .stream()
                .map(str -> use(EarthJson.class).toJsonTree(str))
                .forEach(result::add);

        return transformListResult(select, query, result);
    }

    /**
     * Transform database query result into graph results
     *
     * @param select    name,about,members(source(name,about))
     * @param query     (name, about, (members))
     * @param result    [[name, about, [[name, about], ...]], ...]
     * @return
     */
    private JsonArray transformListResult(JsonArray select, GraphQuery query, JsonArray result) {
        JsonArray transformed = new JsonArray();

        if (!query.isList()) {
            throw new NothingLogicResponse("transformListResult: Cannot transform non-list result.");
        }

        for (int i = 0; i < result.size(); i++) {
            transformed.add(transformObjectResult(select, query, result.get(i).getAsJsonArray()));
        }

        return transformed;
    }

    private JsonObject transformObjectResult(JsonArray select, GraphQuery query, JsonArray result) {
        JsonObject transformed = new JsonObject();

        Map<String, JsonElement> results = new HashMap<>();
        Map<String, GraphQuery> queries = new HashMap<>();

        if (!query.isList()) {
            throw new NothingLogicResponse("transformObjectResult: Cannot transform non-list result.");
        }

        for (int i = 0; i < result.size(); i++) {
            results.put(query.getList().get(i).getTag(), result.get(i));
            queries.put(query.getList().get(i).getTag(), query.getList().get(i));
        }

        // Add default properties
        transformed.add("id", results.get("_key"));
        transformed.add(EarthField.KIND, results.get("kind"));

        for (int i = 0; i < select.size(); i++) {
            if (!select.get(i).isJsonPrimitive()) {
                continue;
            }

            String s = select.get(i).getAsString();
            EarthGraphField f = getField(s);

            if (f.type() == EarthGraphField.Type.VALUE) {
                JsonElement[] selectionResults = Arrays.stream(f.selection())
                        .map(results::get)
                        .collect(Collectors.toList())
                        .toArray(new JsonElement[f.selection().length]);

                transformed.add(s, f.view(as.hasUser() ? as.getUser() : null, selectionResults));
            } else if (f.type() == EarthGraphField.Type.EXPRESSION) {
                JsonElement r = results.get("@" + s);
                transformed.add(s, f.view(as.hasUser() ? as.getUser() : null, new JsonElement[] { r }));
            } else {
                JsonElement r = results.get("@" + s);
                GraphQuery q = queries.get("@" + s);

                if (r == null || q == null) {
                    throw new RuntimeException("Selection mismatch: " + s);
                }

                JsonArray next = i < select.size() - 1 && select.get(i + 1).isJsonArray() ? select.get(i + 1).getAsJsonArray() : new JsonArray();

                if (f.type() == EarthGraphField.Type.OBJECT) {
                    transformed.add(s, r.getAsJsonArray().size() > 0 ? transformObjectResult(next, q, r.getAsJsonArray().get(0).getAsJsonArray()) : null);
                } else if (f.type() == EarthGraphField.Type.LIST) {
                    transformed.add(s, transformListResult(next, q, r.getAsJsonArray()));
                } else {
                    throw new IllegalStateException("Unsupported field type: " + f.type());
                }
            }
        }

        return transformed;
    }

    private void explode(GraphQuery cursor, JsonArray select) {
        // Default fields
        cursor.add("_key");
        cursor.add(EarthField.KIND);

        if (select != null) for (int i = 0; i < select.size(); i++) {
            JsonElement json = select.get(i);

            if (json.isJsonPrimitive()) {
                String fieldName = json.getAsString();
                EarthGraphField field = getField(json.getAsString());

                if (field.type() == EarthGraphField.Type.EXPRESSION) {
                    EarthQuery q = field.query(as);

                    if (q != null) {
                        cursor.add(q).setTag("@" + fieldName);
                    }
                } else if (field.type() != EarthGraphField.Type.VALUE) {
                    JsonElement next = i < select.size() - 1 ? select.get(i + 1) : null;
                    next = next != null && next.isJsonArray() ? next.getAsJsonArray() : null;

                    explode(cursor.add(field.query(as)).setTag("@" + fieldName), (JsonArray) next);

                    if (next != null) {
                        i++;
                    }
                } else for (String selection : field.selection()) {
                    cursor.add(selection);
                }
            }
        }
    }

    private EarthGraphField getField(String field) {
        if (!fields.containsKey(field)) {
            throw new NothingLogicResponse("unsupported field: " + field);
        }

        return fields.get(field);
    }

    public JsonObject queryOne(EarthQuery earthQuery, String select, Map<String, Object> vars) {
        JsonArray result = query(earthQuery, select, vars);
        return result != null && result.size() > 0 ? result.get(0).getAsJsonObject() : null;
    }
}
