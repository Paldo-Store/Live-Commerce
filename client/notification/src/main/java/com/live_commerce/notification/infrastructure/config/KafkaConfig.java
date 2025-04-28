package com.live_commerce.notification.infrastructure.config;

import org.springframework.kafka.support.serializer.JsonDeserializer;
import com.live_commerce.notification.application.dto.NotificationMessage;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
@EnableKafka
public class KafkaConfig {

  private final KafkaProperties props;
  public KafkaConfig(KafkaProperties props) {
    this.props = props;
  }

  @Bean
  public ProducerFactory<String, NotificationMessage> producerFactory(){
    Map<String, Object> config = props.buildProducerProperties();
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.springframework.kafka.support.serializer.JsonSerializer.class);
    return new DefaultKafkaProducerFactory<>(config);
  }

  @Bean
  public KafkaTemplate<String, NotificationMessage> kafkaTemplate(){
    return new KafkaTemplate<>(producerFactory());
  }

  @Bean
  public ConsumerFactory<String, NotificationMessage> consumerFactory() {
    JsonDeserializer<NotificationMessage> deserializer =
        new JsonDeserializer<>(NotificationMessage.class).trustedPackages("*");
    Map<String, Object> config = props.buildConsumerProperties();
    return new DefaultKafkaConsumerFactory<>(
        config, new StringDeserializer(), deserializer);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, NotificationMessage> kafkaListenerContainerFactory(){
    ConcurrentKafkaListenerContainerFactory<String, NotificationMessage> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory());
    return factory;
  }

}