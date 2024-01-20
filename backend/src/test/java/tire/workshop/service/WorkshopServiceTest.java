package tire.workshop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;
import tire.workshop.client.JsonClient;
import tire.workshop.client.XmlClient;
import tire.workshop.config.WorkshopProperties;
import tire.workshop.config.WorkshopTypeEnum;
import tire.workshop.dto.WorkshopAppointment;
import tire.workshop.dto.json.AppointmentJson;
import tire.workshop.dto.xml.AppointmentXml;

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
    JsonClient jsonClient;

    @Mock
    XmlClient xmlClient;

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
        when(jsonClient.findAppointments(any(), any())).thenReturn(new ArrayList<>());
        workshopService.findAppointments(Instant.now(), Instant.now(), null, CAR_TYPE_TRUCK);
        verify(jsonClient, times(1)).findAppointments(any(), any());
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
        when(jsonClient.findAppointments(any(), any())).thenReturn(new ArrayList<>());
        workshopService.findAppointments(Instant.now(), Instant.now(), ADDRESS_MANCHESTER, null);
        verify(jsonClient, times(1)).findAppointments(any(), any());
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
        when(jsonClient.findAppointments(any(), any()))
            .thenReturn(
                List.of(
                    AppointmentJson.builder().id(ID_1).available(true).time(APPOINTMENT_TIME).build(),
                    AppointmentJson.builder().id(ID_2).available(false).time(APPOINTMENT_TIME).build()
                )
            );
        List<WorkshopAppointment> appointments = workshopService.findAppointments(
            Instant.now(),
            Instant.now().plus(1, ChronoUnit.DAYS),
            null,
            null
        );
        verify(jsonClient, times(1)).findAppointments(any(), any());
        assertEquals(appointments.size(), 1);
        WorkshopAppointment workshopAppointment = appointments.get(0);
        assertEquals(workshopAppointment.getTime(), APPOINTMENT_TIME);
        assertEquals(workshopAppointment.getUuid(), ID_1);
        assertEquals(workshopAppointment.getName(), WORKSHOP_NAME);
        assertEquals(workshopAppointment.getCars(), List.of(CAR_TYPE_CAR));
        assertEquals(workshopAppointment.getAddress(), ADDRESS_LONDON);
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
        when(xmlClient.findAppointments(any(), any(), any()))
            .thenReturn(List.of(AppointmentXml.builder().uuid(ID_1).time(APPOINTMENT_TIME).build()));
        List<WorkshopAppointment> appointments = workshopService.findAppointments(
            Instant.now(),
            Instant.now().plus(1, ChronoUnit.DAYS),
            null,
            null
        );
        verify(xmlClient, times(1)).findAppointments(any(), any(), any());
        assertEquals(appointments.size(), 1);
        WorkshopAppointment workshopAppointment = appointments.get(0);
        assertEquals(workshopAppointment.getTime(), APPOINTMENT_TIME);
        assertEquals(workshopAppointment.getUuid(), ID_1);
        assertEquals(workshopAppointment.getName(), WORKSHOP_NAME);
        assertEquals(workshopAppointment.getCars(), List.of(CAR_TYPE_CAR));
        assertEquals(workshopAppointment.getAddress(), ADDRESS_LONDON);
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
        verify(xmlClient, never()).findAppointments(any(), any(), any());
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
        verify(xmlClient, never()).findAppointments(any(), any(), any());
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
        when(xmlClient.bookAppointment(any(), any(), any()))
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
        verify(xmlClient, never()).findAppointments(any(), any(), any());
    }
}
