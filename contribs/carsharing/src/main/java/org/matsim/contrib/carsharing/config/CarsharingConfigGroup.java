package org.matsim.contrib.carsharing.config;

import org.matsim.core.config.ReflectiveConfigGroup;
/** 
 * @author balac
 */

public class CarsharingConfigGroup extends ReflectiveConfigGroup {
	public static final String GROUP_NAME = "Carsharing";
	
	private Integer statsWriterFrequency = null;
	private String vehiclelocationsInputFile = null;
	private String membershipInputFile = null;
	private String vehicleTypeInputFile = null;

	
	public CarsharingConfigGroup() {
		super(GROUP_NAME);
	}
	
	@StringGetter("vechileTypeFile")
	public String getVehicleTypeInputFile() {
		return vehicleTypeInputFile;
	}

	@StringSetter("vechileTypeFile")
	public void setVehicleTypeInputFile(final String vehicleTypeInputFile) {
		this.vehicleTypeInputFile = vehicleTypeInputFile;
	}
	
	@StringGetter( "statsWriterFrequency" )
	public Integer getStatsWriterFrequency() {
		return this.statsWriterFrequency;
	}

	@StringSetter( "statsWriterFrequency" )
	public void setStatsWriterFrequency(final String statsWriterFrequency) {
		this.statsWriterFrequency = Integer.parseInt( statsWriterFrequency );
	}
	
	@StringGetter( "vehiclelocations" )
	public String getvehiclelocations() {
		return this.vehiclelocationsInputFile;
	}

	@StringSetter( "vehiclelocations" )
	public void setvehiclelocations(final String vehiclelocationsInputFile) {
		this.vehiclelocationsInputFile = vehiclelocationsInputFile;
	}
	
	@StringGetter( "membership" )
	public String getmembership() {
		return this.membershipInputFile;
	}

	@StringSetter( "membership" )
	public void setmembership(final String membershipInputFile) {
		this.membershipInputFile = membershipInputFile;
	}
}
