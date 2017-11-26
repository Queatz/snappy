package com.vlllage.graph;

import com.queatz.earth.EarthField;
import com.queatz.earth.EarthKind;
import com.queatz.earth.EarthQuery;
import com.queatz.snappy.as.EarthAs;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Created by jacob on 11/25/17.
 */
public class GraphQueryTest {
    @Test
    public void testAql1() throws Exception {
        GraphQuery query = new GraphQuery();

        assertEquals(query.aql(), "[]");
    }

    @Test
    public void testAql2() throws Exception {
        GraphQuery query = new GraphQuery();

        query.add();

        assertEquals(query.aql(), "[[]]");
    }

    @Test
    public void testAql3() throws Exception {
        GraphQuery query = new GraphQuery();

        query.add("a");

        assertEquals(query.aql(), "[v1.a]");
    }

    @Test
    public void testAql4() throws Exception {
        GraphQuery query = new GraphQuery();

        query.add("a");
        query.add("b");

        assertEquals(query.aql(), "[v1.a, v1.b]");
    }

    @Test
    public void testAql5() throws Exception {
        GraphQuery query = new GraphQuery();
        GraphQuery subQuery = query.add();

        subQuery.add("a");

        assertEquals(query.aql(), "[[v2.a]]");
    }

    @Test
    public void testAql6() throws Exception {
        GraphQuery query = new GraphQuery();
        GraphQuery subQuery = query.add();
        GraphQuery subQuery2 = query.add();

        subQuery.add("a");
        subQuery2.add("a");

        assertEquals(query.aql(), "[[v2.a], [v2.a]]");
    }

    @Test
    public void testAqlEarth() throws Exception {
        GraphQuery query = new GraphQuery(new EarthQuery(new EarthAs()));

        assertEquals(query.aql(), "for v1 in Collection filter v1.concluded_on == null return v1");
    }

    @Test
    public void testAqlEarth1() throws Exception {
        GraphQuery query = new GraphQuery(new EarthQuery(new EarthAs()));

        query.add("_key");
        query.add(EarthField.KIND);

        assertEquals(query.aql(), "for v1 in Collection filter v1.concluded_on == null return [v1._key, v1.kind]");
    }

    @Test
    public void testAqlEarth2() throws Exception {
        GraphQuery query = new GraphQuery(new EarthQuery(new EarthAs()).limit("1"));

        query.add("_key");
        query.add(EarthField.KIND);

        GraphQuery query2 = query.add(new EarthQuery(new EarthAs()).limit("1"));
        query2.add("_key");
        query2.add(EarthField.KIND);

        assertEquals(query.aql(), "for v1 in Collection filter v1.concluded_on == null limit 1 " +
                "return [v1._key, v1.kind, (for v2 in Collection filter v2.concluded_on == null limit 1 " +
                "return [v2._key, v2.kind])]");
    }

    @Test
    public void testAqlEarth3() throws Exception {
        GraphQuery query = new GraphQuery(new EarthQuery(new EarthAs()).filter(EarthField.GOOGLE_URL, "'" + "jacobferrero" + "'").limit("1"));

        query.add("_key");
        query.add(EarthField.KIND);
        query.add(EarthField.FIRST_NAME);
        query.add(EarthField.LAST_NAME);
        query.add(EarthField.ABOUT);

        GraphQuery query2 = query.add(new EarthQuery(new EarthAs())
                .filter(EarthField.TARGET, query.var() + "._key")
                .filter(EarthField.KIND, "'" + EarthKind.MEMBER_KIND + "'"));
        query2.add("_key");
        query2.add(EarthField.KIND);

        GraphQuery query3 = query2.add(new EarthQuery(new EarthAs())
                .filter("_key", query2.var() + "." + EarthField.SOURCE));
        query3.add("_key");
        query3.add(EarthField.KIND);
        query3.add(EarthField.FIRST_NAME);
        query3.add(EarthField.LAST_NAME);
        query3.add(EarthField.NAME);
        query3.add(EarthField.ABOUT);

        assertEquals(query.aql(), "for v1 in Collection filter v1.google_url == 'jacobferrero' and v1.concluded_on == null limit 1 " +
                "return [v1._key, v1.kind, v1.first_name, v1.last_name, v1.about, (for v2 in Collection filter v2.target == v1._key and v2.kind == 'member' and v2.concluded_on == null " +
                "return [v2._key, v2.kind, (for v3 in Collection filter v3._key == v2.source and v3.concluded_on == null " +
                "return [v3._key, v3.kind, v3.first_name, v3.last_name, v3.name, v3.about])])]");
    }

    @Test
    public void testAqlEarth4() throws Exception {
        GraphQuery query = new GraphQuery(new EarthQuery(new EarthAs()));

        query.add("_key");
        query.add(EarthField.KIND);
        GraphQuery query2 = query.add(new EarthQuery(new EarthAs()).filter(EarthField.SOURCE, "{parent}._key"));

        query2.add("_key");
        query2.add(EarthField.KIND);

        assertEquals(query.aql(), "for v1 in Collection filter v1.concluded_on == null " +
                "return [v1._key, v1.kind, (for v2 in Collection filter v2.source == v1._key and v2.concluded_on == null " +
                "return [v2._key, v2.kind])]");
    }

}