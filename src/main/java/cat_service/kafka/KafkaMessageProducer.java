package cat_service.kafka;

import cat_service.constants.KafkaTopic;
import cat_service.dto.KafkaBotDto;
import cat_service.dto.KafkaServiceDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaMessageProducer {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  public void sendToServiceTopic(KafkaServiceDto message) {
    try {
      var messageJson = objectMapper.writeValueAsString(message);
      kafkaTemplate.send(KafkaTopic.SERVICE, messageJson);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void sendToBotTopic(KafkaBotDto message) {
    try {
      var messageJson = objectMapper.writeValueAsString(message);
      kafkaTemplate.send(KafkaTopic.BOT, messageJson);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
