package com.onetick.controller;

import com.onetick.dto.request.CreateAutomationRuleRequest;
import com.onetick.dto.response.AutomationRuleResponse;
import com.onetick.service.AutomationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/automation/rules")
public class AutomationController {
    private final AutomationService automationService;

    public AutomationController(AutomationService automationService) {
        this.automationService = automationService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD')")
    public ResponseEntity<AutomationRuleResponse> createRule(@Valid @RequestBody CreateAutomationRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(automationService.createRule(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD','STAFF')")
    public ResponseEntity<List<AutomationRuleResponse>> listRules(@RequestParam(required = false) Long workspaceId) {
        return ResponseEntity.ok(automationService.listRules(workspaceId));
    }
}
