package tire.workshop.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;
import tire.workshop.client.JsonClient;
import tire.workshop.client.XmlClient;
import tire.workshop.config.WorkshopProperties;
import tire.workshop.config.WorkshopTypeEnum;
import tire.workshop.dto.WorkshopAppointment;
import tire.workshop.dto.xml.TireChangeBookingRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkshopService {

    private final WorkshopProperties workshopProperties;
    private final XmlClient xmlClient;
    private final JsonClient jsonClient;

    public List<WorkshopAppointment> findAppointments(Instant from, Instant to, String address, String car) {
        log.info("Request to find appointments from: {}, to: {}, address: {}, car: {}", from, to, address, car);
        List<WorkshopProperties.Workshop> workshops = filterWorkShops(address, car, null);
        List<WorkshopAppointment> workshopAppointments = new ArrayList<>();
        for (WorkshopProperties.Workshop workshop : workshops) {
            try {
                if (workshop.getType().equals(WorkshopTypeEnum.XML)) {
                    workshopAppointments.addAll(
                        xmlClient
                            .findAppointments(workshop.getUrl(), from, to)
                            .stream()
                            .map(appointment -> new WorkshopAppointment(appointment, workshop))
                            .toList()
                    );
                } else if (workshop.getType().equals(WorkshopTypeEnum.JSON)) {
                    workshopAppointments.addAll(
                        jsonClient
                            .findAppointments(workshop.getUrl(), from)
                            .stream()
                            .filter(appointment ->
                                Boolean.TRUE.equals(appointment.getAvailable()) && to.isAfter(appointment.getTime())
                            )
                            .map(appointment -> new WorkshopAppointment(appointment, workshop))
                            .toList()
                    );
                } else {
                    // Technically dont need this since enum protects us, but just in case for future
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown workshop type");
                }
            } catch (ResourceAccessException e) {
                log.error("Failed to connect to {}", workshop.getUrl());
            }
        }
        workshopAppointments.sort(Comparator.comparing(WorkshopAppointment::getTime));
        return workshopAppointments;
    }

    public WorkshopAppointment bookAppointment(String uuid, String name, String contactInfo) {
        log.info("Request to book appointment name: {}, uuid: {}, contactInfo: {}", uuid, name, contactInfo);
        List<WorkshopProperties.Workshop> workshops = filterWorkShops(null, null, name);
        if (workshops.size() != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error finding workshop by name: " + name);
        }
        WorkshopProperties.Workshop workshop = workshops.get(0);
        try {
            if (workshop.getType().equals(WorkshopTypeEnum.XML)) {
                return new WorkshopAppointment(
                    xmlClient.bookAppointment(workshop.getUrl(), uuid, new TireChangeBookingRequest(contactInfo)),
                    workshop
                );
            } else if (workshop.getType().equals(WorkshopTypeEnum.JSON)) {
                return new WorkshopAppointment(
                    jsonClient.bookAppointment(workshop.getUrl(), uuid, new TireChangeBookingRequest(contactInfo)),
                    workshop
                );
            } else {
                // Technically dont need this since enum protects us, but just in case for future
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown workshop type");
            }
        } catch (HttpClientErrorException e) {
            if (
                e.getStatusCode().equals(HttpStatus.UNPROCESSABLE_ENTITY) ||
                e.getStatusCode().equals(HttpStatus.BAD_REQUEST)
            ) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Appointment already booked or not found with id: " + uuid
                );
            }
            throw e;
        }
    }

    /**
     * In a proper application you would store workshop values in a database and use queries to filter them
     */
    private List<WorkshopProperties.Workshop> filterWorkShops(String address, String car, String name) {
        return workshopProperties
            .getWorkshops()
            .stream()
            .filter(workshop ->
                (
                    car == null ||
                    workshop
                        .getCars()
                        .stream()
                        .anyMatch(workshopCar -> workshopCar.toLowerCase().contains(car.toLowerCase()))
                ) &&
                (address == null || workshop.getAddress().toLowerCase().contains(address.toLowerCase())) &&
                (name == null || workshop.getName().equalsIgnoreCase(name))
            )
            .toList();
    }
}
