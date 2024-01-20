package tire.workshop.dto.json;

import java.time.Instant;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AppointmentJson {

    private String id;
    private Instant time;
    private Boolean available;
}
