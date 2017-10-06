package com.queatz.snappy.exceptions;

import java.lang.*;

/**
 * Created by jacob on 4/2/16.
 */
public class LogicException extends PrintingError {
    public LogicException(String reason) {
        super(Error.SERVER_ERROR, reason);
    }
}
