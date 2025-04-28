package com.live_commerce.coupon.infrastructure.config;

import com.live_commerce.coupon.infrastructure.kafka.dto.CouponUsedMessage;
import com.live_commerce.coupon.infrastructure.kafka.dto.FirstJoinCouponMessage;
import java.util.Map;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@EnableKafka
public class KafkaConfig {

  public static final String FIRST_COUPON_TOPIC = "first-coupon-topic";
  public static final String COUPON_USED_TOPIC = "coupon-used-topic";

  private final KafkaProperties props;

  public KafkaConfig(KafkaProperties props) {
    this.props = props;
  }

  @Bean
  public NewTopic firstCouponTopic() {
    return TopicBuilder.name(FIRST_COUPON_TOPIC)
        .partitions(3)
        .replicas(1)
        .build();
  }

  @Bean
  public NewTopic couponUsedTopic() {
    return TopicBuilder.name(COUPON_USED_TOPIC)
        .partitions(3)
        .replicas(1)
        .build();
  }

  @Bean
  ProducerFactory<String, Object> producerFactory() {
    Map<String, Object> config = props.buildProducerProperties();
    config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return new DefaultKafkaProducerFactory<>(config);
  }

  @Bean
  public KafkaTemplate<String, Object> kafkaTemplate(
      ProducerFactory<String, Object> producerFactory) {
    return new KafkaTemplate<>(producerFactory);
  }

  @Bean
  public ConsumerFactory<String, Object> consumerFactory() {
    JsonDeserializer<Object> deser = new JsonDeserializer<>(Object.class)
        .trustedPackages("*");
    Map<String, Object> cfg = props.buildConsumerProperties();
    return new DefaultKafkaConsumerFactory<>(
        cfg,
        new StringDeserializer(),
        deser
    );
  }

  private <T> ConcurrentKafkaListenerContainerFactory<String, T>
  createListenerFactory(Class<T> payloadType) {
    JsonDeserializer<T> deserializer = new JsonDeserializer<>(payloadType).trustedPackages("*");

    DefaultKafkaConsumerFactory<String, T> config = new DefaultKafkaConsumerFactory<>(
        props.buildConsumerProperties(),
        new StringDeserializer(),
        deserializer
    );

    ConcurrentKafkaListenerContainerFactory<String, T> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(config);
    return factory;
  }


  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, FirstJoinCouponMessage>
  firstJoinListenerContainerFactory(){
    return createListenerFactory(FirstJoinCouponMessage.class);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, CouponUsedMessage>
  couponUsedListenerContainerFactory(){
    return createListenerFactory(CouponUsedMessage.class);
  }
}
