package de.mwelle.backend.logic;

import de.mwelle.backend.dto.WorkingTimeDto;
import de.mwelle.backend.persitence.Employee;
import de.mwelle.backend.persitence.WorkingTime;
import de.mwelle.backend.service.WorkingTimeService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class WorkingTimeMapper {

    public static WorkingTimeMapper INSTANCE = Mappers.getMapper(WorkingTimeMapper.class);

    @Mapping(source = "workingTimeDto.startTime", target = "startTime")
    @Mapping(source = "workingTimeDto.endTime", target = "endTime")
    @Mapping(target = "employee", expression = "java(mapEmployeeFromName(workingTimeDto.getName(), workingTimeService))")
    public abstract WorkingTime toEntity(WorkingTimeDto workingTimeDto, WorkingTimeService workingTimeService);

    public WorkingTime toEntity(String[] csvEntry, WorkingTimeService workingTimeService) {
        if (csvEntry == null || csvEntry.length < 3) {
            return null;
        }

        WorkingTime workingTime = new WorkingTime();
        workingTime.setEmployee(mapEmployeeFromName(csvEntry[0], workingTimeService));
        workingTime.setDate(LocalDate.parse(csvEntry[1]));
        workingTime.setStartTime(csvEntry[1]);
        workingTime.setEndTime(csvEntry[2]);

        return workingTime;
    }

    protected Employee mapEmployeeFromName(String name, WorkingTimeService workingTimeService) {
        return workingTimeService.getEmployeeByName(name);
    }

    public List<WorkingTimeDto> toDtoList(List<WorkingTime> workingTimes) {
        if (workingTimes == null || workingTimes.isEmpty()) {
            return List.of();
        }
        return workingTimes.stream()
                .map(this::toDto)
                .toList();
    }

    @Mapping(target = "name", source = "workingTime.employee", qualifiedByName = "mapEmployeeToName")
    public abstract WorkingTimeDto toDto(WorkingTime workingTime);

    @Named(value = "mapEmployeeToName")
    protected String mapEmployeeToName(Employee employee){
        return employee!=null ? employee.getName() : null;
    }
}
