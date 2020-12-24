package org.matsim.contrib.carsharing.helper;

public class CustomVehicleType {

	private String vehicleType;
	private double maxVelocity;
	private double pcuEvuivalents;
	private double length;
	private double width;
	private int capacity;
	
	public String getVehicleType() {
		return vehicleType;
	}
	public void setVehicleType(String vehicleType) {
		this.vehicleType = vehicleType;
	}
	public double getMaxVelocity() {
		return maxVelocity;
	}
	public void setMaxVelocity(double maxVelocity) {
		this.maxVelocity = maxVelocity;
	}
	public double getPcuEvuivalents() {
		return pcuEvuivalents;
	}
	public void setPcuEvuivalents(double pcuEvuivalents) {
		this.pcuEvuivalents = pcuEvuivalents;
	}
	public double getLength() {
		return length;
	}
	public void setLength(double length) {
		this.length = length;
	}
	public double getWidth() {
		return width;
	}
	public void setWidth(double width) {
		this.width = width;
	}
	public int getCapacity() {
		return capacity;
	}	
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	
	@Override
	public String toString() {
		return "CustomVehicleType [vehicleType=" + vehicleType + ", maxVelocity=" + maxVelocity + ", pcuEvuivalents="
				+ pcuEvuivalents + ", length=" + length + ", width=" + width + ", capacity=" + capacity
				+ ", toString()=" + super.toString() + "]";
	}
}
