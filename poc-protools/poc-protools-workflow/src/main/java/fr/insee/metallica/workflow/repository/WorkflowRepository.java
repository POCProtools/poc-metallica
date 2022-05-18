package fr.insee.metallica.workflow.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.insee.metallica.workflow.domain.Workflow;

public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {
}
