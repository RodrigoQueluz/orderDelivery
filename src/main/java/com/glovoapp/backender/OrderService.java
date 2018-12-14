package com.glovoapp.backender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static java.util.Comparator.*;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class OrderService {

	private static final String DISTANCE_PRIORITY = "distance";
	private static final String FOOD_PRIORITY = "food";
	private static final String VIP_PRIORITY = "vip";
	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private CourierRepository courierRepository;
	@Autowired
	private CourierService courierService;

	@Value("${slot.size}")
	private double slotSize;
	
	@Value("#{'${priority.list}'.split(',')}")
	private List<String> listOfPriorities;

	private List<Order> orders = new ArrayList<>();
	private Courier courier = new Courier();
	private Map<String,Comparator<Order>> comparators=new HashMap<>();
	
	public OrderService() {
		createMapOfComparators();
	}


	public List<Order> fetchOrders(String courierId) {
		this.courier = courierRepository.findById(courierId);
		this.orders = orderRepository.findAll();

		return this.findSlotsOfOrders(courier);

	}

	public List<Order> findSlotsOfOrders(Courier courier) {
		return separateOrdersInSlot();
	}

	private List<Order> separateOrdersInSlot() {
		
		return this.orders
				.stream()
				.collect(Collectors.groupingBy(order -> (slotIndex(order, courier))))
				.values()
					.stream()
					.flatMap(listOfOrders -> prioritiseOrders(listOfOrders)
							.stream())
					.collect(Collectors.toList());
	}
	
	private Long slotIndex(Order order, Courier courier) {
		return (long) (DistanceCalculator.calculateDistance(courier.getLocation(), order.getPickup()) / slotSize);
	}

	private List<Order> prioritiseOrders(List<Order> listOfOrders) {
		
		List<Order> result = listOfOrders
					.stream()
					.filter((order) -> checkIfCourierHasBoxAndCanTravelDistance(order, courier))
					.sorted(createOrderOfPriorities())
					.collect(Collectors.toList());
		
		for(Order order : result) {
    		System.out.println("VIP -> " + order.getVip() + " | FOOD -> " + order.getFood() + " | D -> " + DistanceCalculator.calculateDistance(courier.getLocation(), order.getPickup()));
    	}
		
		System.out.println("----------------------");
		
		return result;

	}
	
	private boolean checkIfCourierHasBoxAndCanTravelDistance(Order order, Courier courier) {
		return courierService.checkIfCourierHasBoxAndCanTravelDistance(courier, order);
	}

	private Comparator<Order> createOrderOfPriorities() {
		
		Comparator<Order> finalOrderComparator = isThereAnyPrioritySet() ? comparators.get(listOfPriorities.get(0)) : comparators.get(DISTANCE_PRIORITY);
		
		for(String priority : listOfPriorities) {
			if(listOfPriorities.indexOf(priority) > 0) {
				finalOrderComparator = finalOrderComparator.thenComparing(comparators.get(priority));
			}
		};
		
		return finalOrderComparator.thenComparing(comparators.get(DISTANCE_PRIORITY));

	}

	private boolean isThereAnyPrioritySet() {
		return listOfPriorities.size() > 0;
	}

	private void createMapOfComparators() {
		Comparator<Order> orderVIPComparator = comparing(Order::getVip).reversed();
		Comparator<Order> orderFoodComparator = comparing(Order::getFood).reversed();
		Comparator<Order> orderDistanceComparator = comparingDouble(
				order -> DistanceCalculator.calculateDistance(courier.getLocation(), order.getPickup()));
		
		comparators.put(VIP_PRIORITY, orderVIPComparator);
		comparators.put(FOOD_PRIORITY, orderFoodComparator);
		comparators.put(DISTANCE_PRIORITY, orderDistanceComparator);
		
		comparators = Collections.unmodifiableMap(comparators);
	}
}
