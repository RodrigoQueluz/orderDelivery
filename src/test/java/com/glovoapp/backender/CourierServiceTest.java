package com.glovoapp.backender;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
public class CourierServiceTest {
	
	private Order order;
	
	@Value("#{'${itens.require.box}'.split(',')}")
	private List<String> itensThatRequiresBoxes;
	
	@Autowired
	private CourierService courierService;
	
	private List<Order> orders;
	private Courier courierWithBox;
	private Courier courierWithoutBox;

	@BeforeEach
	public void initialize() {
		this.orders = new OrderRepository().findAll();

		assertFalse(orders.isEmpty());

		this.courierWithBox = new CourierRepository().findById("courier-1");
		assertTrue(courierWithBox.getBox());
		
		this.courierWithoutBox = new CourierRepository().findById("courier-2");
		assertFalse(courierWithoutBox.getBox());

		
		this.order = orders.get(0);
	}

	@Test
    @DisplayName("Checking if the Courier can travel the Distance")
	void checkIfCourierCanTravelDistance() {
		assertTrue(courierService.isNotFurtherThanParametrized(courierWithoutBox, order));

	}

	@Test
	void checkIfCourierHasVehicleNeeded() {
		Courier courierWithScooter = new CourierRepository().findById("courier-4");
		assertTrue(courierService.courierHasVehicleNeeded(courierWithoutBox));
		assertTrue(courierService.courierHasVehicleNeeded(courierWithScooter));
	}
	
	@Test
	void checkIfCourierHasNotVehicleNeeded() {
		Courier courierWithBycicle = new CourierRepository().findById("courier-3");
		assertFalse(courierService.courierHasVehicleNeeded(courierWithBycicle));
	}
	
	@Test
	void checkIfCourierCannotCarryItemWithoutBox() {
		assertFalse(courierService.courierHasBox(courierWithoutBox, order));
	}
	
	@Test
	void checkIfCourierCanCarryItemWithoutBox() {
		
		Order orderWithoutItensRequireBox = this.orders.get(1);
	
		assertTrue(itensThatRequiresBoxes.parallelStream().allMatch((item) -> !orderWithoutItensRequireBox.getDescription().toLowerCase().contains(item)));
		
		assertTrue(courierService.courierHasBox(courierWithoutBox, orderWithoutItensRequireBox));
	}
	
	@Test
	void checkIfCourierCanCarryItem() {
		assertTrue(courierService.courierHasBox(courierWithBox, order));
	}
	
	@Test
	void checkIfCourierCanDeliverItem() {
		assertTrue(this.courierService.checkIfCourierHasBoxAndCanTravelDistance(courierWithBox, order));
	}

	@Test
	void checkIfCourierCannotDeliverItem() {
		assertFalse(this.courierService.checkIfCourierHasBoxAndCanTravelDistance(courierWithoutBox, order));
	}
	
}