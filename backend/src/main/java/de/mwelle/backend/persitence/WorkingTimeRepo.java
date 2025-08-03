package de.mwelle.backend.persitence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface WorkingTimeRepo extends JpaRepository<WorkingTime, Integer> {
    Optional<WorkingTime> findByEmployeeAndDate(Employee employee, LocalDate date);
}
