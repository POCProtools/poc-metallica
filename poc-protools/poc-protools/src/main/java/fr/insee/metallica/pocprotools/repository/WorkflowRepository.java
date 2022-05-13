package fr.insee.metallica.pocprotools.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.insee.metallica.pocprotools.domain.Workflow;

public interface WorkflowRepository extends JpaRepository<Workflow, UUID> {
}
