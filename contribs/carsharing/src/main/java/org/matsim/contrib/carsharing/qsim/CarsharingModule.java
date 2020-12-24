package org.matsim.contrib.carsharing.qsim;

import org.matsim.contrib.carsharing.helper.VehicleTypeContainer;
import org.matsim.contrib.carsharing.manager.supply.CarsharingSupplyInterface;
import org.matsim.core.mobsim.qsim.QSim;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class CarsharingModule extends AbstractModule {

	@Provides
	@Singleton
	public ParkCSVehicles provideAgentSource(QSim qsim, CarsharingSupplyInterface carsharingSupply, VehicleTypeContainer vehicleTypeContainer) {
		return new ParkCSVehicles(qsim, carsharingSupply, vehicleTypeContainer);
	}

	@Override
	protected void configure() {

	}

}
