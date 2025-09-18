package hf.kafka_consumer_demo;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@EmbeddedKafka(topics = "widgets", partitions = 1, ports = 9092, controlledShutdown = true)
@ActiveProfiles("test")
@SpringBootTest
class WidgetConsumerTest {

  @Autowired WidgetConsumer consumer;

  @Autowired WidgetProducer producer;

  @Test
  void testConsumeNoErrors() {
    producer.produce(new Widget(1, "First Widget", 1.23f));
    producer.produce(new Widget(2, "Square Widget", 2.34f));
    producer.produce(new Widget(3, "Round Widget", 3.45f));
    producer.produce(new Widget(4, "Red Widget", 4.56f));
    producer.produce(new Widget(5, "Blue Widget", 5.67f));
    consumer.waitUntilConsumed(5, Duration.ofSeconds(10));
    consumer.logAllConsumed();
  }

  @Test
  void testConsumeWithProcessingError() {
    producer.produce(new Widget(1, "First Widget", 1.23f));
    producer.produce(new Widget(7, "Problem Widget", 1.23f));
    producer.produce(new Widget(2, "Square Widget", 2.34f));
    producer.produce(new Widget(3, "Round Widget", 3.45f));

    consumer.waitUntilConsumed(3, Duration.ofSeconds(10));
    consumer.logAllConsumed();
  }

  @Test
  void testConsumeWithParseError() {
    producer.produce(new Widget(1, "First Widget", 1.23f));
    producer.producePoison();
    producer.produce(new Widget(2, "Square Widget", 2.34f));
    producer.produce(new Widget(3, "Round Widget", 3.45f));

    consumer.waitUntilConsumed(3, Duration.ofSeconds(10));
    consumer.logAllConsumed();
  }
}
