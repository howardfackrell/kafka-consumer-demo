kafka-consumer-demo
--
Playground to experiment with Error Handling in Kafka Consumers using Spring Kafka

# Well-behaved Kafka Consumers

* Idempotency (replayable)
    * Kafka guarantees "at-least-once" delivery, so your consumer must be able to handle duplicate messages
    * You should not count on your consumer never having a problem. It will. Being able to replay messages is a key part
      of being able to recover from problems
* Independently stoppable/startable
    * Can you stop your consumer without stopping the whole app?
    * Can you stop one consumer but leave others running?
* Unique consumer-group-names
    * You've used the akkeris app name as the consumer-group name for both topic A and B. It's been working great. Now
      you don't need topic B anmore. **Go unsubscribe from B. In prod. I dare you**
* Appropriate scalability
    * No 1 size fits all for scaling consumers
    * Can you scale up with multiple threads in a single JVM?
    * Can you scale the topic A consumer independently of topic B? Do you need to?
* 'Good' Error handling
    * No 1 size fits all for error handling. Good error handling is specific to your use case
    * It can be easy to forget about and it will go unnoticed until it's an emergency
    * Retrying is usually a good idea, but retrying indefinitely is almost never the right answer
        * At some point you'll need to ignore or alert when retry is not working. **Which is it?**
    * Retries work when the error is transient (network blip, downstream service temporarily unavailable, etc). Doing
      all your retries in quick succession might not give time for the transient error to resolve
    * infinite retries with no delay can eat a ton of resources really quickly

# Understanding Spring Kafka Error Handling

Spring has great documentation
on [error handling for Spring Kafka](https://docs.spring.io/spring-kafka/reference/kafka/annotation-error-handling.html)

There are 2 ways of changing how errors are handled.

The first is to set an `errorHandler` on the `@KafkaListener` annotation. This is where you can handle the error for a
single retry attempt. You can't change the number of attempts, the BackOff, or the behavior if all retry attempts fail
with this, but you can perform additional logging or short-circuit the retry attempts if you want.

```java

@KafkaListener(
        topics = "widgets",
        groupId = "widget-consumers",
        errorHandler = "noopHandler",
        concurrency = "1")
public void consume(Widget widget) {
    // process the widget
}
  }
```

The other way to change error handling is to customize the ContainerFactory of the @KafkaListener. There are a few ways
to do this. In this app there is an @Bean defining the DefaultErrorHandler instance and Spring will autowire that into
the ContainerFactory.

This is where you can configure a a BackOff policy and the ConsumerRecordRecoverer which will be used when all the
BackOff attempts have failed, or when a non-retryable exception is thrown.

```java
import org.springframework.kafka.listener.DefaultErrorHandler;

@Bean
public DefaultErrorHandler defaultErrorHandler() {
    // customize the DefaultErrorHandler here
    return new DefaultErrorHandler();
}
```

## Experimenting with Error Handling

The WidgetConsumerTest has 3 test methods

* testConsumeNoErrors()
* testConsumeWithProcessingError()
* testConsumeWithParseError()

You can tweak the DefaultErrorHandler in KafkaConsumerDemoApplication and the @KafkaListener annotation in
WidgetConsumer to change the error handling, and then run the Test class to see what happens

There are a couple of implementations of KafkaListenerErrorHandler that can be used with the annotation, and you can
create additional ones to experiment with.

The WidgetConsumer has a couple of simple methods that will check for certain conditions and throw specific typed
exceptions so you can experiment with mixing recoverable and non-recoverable Exceptions.

The WidgetProducer has a method to produce a message that is not a widget at all to test deserialization errors

Read the docs, ask yourself questions, experiment and mold the kafka error handling to your whims.

## Some questions to get started

* With no errorHandler or DefaultErrorHandler customizations, what happens when a processing error occurs?
* With no errorHandler or DefaultErrorHandler customizations, what happens when deserialization error occurs?
* Can all the messages still get processed if there is a high probability of processing errors? Use the throwSometimes()
  method in the WidgetConsumer
* What's different if you use the NoopErrorHandler vs the LoggingErrorHandler?
* What happens if you throw a non-retryable exception from the errorHandler method?
* What happens if you set the maxAttempts to 1 in the DefaultErrorHandler?
* What happens if you set the BackOff to a FixedBackOff with a long delay?
* What happens if you set the BackOff to a ExponentialBackOff?
* How would you retry a message indefinitely until it succeeds?
* How would you skip a failing message, but send an alert to PagerDuty that a message was skipped?
* How would you alert PagerDuty if more than N messages can not be successfully processed in Y minutes? 