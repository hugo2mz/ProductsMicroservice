package pe.joedayz.ws.products.service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import pe.joedayz.ws.core.ProductCreatedEvent;
import pe.joedayz.ws.products.rest.CreateProductRestModel;

/**
 * @author josediaz
 **/
@Service
public class ProductServiceImpl implements ProductService {

  KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate;
  private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

  public ProductServiceImpl(KafkaTemplate<String, ProductCreatedEvent> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @Override
  public String createProduct(CreateProductRestModel createProductRestModel) throws Exception {
    String productId = UUID.randomUUID().toString();
    //TODO persistir en la BD antes de enviar el evento
    ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent(productId,
        createProductRestModel.getTitle(), createProductRestModel.getPrice(),
        createProductRestModel.getQuantity());

    // SINCRONO
//    LOGGER.info("Antes de enviar el evento ProductCreatedEvent");
//    SendResult<String, ProductCreatedEvent> result = kafkaTemplate.send(
//        "product-created-events-topic", productId, productCreatedEvent).get();
//
//    LOGGER.info("Partition: " + result.getRecordMetadata().partition());
//    LOGGER.info("Topic: " + result.getRecordMetadata().topic());
//    LOGGER.info("Offset: " + result.getRecordMetadata().offset());
//
//    LOGGER.info("Returnando product Id");

    //ASINCRONO
    CompletableFuture<SendResult<String, ProductCreatedEvent>> future =
        kafkaTemplate.send("product-created-events-topic", productId, productCreatedEvent);

    future.whenComplete((result, exception) -> {
      if(exception != null) {
        LOGGER.error("******* Failed to send message: " + exception.getMessage());
      }else{
        LOGGER.info("Partition: " + result.getRecordMetadata().partition());
        LOGGER.info("Topic: " + result.getRecordMetadata().topic());
        LOGGER.info("Offset: " + result.getRecordMetadata().offset());
      }
    });

    future.join();

    LOGGER.info("******* Returning product id");

    return productId;
  }
}
