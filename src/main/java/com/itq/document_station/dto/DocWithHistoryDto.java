package com.itq.document_station.dto;

import com.itq.document_station.enumeration.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DocWithHistoryDto {

    private Long id;
    private String docNumber;
    private String name;
    private StatusEnum status;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private UserDto user;
    private List<HistoryDto> histories = new ArrayList<>();

}
