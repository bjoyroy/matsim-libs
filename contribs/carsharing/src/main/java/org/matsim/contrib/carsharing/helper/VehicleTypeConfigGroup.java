package org.matsim.contrib.carsharing.helper;

import org.matsim.core.config.ReflectiveConfigGroup;

public class VehicleTypeConfigGroup extends ReflectiveConfigGroup{
	
	public static final String GROUP_NAME = "VehicleType";
	
	public VehicleTypeConfigGroup() {
		super(GROUP_NAME);
	}

	private String vehicleType;
	private double maxVelocity;
	private double pcuEvuivalents;
	private double length;
	private double width;
	private int capacity;
	
	
	@StringGetter("vehicleType")
	public String getVehicleType() {
		return vehicleType;
	}
	
	@StringSetter("vehicleType")
	public void setVehicleType(String vehicleType) {
		this.vehicleType = vehicleType;
	}
	
	@StringGetter("maxVelocity")
	public double getMaxVelocity() {
		return maxVelocity;
	}
	
	@StringSetter("maxVelocity")
	public void setMaxVelocity(double maximumSpeed) {
		this.maxVelocity = maximumSpeed;
	}
	
	@StringSetter("pcuEvuivalents")
	public double getPcuEvuivalents() {
		return pcuEvuivalents;
	}
	
	@StringSetter("pcuEvuivalents")
	public void setPcuEvuivalents(double pcuEvuivalents) {
		this.pcuEvuivalents = pcuEvuivalents;
	}
	
	@StringSetter("length")
	public double getLength() {
		return length;
	}
	
	@StringSetter("length")
	public void setLength(double length) {
		this.length = length;
	}
	
	@StringGetter("width")
	public double getWidth() {
		return width;
	}
	
	@StringSetter("width")
	public void setWidht(double width) {
		this.width = width;
	}
	
	@StringGetter("capacity")
	public int getCapacity() {
		return capacity;
	}
	
	@StringSetter("capacity")
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	
	
}
