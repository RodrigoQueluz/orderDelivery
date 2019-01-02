package com.glovoapp.backender;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@PropertySource(value = { "classpath:test.properties" })
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = API.class)
@PropertySource(value = { "classpath:test.properties" })
public class OrderServiceTest {
	
	
	@Value("#{'${itens.require.box}'.split(',')}")
	private List<String> itensThatRequiresBoxes;
	
	@Autowired
	private OrderService orderService;
	private List<Order> orders;
	private Courier courier;

	@BeforeEach
	void initialize() {
		this.orders = new OrderRepository().findAll();

		assertFalse(orders.isEmpty());

		this.courier = new CourierRepository().findById("courier-1");
	}
	
	@Test
	void checkIfPrioritiesAreSet() {
		assertTrue(orderService.isThereAnyPrioritySet());
	}
	
	@Test
	void checkIfReturnListOfOrders() {
		assertTrue(orderService.fetchOrders(courier.getId()).size() > 0);
	}
	
}