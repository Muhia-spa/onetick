package com.onetick.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateDepartmentRequest {
    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 20)
    private String code;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
