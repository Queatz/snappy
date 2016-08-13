package com.queatz.snappy.backend;

import com.queatz.snappy.service.Api;

/**
 * Throwing this exception will be caught by the servlet and expose to the client.
 *
 * Created by jacob on 2/8/15.
 */
public class PrintingError extends RuntimeException {
    Api.Error mError;
    String mReason;

    public PrintingError(Api.Error error) {
        mError = error;
    }

    public PrintingError(Api.Error error, String reason) {
        mError = error;
        mReason = reason;
    }

    public Api.Error getError() {
        return mError;
    }

    public String getReason() {
        return mReason;
    }

    @Override
    public String toString() {
        return getError() + " | " + getReason();
    }
}
