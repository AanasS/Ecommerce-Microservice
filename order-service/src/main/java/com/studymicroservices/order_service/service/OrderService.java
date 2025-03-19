package com.studymicroservices.order_service.service;

import com.studymicroservices.order_service.dto.OrderLineItemsDto;
import com.studymicroservices.order_service.dto.OrderRequest;
import com.studymicroservices.order_service.model.Order;
import com.studymicroservices.order_service.model.OrderLineItem;
import com.studymicroservices.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {
    private  final OrderRepository orderRepository;
    public void placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItem> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToData)
                .toList();
        order.setOrderLineItems(orderLineItems);
    orderRepository.save(order);
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
