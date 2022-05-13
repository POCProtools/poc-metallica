package fr.insee.metallica.pocprotools.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import fr.insee.metallica.pocprotools.domain.WorkflowStep;

public interface WorkflowStepRepository extends JpaRepository<WorkflowStep, UUID> {
}
