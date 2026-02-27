package com.itq.document_station.enumeration;

/**
 * Статус документа
 */
public enum StatusEnum {

    DRAFT("Черновик"),
    SUBMITTED("На согласовании"),
    APPROVED("Согласован");

    private final String status;

    StatusEnum(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
