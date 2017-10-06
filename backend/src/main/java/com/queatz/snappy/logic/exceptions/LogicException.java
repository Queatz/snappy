package com.queatz.snappy.logic.exceptions;

import com.queatz.snappy.api.PrintingError;
import com.queatz.snappy.api.Error;

/**
 * Created by jacob on 4/2/16.
 */
public class LogicException extends PrintingError {
    public LogicException(String reason) {
        super(Error.SERVER_ERROR, reason);
    }
}
