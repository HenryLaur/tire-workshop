package tire.workshop.dto;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import tire.workshop.config.WorkshopProperties;
import tire.workshop.dto.json.AppointmentJson;
import tire.workshop.dto.xml.AppointmentXml;

@Data
@Builder
@AllArgsConstructor
public class WorkshopAppointment {

    private String uuid;
    private Instant time;
    private List<String> cars;
    private String address;
    private String name;

    public WorkshopAppointment(AppointmentXml appointmentXML, WorkshopProperties.Workshop workshop) {
        this.uuid = appointmentXML.getUuid();
        this.time = appointmentXML.getTime();
        this.cars = workshop.getCars();
        this.address = workshop.getAddress();
        this.name = workshop.getName();
    }

    public WorkshopAppointment(AppointmentJson appointmentJson, WorkshopProperties.Workshop workshop) {
        this.uuid = appointmentJson.getId();
        this.time = appointmentJson.getTime();
        this.cars = workshop.getCars();
        this.address = workshop.getAddress();
        this.name = workshop.getName();
    }
}
