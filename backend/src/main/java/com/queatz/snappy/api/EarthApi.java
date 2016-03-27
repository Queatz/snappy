package com.queatz.snappy.api;

import com.queatz.snappy.earth.access.As;
import com.queatz.snappy.earth.access.NothingEarthException;
import com.queatz.snappy.earth.concept.ViewConcept;
import com.queatz.snappy.earth.concept.RepositoryConcept;
import com.queatz.snappy.earth.thing.Existence;
import com.queatz.snappy.earth.thing.UpdateRelation;
import com.queatz.snappy.service.Api;
import com.queatz.snappy.earth.Earth;
import com.queatz.snappy.shared.Config;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

/**
 * Created by jacob on 3/26/16.
 */
public class EarthApi extends Api.Path {
    public EarthApi(Api api) {
        super(api);
    }

    @Override
    public void call() throws IOException {
        try {
            handle();
        } catch (NothingEarthException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handle() throws IOException {
        switch (method) {
            case GET:
                if (path.size() > 0) {
                    if (Config.PATH_HERE.equals(path.get(0))) {
                        return;
                    }
                }

                switch (path.size()) {
                    case 2:
                        get(path.get(0), path.get(1));

                        break;
                    default:
                        die("thing - bad path");
                }
                break;
            case POST:
                switch (path.size()) {
                    case 1:
                        post(path.get(0));

                        break;
                    default:
                        die("thing - bad path");
                }
                break;
            case DELETE:

                break;
            case PUT:

                break;
            default:
                die("thing - bad method");
        }
    }

    private void get(String kind, String id) throws IOException {

        Existence thing = Earth
                .as(user)
                .concept(RepositoryConcept.class)
                .get(kind)
                .id(id);

        String json = Earth
                .as(user)
                .concept(ViewConcept.class)
                .read(thing)
                .json();

        response.getWriter().write(json);
    }

    private void post(String kind) {
        Existence thing = Earth
                .as(user)
                .concept(RepositoryConcept.class)
                .make(kind);

        Earth.as(user)
                .concept(ViewConcept.class)
                .write(thing)
                .json(request.getParameterMap());

        Earth.as(user)
                .concept(RepositoryConcept.class)
                .save(thing);
    }

    private void update(String kind, String id) throws IOException {
        final As as = Earth.as(user);
        final RepositoryConcept repository = as.concept(RepositoryConcept.class);
        final ViewConcept view = as.concept(ViewConcept.class);

        UpdateRelation update = repository.make(UpdateRelation.class);
        view.write(update).json(request.getParameterMap());
        repository.save(update);

        response.getWriter().write(view.read(update).json());
    }
}
