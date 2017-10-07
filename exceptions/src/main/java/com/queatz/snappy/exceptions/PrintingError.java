package com.queatz.snappy.exceptions;

/**
 * Throwing this exception will be caught by the servlet and expose to the client.
 *
 * Created by jacob on 2/8/15.
 */
public class PrintingError extends RuntimeException {

    private Error error;
    private String reason;

    public PrintingError(Error error) {
        this.error = error;
    }

    public PrintingError(Error error, String reason) {
        this.error = error;
        this.reason = reason;
    }

    public Error getError() {
        return error;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return getError() + " | " + getReason();
    }
}
