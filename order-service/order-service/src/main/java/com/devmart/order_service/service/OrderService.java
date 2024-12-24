package com.devmart.order_service.service;

import com.devmart.order_service.dto.InventoryResponse;
import com.devmart.order_service.dto.OrderItemDto;
import com.devmart.order_service.dto.OrderRequest;
import com.devmart.order_service.event.OrderPlacedEvent;
import com.devmart.order_service.model.Order;
import com.devmart.order_service.model.OrderItem;
import com.devmart.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;


import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderItem> list = orderRequest.getOrderItemDtoList()
                .stream()
                .map(orderItemDto -> mapToDto(orderItemDto)).toList();

        order.setOrderItemList(list);

        List<String> skuCodes = order.getOrderItemList().stream()
                                    .map(orderItem -> orderItem.getSkuCode()).toList();

        //check in the inventory
        InventoryResponse[] inventoryResponses = webClient.get().uri("http://localhost:8082/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        boolean allProductIsInStock = Arrays.stream(inventoryResponses)
                            .allMatch(inventoryResponse -> inventoryResponse.isInStock());

        if (allProductIsInStock) {
            orderRepository.save(order);
            kafkaTemplate.send("notificationTopic", new OrderPlacedEvent(order.getOrderNumber()));
        } else {
            throw new IllegalArgumentException("Product is not in stock!!");
        }
    }

    private OrderItem mapToDto(OrderItemDto orderItemDto) {
        OrderItem orderItem = new OrderItem();
        orderItem.setPrice(orderItemDto.getPrice());
        orderItem.setSkuCode(orderItemDto.getSkuCode());
        orderItem.setQuantity(orderItemDto.getQuantity());
        return orderItem;
    }
}
