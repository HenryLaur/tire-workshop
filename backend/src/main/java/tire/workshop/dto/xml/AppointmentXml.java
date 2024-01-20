package tire.workshop.dto.xml;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppointmentXml {

    private String uuid;
    private Instant time;
}
