package fr.insee.metallica.pocprotools.command.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import fr.insee.metallica.pocprotools.command.domain.Command;
import fr.insee.metallica.pocprotools.command.domain.Command.Status;

public interface CommandRepository extends JpaRepository<Command, UUID>{
	@Query
	public Page<Command> findPageByStatusInAndNextScheduledTimeLessThanEqualOrderByNextScheduledTimeAsc(List<Command.Status> status, LocalDateTime before, Pageable p);

	@Query
	public Page<Command> findPageByStatusAndLastHeartBeatLessThanEqual(Command.Status status, LocalDateTime lastHeartBeat, Pageable p);

	public int countByLimitKeyAndStatus(String limitKey, Status status);

	public default Command getOneCommandToRun() {
		var page = findPageByStatusInAndNextScheduledTimeLessThanEqualOrderByNextScheduledTimeAsc(List.of(Status.Pending, Status.Retry), LocalDateTime.now(),  PageRequest.of(0, 1));
		if (page.getNumberOfElements() == 0) {
			return null;
		}
		return page.getContent().get(0);
	}

	public default List<Command> findCommandsScheduled(int nbElement) {
		var page = findPageByStatusInAndNextScheduledTimeLessThanEqualOrderByNextScheduledTimeAsc(List.of(Status.Pending, Status.Retry), LocalDateTime.now(),  PageRequest.of(0, nbElement));
		return page.getContent();
	}

	@Query(
			nativeQuery = true,
			value = "Update command set status = :next where id = :id and status = :current"
		)
	@Modifying
	@Transactional
	public int setStatus(@Param("id") UUID id, @Param("current") String current, @Param("next") String next);

	@Query(
			nativeQuery = true,
			value = "Update command set status = :next where id = :id and status in (:current)"
		)
	@Modifying
	@Transactional
	public int setStatus(@Param("id") UUID id, @Param("current") List<String> current, @Param("next") String next);

	@Query(
		nativeQuery = true,
		value = "Update command set status = :next, next_scheduled_time = :next_scheduled_time where id = :id and status = :current"
	)
	@Modifying
	@Transactional
	public int reschedule(@Param("id") UUID id, @Param("current") String current, @Param("next") String next, @Param("next_scheduled_time") LocalDateTime nextscheduledtime);

	@Transactional
	public default int setStatus(UUID id, Status current, Status next) {
		return setStatus(id, current.toString(), next.toString());
	}

	@Transactional
	public default int setStatus(UUID id, List<Status> current, Status next) {
		return setStatus(id, current.stream().map(Status::toString).collect(Collectors.toList()), next.toString());
	}

	
	@Query(
		nativeQuery = true,
		value = "Update command set last_heart_beat = :lastHeartBeat where id = :id"
	)
	@Modifying
	@Transactional
	public void heartBeat(@Param("id") UUID id, @Param("lastHeartBeat") LocalDateTime lastHeartBeat);

	@Transactional
	public default void heartBeat(UUID id) {
		heartBeat(id, LocalDateTime.now());
	}

	@Transactional
	default public void reschedule(UUID commandId, Status currentStatus, Status nextStatus, LocalDateTime plusSeconds) {
		reschedule(commandId, currentStatus.toString(), nextStatus.toString(), plusSeconds);
	}
}
