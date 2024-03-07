package tire.workshop.config;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tire.workshop.client.JsonClient;
import tire.workshop.client.XmlClient;
import tire.workshop.service.JsonClientStrategy;
import tire.workshop.service.WorkShopClientStrategy;
import tire.workshop.service.XmlClientStrategy;

@Configuration
@RequiredArgsConstructor
public class ClientStrategyConfig {

    private final XmlClient xmlClient;
    private final JsonClient jsonClient;

    @Bean
    public Map<WorkshopTypeEnum, WorkShopClientStrategy> getStragegyMap() {
        Map<WorkshopTypeEnum, WorkShopClientStrategy> strategyMap = new HashMap<>();
        strategyMap.put(WorkshopTypeEnum.JSON, new JsonClientStrategy(jsonClient));
        strategyMap.put(WorkshopTypeEnum.XML, new XmlClientStrategy(xmlClient));
        return strategyMap;
    }
}
