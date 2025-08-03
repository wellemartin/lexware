package de.mwelle.backend.persitence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepo extends JpaRepository<Employee, Integer> {

    Employee findByName(String name);
}
