package com.onetick.service;

import com.onetick.dto.request.CreateDocRequest;
import com.onetick.dto.request.UpdateDocRequest;
import com.onetick.dto.response.DocResponse;

import java.util.List;

public interface DocService {
    DocResponse create(CreateDocRequest request);
    DocResponse update(Long docId, UpdateDocRequest request);
    List<DocResponse> list(Long workspaceId);
}
