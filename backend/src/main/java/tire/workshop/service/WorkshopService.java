package tire.workshop.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import tire.workshop.config.WorkshopProperties;
import tire.workshop.config.WorkshopTypeEnum;
import tire.workshop.dto.WorkshopAppointment;
import tire.workshop.dto.xml.TireChangeBookingRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkshopService {

    private final WorkshopProperties workshopProperties;
    private final Map<WorkshopTypeEnum, WorkShopClientStrategy> strategies;

    public List<WorkshopAppointment> findAppointments(Instant from, Instant to, String address, String car) {
        log.info("Request to find appointments from: {}, to: {}, address: {}, car: {}", from, to, address, car);
        List<WorkshopProperties.Workshop> workshops = filterWorkShops(address, car, null);
        List<WorkshopAppointment> workshopAppointments = new ArrayList<>();
        for (WorkshopProperties.Workshop workshop : workshops) {
            WorkShopClientStrategy strategy = getStrategy(workshop);
            workshopAppointments.addAll(strategy.findAppointments(workshop, from, to));
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
            WorkShopClientStrategy strategy = getStrategy(workshop);
            return strategy.bookAppointment(workshop, uuid, new TireChangeBookingRequest(contactInfo));
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

    private WorkShopClientStrategy getStrategy(WorkshopProperties.Workshop workshop) {
        WorkShopClientStrategy strategy = strategies.get(workshop.getType());
        if (strategy == null) {
            // Technically dont need this since enum protects us, but just in case for future
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown workshop type");
        }
        return strategy;
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
