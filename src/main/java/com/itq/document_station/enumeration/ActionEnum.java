package com.itq.document_station.enumeration;


/**
 * Действие над документом
 */
public enum ActionEnum {

    SHIFT_SUBMITTED("Перевод в согласование"),
    SHIFT_APPROVED("Перевод в утверждение");

    private final String action;

    ActionEnum(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }
}
