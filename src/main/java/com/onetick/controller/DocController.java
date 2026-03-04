package com.onetick.controller;

import com.onetick.dto.request.CreateDocRequest;
import com.onetick.dto.request.UpdateDocRequest;
import com.onetick.dto.response.DocResponse;
import com.onetick.service.DocService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/docs")
public class DocController {
    private final DocService docService;

    public DocController(DocService docService) {
        this.docService = docService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD','STAFF')")
    public ResponseEntity<DocResponse> create(@Valid @RequestBody CreateDocRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(docService.create(request));
    }

    @PutMapping("/{docId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD','STAFF')")
    public ResponseEntity<DocResponse> update(@PathVariable Long docId,
                                              @Valid @RequestBody UpdateDocRequest request) {
        return ResponseEntity.ok(docService.update(docId, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD','STAFF')")
    public ResponseEntity<List<DocResponse>> list(@RequestParam(required = false) Long workspaceId) {
        return ResponseEntity.ok(docService.list(workspaceId));
    }
}
