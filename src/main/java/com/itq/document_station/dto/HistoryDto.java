package com.itq.document_station.dto;

import com.itq.document_station.enumeration.ActionEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class HistoryDto {

    private Long id;
    private UserDto user;
    private ActionEnum action;
    private LocalDateTime createdDate;
    private String comment;

}
