package de.mwelle.backend.api;

import de.mwelle.backend.dto.WorkingTimeDto;
import de.mwelle.backend.service.WorkingTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST,
        RequestMethod.DELETE, RequestMethod.PUT})
@RequestMapping("/working-time")
@RequiredArgsConstructor
public class WorkingTimeController {

    private final WorkingTimeService workingTimeService;

    @PostMapping
    public ResponseEntity<Void> saveWorkingTime(@RequestBody WorkingTimeDto workingTime) {
        return workingTimeService.saveWorkingTime(workingTime);
    }

    @GetMapping
    public ResponseEntity<List<WorkingTimeDto>> getWorkingTime() {
        return workingTimeService.getWorkingTime();
    }
}
