package de.wenda.it.runtime;

import java.io.Serial;

public class ActionException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -3124100515716359988L;

    public ActionException(Throwable throwable) {
        super(throwable);
    }
}
