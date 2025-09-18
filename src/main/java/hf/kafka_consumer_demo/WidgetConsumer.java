package hf.kafka_consumer_demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WidgetConsumer extends CountingConsumer<Widget> {

  @KafkaListener(
      topics = "widgets",
      groupId = "widget-consumers",
      //          errorHandler = "noopHandler",
      concurrency = "1")
  public void consume(Widget widget) {
    log.info("Consuming widget: {}", widget);
//    throwSometimes(widget);
//    throwOn7(widget);
    throwOnNegativePrice(widget);
    count(widget);
  }

  public static void throwOn7(Widget widget) {
    if (widget.id() == 7) {
      throw new SevenException("Simulated exception on widget id 7");
    }
  }

  public static void throwOnNegativePrice(Widget widget) {
    if (widget.price() < 0) {
      throw new NegativePriceException("Negative price for widget " + widget.id());
    }
  }

  public static void throwSometimes(Widget widget) {
    if (Math.random() < 0.5) {
      throw new RuntimeException("Simulated random exception on widget " + widget.id());
    }
  }

  public static class NegativePriceException extends RuntimeException {
    public NegativePriceException(String message) {
      super(message);
    }
  }

  public static class SevenException extends RuntimeException {
    public SevenException(String message) {
      super(message);
    }
  }
}
