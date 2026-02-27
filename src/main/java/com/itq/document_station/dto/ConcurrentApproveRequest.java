package com.itq.document_station.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class ConcurrentApproveRequest {

    @NotNull
    private Long documentId;
    @Min(1)
    private int threads = 2; // количество параллельных потоков
    @Min(1)
    private int attempts = 1; // количество попыток в каждом потоке

}
