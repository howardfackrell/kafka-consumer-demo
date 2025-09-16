package hf.kafka_consumer_demo;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.KafkaConsumerBackoffManager;
import org.springframework.kafka.listener.RetryListener;
import org.springframework.kafka.retrytopic.ListenerContainerFactoryConfigurer;
import org.springframework.util.backoff.ExponentialBackOff;

import java.util.Map;

@Slf4j
@SpringBootApplication
public class KafkaConsumerDemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(KafkaConsumerDemoApplication.class, args);
  }

  @Bean
  public KafkaTemplate<Integer, Widget> kafkaTemplate(KafkaProperties kafkaProperties) {

    DefaultKafkaProducerFactory<Integer, Widget> factory =
        new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties());
    return new KafkaTemplate<>(factory);
  }

  @Bean
  public KafkaTemplate<Integer, WidgetProducer.PoisonPill> poison(KafkaProperties kafkaProperties) {
    DefaultKafkaProducerFactory<Integer, WidgetProducer.PoisonPill> factory =
        new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties());
    return new KafkaTemplate<>(factory);
  }

//  @Bean
  public DefaultErrorHandler defaultErrorHandler() {
    var defaultErrorHandler =
        new DefaultErrorHandler(
            (record, exception) -> {
              log.info("Not going to be able to recover from exception");
            },
            new ExponentialBackOff(500L, 2.0));
    defaultErrorHandler.setRetryListeners(
        new RetryListener() {
          @Override
          public void failedDelivery(
              ConsumerRecord<?, ?> record, Exception ex, int deliveryAttempt) {
            log.warn("Failed delivery of record {} on attempt {}", record, deliveryAttempt);
          }
        });
    defaultErrorHandler.addNotRetryableExceptions(
        WidgetConsumer.NegativePriceException.class, WidgetConsumer.SevenException.class);
    return defaultErrorHandler;
  }
}
