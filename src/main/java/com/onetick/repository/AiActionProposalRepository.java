package com.onetick.repository;

import com.onetick.entity.AiActionProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AiActionProposalRepository extends JpaRepository<AiActionProposal, Long>, JpaSpecificationExecutor<AiActionProposal> {
}
