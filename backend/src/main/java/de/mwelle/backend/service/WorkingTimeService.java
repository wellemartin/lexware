package de.mwelle.backend.service;


import de.mwelle.backend.dto.WorkingTimeDto;
import de.mwelle.backend.enums.Role;
import de.mwelle.backend.logic.WorkingTimeMapper;
import de.mwelle.backend.persitence.Employee;
import de.mwelle.backend.persitence.EmployeeRepo;
import de.mwelle.backend.persitence.WorkingTime;
import de.mwelle.backend.persitence.WorkingTimeRepo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

@Service
@RequiredArgsConstructor
@Log4j2
public class WorkingTimeService {

    private final WorkingTimeRepo workingTimeRepo;
    private final EmployeeRepo employeeRepo;
    private final Path workingTimePath = Paths.get("./workingTime");

    @PostConstruct
    private void init() {
        Employee employee = new Employee();
        employee.setName("Max Mustermann");
        employee.setRole(Role.EMPLOYEE);
        employeeRepo.save(employee);
        Employee employee2 = new Employee();
        employee2.setName("Maria Musterfrau");
        employee2.setRole(Role.EMPLOYEE);
        employeeRepo.save(employee2);
        Executors.newSingleThreadExecutor().submit(() -> {
            try (WatchService watchService = workingTimePath.getFileSystem().newWatchService()) {
                readExternalWorkingTime(watchService);
            } catch (Exception e) {
                log.error("Error reading external working time files", e);
            }
        });
    }

    public ResponseEntity<Void> saveWorkingTime(WorkingTimeDto workingTime) {
        return save(WorkingTimeMapper.INSTANCE.toEntity(workingTime, this));
    }

    private ResponseEntity<Void> save(WorkingTime workingTime) {
        if (workingTime == null || workingTime.getEmployee() == null || workingTime.getStartTime() == null || workingTime.getEndTime() == null) {
            log.warn("Invalid working time data: {}", workingTime);
            return (workingTime!=null && workingTime.getEmployee() == null) ? ResponseEntity.notFound().build() : ResponseEntity.badRequest().build();
        }
        AtomicReference<ResponseEntity<Void>> response = new AtomicReference<>(ResponseEntity.ok().build());
        workingTimeRepo.findByEmployeeAndDate(
                workingTime.getEmployee(),
                workingTime.getDate()
        ).ifPresentOrElse(existing -> {
            log.warn("Working time already exists for employee: {} at {}", existing.getEmployee().getName(), existing.getStartTime());
            response.set(ResponseEntity.status(409).build()); // Conflict
        }, () -> {
            workingTimeRepo.save(workingTime);
        });

        return response.get();
    }

    private void readExternalWorkingTime(WatchService watchService) throws IOException, InterruptedException {
        if (!Files.exists(workingTimePath)) {
            Files.createDirectories(workingTimePath);
        }
        workingTimePath.register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_MODIFY
        );

        while (true) {
            WatchKey key = watchService.take();
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                Path fileName = (Path) event.context();
                Path fullPath = workingTimePath.resolve(fileName);
                if (kind == StandardWatchEventKinds.ENTRY_CREATE || kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    try (BufferedReader br = new BufferedReader(new FileReader(fullPath.toFile()))) {
                        String line;

                        while ((line = br.readLine()) != null) {
                            String[] fields = line.split(",");
                            save(
                                    WorkingTimeMapper.INSTANCE.toEntity(fields, this)
                            );
                        }

                    } catch (IOException e) {
                        log.error("Error reading file: " + fullPath, e);
                    }
                }

            }

            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }
    }

    public Employee getEmployeeById(Integer id) {
        return this.employeeRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee with ID " + id + " not found"));
    }

    public Employee getEmployeeByName(String name) {
        return this.employeeRepo.findByName(name);
    }

    public ResponseEntity<List<WorkingTimeDto>> getWorkingTime() {
        List<WorkingTime> workingTimes = workingTimeRepo.findAll();
        if (workingTimes.isEmpty()) {
            log.warn("No working times found");
            return ResponseEntity.noContent().build();
        }
        List<WorkingTimeDto> workingTimeDtos = WorkingTimeMapper.INSTANCE.toDtoList(workingTimes);
        return ResponseEntity.ok(workingTimeDtos);
    }
}
