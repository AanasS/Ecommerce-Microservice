package com.studymicroservices.order_service.service;

import com.studymicroservices.order_service.dto.InventoryResponse;
import com.studymicroservices.order_service.dto.OrderLineItemsDto;
import com.studymicroservices.order_service.dto.OrderRequest;
import com.studymicroservices.order_service.model.Order;
import com.studymicroservices.order_service.model.OrderLineItem;
import com.studymicroservices.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient webClient;

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItem> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToData)
                .toList();
        order.setOrderLineItems(orderLineItems);
//          Call Inventory service and place order if the product in the stock
        List<String> skuCodesList = order.getOrderLineItems().stream()
                .map(OrderLineItem::getSkuCode).toList();
        InventoryResponse[] inventoryResponses = webClient.get()
                .uri("http://localhost:8082/api/inventory"
                        , uriBuilder -> uriBuilder.queryParam("skuCode", skuCodesList).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();
        boolean allProductsInStock = Arrays.stream(inventoryResponses).allMatch(InventoryResponse::isInStock);
        if (allProductsInStock) {
            orderRepository.save(order);
        } else {
            throw new IllegalArgumentException("Product is not in stock, Please try again later");
        }
        log.info("Order Saved");

    }

    private OrderLineItem mapToData(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItem orderLineItem = new OrderLineItem();
        orderLineItem.setPrice(orderLineItemsDto.getPrice());
        orderLineItem.setSkuCode(orderLineItemsDto.getSkuCode());
        orderLineItem.setQuantity(orderLineItemsDto.getQuantity());
        return orderLineItem;
    }


}
