package org.babi.backend.common.api.handler;

public enum GlobalErrorAttributesKey {
    HTTP_STATUS("httpStatus"), MESSAGE("message");
    private final String value;

    GlobalErrorAttributesKey(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
