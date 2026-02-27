package com.onetick.controller;

import com.onetick.dto.request.CreateDepartmentRequest;
import com.onetick.dto.response.DepartmentResponse;
import com.onetick.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody CreateDepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.create(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','TEAM_LEAD')")
    public ResponseEntity<List<DepartmentResponse>> list() {
        return ResponseEntity.ok(departmentService.list());
    }
}
