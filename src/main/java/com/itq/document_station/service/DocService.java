package com.itq.document_station.service;

import com.itq.document_station.dto.*;
import com.itq.document_station.enumeration.ProcessingResult;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Map;

public interface DocService {

    DocDto createDoc(UserDetails userDetails, DocCreateRequest request);

    DocWithHistoryDto findById(Long id);

    PageDto<DocDto> findBatch(DocSearchRequest request);

    Map<Long, ProcessingResult> submitDocuments(List<Long> ids, UserDetails userDetails, String comment);

    Map<Long, ProcessingResult> approveDocuments(List<Long> ids, UserDetails userDetails, String comment);

    ConcurrentApproveResponse concurrentApprove(UserDetails userDetails, ConcurrentApproveRequest request, String comment);
}
