package com.onetick.repository;

import com.onetick.entity.Project;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long>, JpaSpecificationExecutor<Project> {
    @Override
    @Cacheable(cacheNames = "projects", key = "#id")
    Optional<Project> findById(Long id);

    @Override
    @CachePut(cacheNames = "projects", key = "#result.id")
    <S extends Project> S save(S entity);

    @Override
    @CacheEvict(cacheNames = "projects", key = "#id")
    void deleteById(Long id);

    Optional<Project> findByWorkspaceIdAndCode(Long workspaceId, String code);
}
