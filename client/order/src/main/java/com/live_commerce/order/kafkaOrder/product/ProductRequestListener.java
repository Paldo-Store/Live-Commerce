package com.live_commerce.order.kafkaOrder.product;

import com.live_commerce.order.infrastructure.client.response.ProductCreateResponseDto;
import com.live_commerce.order.kafkaOrder.product.message.ProductRequestMessage;
import com.live_commerce.order.kafkaOrder.product.message.ProductResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

//product 모듈에 있어야하는 클래스
//상품 요청을 받아서 응답을 보내주는 역할
//상품 요청이 왔을 때 상품 정보를 찾아서 응답해주는 역할
@Component
public class ProductRequestListener {

    private final KafkaTemplate<String, ProductResponseMessage> kafkaTemplate;

    @Value("${kafka.topic.product-response}")
    private String productResponseTopic;

    @Autowired
    private RestTemplate restTemplate;  // RestTemplate을 사용하여 외부 Product 서비스 호출

    public ProductRequestListener(KafkaTemplate<String, ProductResponseMessage> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // 상품 조회 요청을 처리하는 메서드
    @KafkaListener(topics = "product-request", groupId = "product-service")
    public void listenProductRequest(ProductRequestMessage message) {
        // 실제 상품 정보를 조회하는 로직
        ProductCreateResponseDto productDetails = getProductDetails(message.productId());

        // 상품 정보가 조회되지 않으면, 실패 응답을 보낼 수 있습니다.
        if (productDetails == null) {
            // 상품이 존재하지 않으면 응답을 보내지 않거나, 실패 응답을 보낼 수 있음
            return;
        }

        // 응답 메시지 생성
        ProductResponseMessage responseMessage = new ProductResponseMessage(message.productId(), productDetails);

        // 응답 메시지를 product-response 토픽으로 전송
        kafkaTemplate.send(productResponseTopic, message.productId().toString(), responseMessage);
    }

    // 외부 Product 서비스에서 상품 정보를 조회하는 메서드
    private ProductCreateResponseDto getProductDetails(UUID productId) {
        // 외부 Product 서비스의 API를 호출하여 상품 정보를 조회
        String url = "http://product-service/products/" + productId;

        // RestTemplate을 사용하여 API 호출
        ResponseEntity<ProductCreateResponseDto> responseEntity = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,  // 요청 본문이 필요 없으므로 null
                ProductCreateResponseDto.class
        );

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            return responseEntity.getBody();  // 조회된 상품 정보 반환
        } else {
            // 상품 조회 실패 시 null 반환
            return null;
        }
    }
}