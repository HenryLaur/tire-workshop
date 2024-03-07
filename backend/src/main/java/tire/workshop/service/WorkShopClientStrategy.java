package tire.workshop.service;

import java.time.Instant;
import java.util.List;
import tire.workshop.config.WorkshopProperties;
import tire.workshop.dto.WorkshopAppointment;
import tire.workshop.dto.xml.TireChangeBookingRequest;

public interface WorkShopClientStrategy {
    List<WorkshopAppointment> findAppointments(WorkshopProperties.Workshop workshopUrl, Instant from, Instant to);
    WorkshopAppointment bookAppointment(
        WorkshopProperties.Workshop workshop,
        String uuid,
        TireChangeBookingRequest request
    );
}
