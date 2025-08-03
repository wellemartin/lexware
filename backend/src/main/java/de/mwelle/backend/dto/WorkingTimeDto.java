package de.mwelle.backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class WorkingTimeDto {
    private String name;
    private LocalDate date;
    private String startTime;
    private String endTime;
}
