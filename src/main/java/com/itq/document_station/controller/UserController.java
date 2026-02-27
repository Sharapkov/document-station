package com.itq.document_station.controller;

import com.itq.document_station.dto.MessageResponse;
import com.itq.document_station.exception.EntityNotFoundException;
import com.itq.document_station.utill.MethodLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value="api/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<MessageResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        log.info("REST request to " + MethodLogger.getMethodName());
        if (userDetails == null) throw new EntityNotFoundException("Not authenticated");
        log.info("userDetails: {}", userDetails.toString());
        return ResponseEntity.ok(new MessageResponse(HttpStatus.OK.value(), userDetails.getUsername()));
    }
}
