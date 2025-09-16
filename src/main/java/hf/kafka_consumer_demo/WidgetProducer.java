package hf.kafka_consumer_demo;

import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class WidgetProducer {

  private final KafkaTemplate<Integer, Widget> kafkaTemplate;
  private final KafkaTemplate<Integer, PoisonPill> poison;
  private final String topic = "widgets";

  public void produce(Widget widget) {
    kafkaTemplate.send(topic, widget.id(), widget);
  }

  public void producePoison() {
    poison.send(topic, -1, PoisonPill.INSTANCE);
  }

  public static record PoisonPill(String name) {
    public static final PoisonPill INSTANCE = new PoisonPill("POISON_PILL");
  }
}
