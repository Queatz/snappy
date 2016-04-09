package com.queatz.snappy.logic.exceptions;

import com.queatz.snappy.backend.PrintingError;
import com.queatz.snappy.service.Api;

/**
 * Created by jacob on 4/2/16.
 */
public class LogicException extends PrintingError {
    public LogicException(String reason) {
        super(Api.Error.SERVER_ERROR, reason);
    }
}
