package tire.workshop.client;

import static tire.workshop.config.Constants.DATE_PATTERN;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tire.workshop.dto.json.AppointmentJson;
import tire.workshop.dto.xml.TireChangeBookingRequest;

@Component
@RequiredArgsConstructor
public class JsonClient {

    private final RestTemplate restTemplate;
    private final DateTimeFormatter formatter = DateTimeFormatter
        .ofPattern(DATE_PATTERN)
        .withZone(ZoneId.systemDefault());

    public List<AppointmentJson> findAppointments(String url, Instant from) {
        String urlTemplate = UriComponentsBuilder
            .fromHttpUrl(url + "tire-change-times")
            .queryParam("from", formatter.format(from))
            .toUriString();

        ResponseEntity<List<AppointmentJson>> response = restTemplate.exchange(
            urlTemplate,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {}
        );

        return response.getBody() == null ? List.of() : response.getBody();
    }

    public AppointmentJson bookAppointment(String url, String uuid, TireChangeBookingRequest request) {
        String urlTemplate = UriComponentsBuilder
            .fromHttpUrl(url + "tire-change-times/" + uuid + "/booking")
            .toUriString();
        ResponseEntity<AppointmentJson> response = restTemplate.exchange(
            urlTemplate,
            HttpMethod.POST,
            new HttpEntity<>(request),
            AppointmentJson.class
        );
        return response.getBody();
    }
}
