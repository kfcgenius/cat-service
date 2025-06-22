package cat_service.configs;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "session")
@Data
public class SessionConfig {

  private int timeoutDays;
}
