package tire.workshop.web;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tire.workshop.dto.WorkshopAppointment;
import tire.workshop.service.WorkshopService;

@Slf4j
@RestController
@RequestMapping("/api/workshops")
@RequiredArgsConstructor
public class WorkshopResource {

    private final WorkshopService workshopService;

    @GetMapping
    public ResponseEntity<List<WorkshopAppointment>> findAppointments(
        @RequestParam Instant from,
        @RequestParam Instant to,
        @RequestParam(required = false) String address,
        @RequestParam(required = false) String car
    ) {
        log.info("REST request get appointments from: {}, to: {}, address: {}, car: {}", from, to, address, car);
        List<WorkshopAppointment> workshopAppointments = workshopService.findAppointments(from, to, address, car);
        return ResponseEntity.ok(workshopAppointments);
    }

    @PutMapping
    public ResponseEntity<WorkshopAppointment> bookAppointment(
        @RequestParam String uuid,
        @RequestParam String name,
        @RequestParam String contactInfo
    ) {
        log.info("REST request to book appointment uuid: {}, name: {}, contactInfo: {}", uuid, name, contactInfo);
        WorkshopAppointment response = workshopService.bookAppointment(uuid, name, contactInfo);
        return ResponseEntity.ok(response);
    }
}
