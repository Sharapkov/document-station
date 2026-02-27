package com.itq.document_station.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ConcurrentApproveResponse {
    private int totalAttempts;
    private int successCount;
    private int conflictCount;
    private int errorCount;
    private DocWithHistoryDto doc;
    private RegisterDto register;

}
