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
import tire.workshop.dto.xml.AppointmentXml;
import tire.workshop.dto.xml.TireChangeBookingRequest;

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

        ResponseEntity<List<AppointmentXml>> response = restTemplate.exchange(
            urlTemplate,
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<>() {}
        );
        return response.getBody() == null ? List.of() : response.getBody();
    }

    public AppointmentXml bookAppointment(String url, String uuid, TireChangeBookingRequest request) {
        String urlTemplate = UriComponentsBuilder
            .fromHttpUrl(url + "tire-change-times/" + uuid + "/booking")
            .toUriString();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_XML);
        ResponseEntity<AppointmentXml> response = restTemplate.exchange(
            urlTemplate,
            HttpMethod.PUT,
            new HttpEntity<>(request, headers),
            AppointmentXml.class
        );
        return response.getBody();
    }
}
