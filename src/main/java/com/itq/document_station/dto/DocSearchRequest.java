package com.itq.document_station.dto;

import com.itq.document_station.enumeration.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class DocSearchRequest {
    @NotNull(message = "IDs list must not be null")
    @NotEmpty(message = "IDs list must not be empty")
    private List<Long> ids;
    private StatusEnum status;
    private Long authorId;
    private LocalDateTime dateFrom;
    private LocalDateTime dateTo;

    private int page = 0;
    private int size = 20;
    private String sortBy = "updatedDate";
    private Sort.Direction sortDirection = Sort.Direction.DESC;
}
