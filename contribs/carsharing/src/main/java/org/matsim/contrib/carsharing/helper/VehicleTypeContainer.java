package org.matsim.contrib.carsharing.helper;

import java.util.HashMap;
import java.util.Map;

public class VehicleTypeContainer {
	private Map<String, CustomVehicleType> vehicleTypeMap = new HashMap<String, CustomVehicleType>();

	public Map<String, CustomVehicleType> getVehicleTypeMap() {
		return vehicleTypeMap;
	}
	
	public void addVehicleType (CustomVehicleType vehicleType) {
		vehicleTypeMap.put(vehicleType.getVehicleType(), vehicleType);
	}

	public void setVehicleTypeMap(Map<String, CustomVehicleType> vehicleTypeMap) {
		this.vehicleTypeMap = vehicleTypeMap;
	}
}
