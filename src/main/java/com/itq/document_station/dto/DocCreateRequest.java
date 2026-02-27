package com.itq.document_station.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class DocCreateRequest {

    @NotBlank(message = "Document name cannot be blank")
    @NotNull(message = "Document name cannot be null")
    String name;
}
