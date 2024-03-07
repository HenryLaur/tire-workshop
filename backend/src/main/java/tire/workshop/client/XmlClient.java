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
import tire.workshop.dto.xml.AppointmentXml;
import tire.workshop.dto.xml.TireChangeBookingRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class XmlClient {

    private final RestTemplate restTemplate;
    private final DateTimeFormatter formatter = DateTimeFormatter
        .ofPattern(DATE_PATTERN)
        .withZone(ZoneId.systemDefault());

    public List<AppointmentXml> findAppointments(String url, Instant from, Instant to) {
        String urlTemplate = UriComponentsBuilder
            .fromHttpUrl(url + "tire-change-times/available")
            .queryParam("from", formatter.format(from))
            .queryParam("until", formatter.format(to))
            .toUriString();

        ResponseEntity<List<AppointmentXml>> response = performExchange(
            urlTemplate,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<AppointmentXml>>() {}
        );
        return response.getBody() == null ? List.of() : response.getBody();
    }

    public AppointmentXml bookAppointment(String url, String uuid, TireChangeBookingRequest request) {
        String urlTemplate = UriComponentsBuilder
            .fromHttpUrl(url + "tire-change-times/" + uuid + "/booking")
            .toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);

        ResponseEntity<AppointmentXml> response = performExchange(
            urlTemplate,
            HttpMethod.PUT,
            new HttpEntity<>(request, headers),
            new ParameterizedTypeReference<AppointmentXml>() {}
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
