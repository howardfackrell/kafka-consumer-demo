package hf.kafka_consumer_demo;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class CountingConsumer<T> {
  private List<T> messageList = new ArrayList<>();

  public int consumedCount() {
    return messageList.size();
  }

  public void waitUntilConsumed(int count, Duration duration) {
    var timeout = Instant.now().plus(duration);
    while (consumedCount() < count && Instant.now().isBefore(timeout)) {
      sleep();
    }
  }

  private static void sleep() {
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  protected void count(T message) {
    messageList.add(message);
  }

  protected void withMessages(Consumer<List<T>> op) {
    op.accept(messageList);
  }

  public void logAllConsumed() {
    withMessages(l -> l.forEach(m -> log.info("Consumed: {}", m)));
  }
}
