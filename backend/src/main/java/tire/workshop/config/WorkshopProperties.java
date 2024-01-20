package tire.workshop.config;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "clients")
public class WorkshopProperties {

    private List<Workshop> workshops;

    @Setter
    @Getter
    @Builder
    public static class Workshop {

        private String url;
        private String address;
        private List<String> cars;
        private String name;
        private WorkshopTypeEnum type;
    }
}
