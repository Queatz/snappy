package com.queatz.snappy.api;

import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.service.Api;

import java.io.IOException;

/**
 * Created by jacob on 9/15/15.
 */
public class Quest extends Api.Path {
    public Quest(Api api) {
        super(api);
    }

    @Override
    public void call() throws IOException, PrintingError {
        switch (method) {
            case POST:
                switch (path.size()) {
                    case 0:
                        // New quest
                        break;
                    case 1:
                        // Start/join quest
                        break;
                    default:
                        die("quest - bad path");
                }

                break;
            case DELETE:
                switch (path.size()) {
                    case 1:
                        // Delete quest
                        break;
                    default:
                        die("quest - bad path");
                }

                break;
            default:
                die("quest - bad path");
        }
    }
}
