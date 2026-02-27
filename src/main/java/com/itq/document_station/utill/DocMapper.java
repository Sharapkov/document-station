package com.itq.document_station.utill;

import com.itq.document_station.dto.DocDto;
import com.itq.document_station.dto.DocWithHistoryDto;
import com.itq.document_station.dto.HistoryDto;
import com.itq.document_station.dto.UserDto;
import com.itq.document_station.model.Doc;
import com.itq.document_station.model.History;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DocMapper {
    public static DocWithHistoryDto mapToDocWithHistoryDto(Doc doc){
        DocWithHistoryDto dto = new DocWithHistoryDto();

        dto.setId(doc.getId());
        dto.setDocNumber(doc.getDocNumber());
        dto.setName(doc.getName());
        dto.setStatus(doc.getStatus());
        dto.setCreatedDate(doc.getCreatedDate());
        dto.setUpdatedDate(doc.getUpdatedDate());

        UserDto userDto = new UserDto();
        userDto.setId(doc.getUser().getId());
        userDto.setUsername(doc.getUser().getUsername());
        dto.setUser(userDto);

        List<HistoryDto> histories = doc.getHistories().stream()
                .sorted(Comparator.comparing(History::getCreatedDate).reversed())
                .map(h -> {
                    HistoryDto history = new HistoryDto();
                    history.setId(h.getId());
                    history.setUser(new UserDto(h.getUser().getId(), h.getUser().getUsername()));
                    history.setAction(h.getAction());
                    history.setCreatedDate(h.getCreatedDate());
                    history.setComment(h.getComment());
                    return history;
                })
                .collect(Collectors.toList());

        dto.setHistories(histories);

        return dto;
    }

    public static List<DocDto> mapToList(List<Doc> docs){
        List<DocDto> docsDto = new ArrayList<>();
        docs.forEach(doc -> docsDto.add(mapToDto(doc)));
        return docsDto;
    }

    public static DocDto mapToDto(Doc doc){
        DocDto dto = new DocDto();

        dto.setId(doc.getId());
        dto.setDocNumber(doc.getDocNumber());
        dto.setName(doc.getName());
        dto.setStatus(doc.getStatus());
        dto.setCreatedDate(doc.getCreatedDate());
        dto.setUpdatedDate(doc.getUpdatedDate());

        UserDto userDto = new UserDto();
        userDto.setId(doc.getUser().getId());
        userDto.setUsername(doc.getUser().getUsername());
        dto.setUser(userDto);

        return dto;
    }
}
