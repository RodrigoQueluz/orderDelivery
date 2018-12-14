package com.glovoapp.backender;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class CourierService {

	@Value("${max.distance}")
	private Double maxDistance;

	@Value("#{'${itens.require.box}'.split(',')}")
	private List<String> itensThatRequiresBoxes;

	public boolean checkIfCourierHasBoxAndCanTravelDistance(Courier courier, Order order) {
		return courierCanTravelDistance(courier, order) && courierHasBox(courier, order);
	}

	public boolean courierCanTravelDistance(Courier courier, Order order) {
		return isNotFurtherThanParametrized(courier, order) || courierHasVehicleNeeded(courier);
	}

	private boolean courierHasVehicleNeeded(Courier courier) {
		return courier.getVehicle().equals(Vehicle.ELECTRIC_SCOOTER) || courier.getVehicle().equals(Vehicle.MOTORCYCLE);
	}

	private boolean isNotFurtherThanParametrized(Courier courier, Order order) {
		return DistanceCalculator.calculateDistance(courier.getLocation(), order.getPickup()) < maxDistance;
	}

	private boolean courierHasBox(Courier courier, Order order) {

		if (courier.getBox()) {
			return itensThatRequiresBoxes.parallelStream().anyMatch(order.getDescription().toLowerCase()::contains);
		} else {
			return itensThatRequiresBoxes.parallelStream()
					.allMatch((item) -> !order.getDescription().toLowerCase().contains(item));
		}

	}

}
