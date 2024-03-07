package tire.workshop.client;

import static tire.workshop.config.Constants.DATE_PATTERN;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import tire.workshop.dto.json.AppointmentJson;
import tire.workshop.dto.xml.TireChangeBookingRequest;

@Slf4j
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

        ResponseEntity<List<AppointmentJson>> response = performExchange(
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
        ResponseEntity<AppointmentJson> response = performExchange(
            urlTemplate,
            HttpMethod.POST,
            new HttpEntity<>(request),
            new ParameterizedTypeReference<AppointmentJson>() {}
        );
        return response.getBody();
    }

    private <T> ResponseEntity<T> performExchange(
        String url,
        HttpMethod method,
        HttpEntity<?> entity,
        ParameterizedTypeReference<T> responseType
    ) {
        try {
            return restTemplate.exchange(url, method, entity, responseType);
        } catch (ResourceAccessException e) {
            log.error("Failed to connect to {}", url);
            throw e; // Consider a custom exception
        }
    }
}
