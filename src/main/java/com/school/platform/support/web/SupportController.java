package com.school.platform.support.web;

import com.school.platform.support.application.dto.HelpCenterDataDTO;
import com.school.platform.support.application.dto.SupportTicketDTO;
import com.school.platform.support.application.dto.SupportTicketPayloadDTO;
import com.school.platform.support.application.SupportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/support")
@RequiredArgsConstructor
public class SupportController {
    private final SupportService supportService;

    @GetMapping("/help-center")
    @PreAuthorize("isAuthenticated()")
    public HelpCenterDataDTO getHelpCenter() {
        return supportService.getHelpCenter();
    }

    @PostMapping("/tickets")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public SupportTicketDTO createTicket(@Valid @RequestBody SupportTicketPayloadDTO payload) {
        return supportService.createTicket(payload);
    }
}
