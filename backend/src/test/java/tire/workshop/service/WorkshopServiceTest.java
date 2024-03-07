package tire.workshop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import tire.workshop.config.WorkshopProperties;
import tire.workshop.config.WorkshopTypeEnum;
import tire.workshop.dto.WorkshopAppointment;

@ExtendWith(MockitoExtension.class)
class WorkshopServiceTest {

    public static final Instant APPOINTMENT_TIME = Instant.now().plus(1, ChronoUnit.MINUTES);
    public static final String CAR_TYPE_TRUCK = "Truck";
    public static final String CAR_TYPE_CAR = "Car";
    public static final String ADDRESS_LONDON = "London";
    public static final String ADDRESS_MANCHESTER = "Manchester";
    public static final String WORKSHOP_NAME = "Name";
    public static final String ID_1 = "1";
    public static final String ID_2 = "2";

    @InjectMocks
    private WorkshopService workshopService;

    @Mock
    XmlClientStrategy xmlClientStrategy;

    @Mock
    JsonClientStrategy jsonClientStrategy;

    @Mock
    Map<WorkshopTypeEnum, WorkShopClientStrategy> strategies;

    @Mock
    private WorkshopProperties workshopProperties;

    @Test
    void checkIfNoWorkshopsAreFoundEmptyListIsReturned() {
        when(workshopProperties.getWorkshops()).thenReturn(new ArrayList<>());
        List<WorkshopAppointment> appointments = workshopService.findAppointments(
            Instant.now(),
            Instant.now(),
            null,
            null
        );
        assertTrue(appointments.isEmpty());
    }

    @Test
    void checkIfCanFilterByCars() {
        when(workshopProperties.getWorkshops())
            .thenReturn(
                List.of(
                    WorkshopProperties.Workshop
                        .builder()
                        .type(WorkshopTypeEnum.JSON)
                        .cars(List.of(CAR_TYPE_TRUCK))
                        .build(),
                    WorkshopProperties.Workshop
                        .builder()
                        .type(WorkshopTypeEnum.JSON)
                        .cars(List.of(CAR_TYPE_CAR))
                        .build()
                )
            );
        when(jsonClientStrategy.findAppointments(any(), any(), any())).thenReturn(new ArrayList<>());
        when(strategies.get(WorkshopTypeEnum.JSON)).thenReturn(jsonClientStrategy);
        workshopService.findAppointments(Instant.now(), Instant.now(), null, CAR_TYPE_TRUCK);
        verify(jsonClientStrategy, times(1)).findAppointments(any(), any(), any());
    }

    @Test
    void checkIfCanFilterByAddress() {
        when(workshopProperties.getWorkshops())
            .thenReturn(
                List.of(
                    WorkshopProperties.Workshop.builder().type(WorkshopTypeEnum.JSON).address(ADDRESS_LONDON).build(),
                    WorkshopProperties.Workshop
                        .builder()
                        .type(WorkshopTypeEnum.JSON)
                        .address(ADDRESS_MANCHESTER)
                        .build()
                )
            );
        when(strategies.get(WorkshopTypeEnum.JSON)).thenReturn(jsonClientStrategy);
        when(jsonClientStrategy.findAppointments(any(), any(), any())).thenReturn(new ArrayList<>());
        workshopService.findAppointments(Instant.now(), Instant.now(), ADDRESS_MANCHESTER, null);
        verify(jsonClientStrategy, times(1)).findAppointments(any(), any(), any());
    }

    @Test
    void checkIfJsonResponseIsMappedCorrectly() {
        when(workshopProperties.getWorkshops())
            .thenReturn(
                List.of(
                    WorkshopProperties.Workshop
                        .builder()
                        .type(WorkshopTypeEnum.JSON)
                        .address(ADDRESS_LONDON)
                        .name(WORKSHOP_NAME)
                        .cars(List.of(CAR_TYPE_CAR))
                        .build()
                )
            );
        when(strategies.get(WorkshopTypeEnum.JSON)).thenReturn(jsonClientStrategy);
        when(jsonClientStrategy.findAppointments(any(), any(), any()))
            .thenReturn(
                List.of(
                    WorkshopAppointment.builder().uuid(ID_1).time(APPOINTMENT_TIME).build(),
                    WorkshopAppointment.builder().uuid(ID_2).time(APPOINTMENT_TIME).build()
                )
            );
        List<WorkshopAppointment> appointments = workshopService.findAppointments(
            Instant.now(),
            Instant.now().plus(1, ChronoUnit.DAYS),
            null,
            null
        );
        verify(jsonClientStrategy, times(1)).findAppointments(any(), any(), any());
        assertEquals(appointments.size(), 2);
    }

    @Test
    void checkIfXmlResponseIsMappedCorrectly() {
        when(workshopProperties.getWorkshops())
            .thenReturn(
                List.of(
                    WorkshopProperties.Workshop
                        .builder()
                        .type(WorkshopTypeEnum.XML)
                        .address(ADDRESS_LONDON)
                        .name(WORKSHOP_NAME)
                        .cars(List.of(CAR_TYPE_CAR))
                        .build()
                )
            );
        when(strategies.get(WorkshopTypeEnum.XML)).thenReturn(xmlClientStrategy);
        when(xmlClientStrategy.findAppointments(any(), any(), any()))
            .thenReturn(List.of(WorkshopAppointment.builder().uuid(ID_1).time(APPOINTMENT_TIME).build()));
        List<WorkshopAppointment> appointments = workshopService.findAppointments(
            Instant.now(),
            Instant.now().plus(1, ChronoUnit.DAYS),
            null,
            null
        );
        verify(xmlClientStrategy, times(1)).findAppointments(any(), any(), any());
        assertEquals(appointments.size(), 1);
    }

    @Test
    void checkIfErrorIsThrownWhenTooManyWorkshopsAreFound() {
        when(workshopProperties.getWorkshops())
            .thenReturn(
                List.of(
                    WorkshopProperties.Workshop.builder().type(WorkshopTypeEnum.XML).name(ADDRESS_LONDON).build(),
                    WorkshopProperties.Workshop.builder().type(WorkshopTypeEnum.XML).name(ADDRESS_LONDON).build()
                )
            );
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> workshopService.bookAppointment(null, WORKSHOP_NAME, null)
        );
        assertThat(exception.getMessage())
            .isEqualTo(
                new ResponseStatusException(BAD_REQUEST, "Error finding workshop by name: " + WORKSHOP_NAME)
                    .getMessage()
            );
        verify(xmlClientStrategy, never()).findAppointments(any(), any(), any());
    }

    @Test
    void checkIfErrorIsThrownWhenNoWorkshopsAreFound() {
        when(workshopProperties.getWorkshops()).thenReturn(List.of());
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> workshopService.bookAppointment(null, WORKSHOP_NAME, null)
        );
        assertThat(exception.getMessage())
            .isEqualTo(
                new ResponseStatusException(BAD_REQUEST, "Error finding workshop by name: " + WORKSHOP_NAME)
                    .getMessage()
            );
        verify(xmlClientStrategy, never()).findAppointments(any(), any(), any());
    }

    @Test
    void checkIfErrorIsHandled() {
        when(workshopProperties.getWorkshops())
            .thenReturn(
                List.of(
                    WorkshopProperties.Workshop
                        .builder()
                        .type(WorkshopTypeEnum.XML)
                        .address(ADDRESS_LONDON)
                        .name(WORKSHOP_NAME)
                        .cars(List.of(CAR_TYPE_CAR))
                        .build()
                )
            );
        when(strategies.get(WorkshopTypeEnum.XML)).thenReturn(xmlClientStrategy);
        when(xmlClientStrategy.bookAppointment(any(), any(), any()))
            .thenThrow(new HttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY));
        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> workshopService.bookAppointment(ID_1, WORKSHOP_NAME, null)
        );
        assertThat(exception.getMessage())
            .isEqualTo(
                new ResponseStatusException(BAD_REQUEST, "Appointment already booked or not found with id: " + ID_1)
                    .getMessage()
            );
        verify(xmlClientStrategy, never()).findAppointments(any(), any(), any());
    }
}
