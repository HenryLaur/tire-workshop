package tire.workshop.service;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tire.workshop.client.XmlClient;
import tire.workshop.config.WorkshopProperties;
import tire.workshop.dto.WorkshopAppointment;
import tire.workshop.dto.xml.TireChangeBookingRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class XmlClientStrategy implements WorkShopClientStrategy {

    private final XmlClient xmlClient;

    @Override
    public List<WorkshopAppointment> findAppointments(WorkshopProperties.Workshop workshop, Instant from, Instant to) {
        return xmlClient
            .findAppointments(workshop.getUrl(), from, to)
            .stream()
            .map(appointment -> new WorkshopAppointment(appointment, workshop))
            .toList();
    }

    @Override
    public WorkshopAppointment bookAppointment(
        WorkshopProperties.Workshop workshop,
        String uuid,
        TireChangeBookingRequest request
    ) {
        return new WorkshopAppointment(xmlClient.bookAppointment(workshop.getUrl(), uuid, request), workshop);
    }
}
