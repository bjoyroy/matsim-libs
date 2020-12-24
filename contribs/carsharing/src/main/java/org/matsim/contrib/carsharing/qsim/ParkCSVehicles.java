package org.matsim.contrib.carsharing.qsim;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.contrib.carsharing.helper.CustomVehicleType;
import org.matsim.contrib.carsharing.helper.VehicleTypeContainer;
import org.matsim.contrib.carsharing.manager.supply.CarsharingSupplyInterface;
import org.matsim.contrib.carsharing.vehicles.CSVehicle;
import org.matsim.core.mobsim.framework.AgentSource;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.qnetsimengine.QVehicleImpl;
import org.matsim.vehicles.Vehicle;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;
import org.matsim.vehicles.VehiclesFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/** 
 * @author balac
 */
public class ParkCSVehicles implements AgentSource {
	private QSim qsim;
	private Map<String, VehicleType> modeVehicleTypes;
	private Collection<String> mainModes;
	private CarsharingSupplyInterface carsharingSupply;
	//private final static Logger log = Logger.getLogger(ParkCSVehicles.class);
	
	public ParkCSVehicles(QSim qSim,
			CarsharingSupplyInterface carsharingSupply,
			VehicleTypeContainer vehicleTypeContainer) {
		
		this.qsim = qSim;  
		this.modeVehicleTypes = new HashMap<>();
		this.mainModes = qsim.getScenario().getConfig().qsim().getMainModes();
	
		for (String mode : mainModes) {
			modeVehicleTypes.put(mode, VehicleUtils.getDefaultVehicleType());
		}
		this.carsharingSupply =  carsharingSupply;
		
		Map<String, CustomVehicleType> customVehicleMap = vehicleTypeContainer.getVehicleTypeMap(); // 
		
		for(Map.Entry<String, CustomVehicleType> entry : customVehicleMap.entrySet()) {
			String customMode = entry.getKey();
			CustomVehicleType cvt = entry.getValue();
			
			
			
			
			VehicleType vt = VehicleUtils.getFactory().createVehicleType(Id.create(customMode, VehicleType.class)); // vehicle mode form xml
			//VehicleType vt = VehicleUtils.getFactory().createVehicleType(Id.create(TransportMode.bike, VehicleType.class));
			
			// xml file
			vt.setMaximumVelocity(cvt.getMaxVelocity());
			vt.setPcuEquivalents(cvt.getPcuEvuivalents());
			vt.setLength(cvt.getLength());
			vt.setWidth(cvt.getWidth());
			vt.getCapacity().setSeats(cvt.getCapacity());
			
			modeVehicleTypes.put("twoway", vt);
			modeVehicleTypes.put("freefloating", vt);
			modeVehicleTypes.put("oneway", vt);
		}
		
		/*
		modeVehicleTypes.put("twoway", VehicleUtils.getDefaultVehicleType());
		modeVehicleTypes.put("freefloating", VehicleUtils.getDefaultVehicleType());

		modeVehicleTypes.put("oneway", VehicleUtils.getDefaultVehicleType());
		*/
		
		
		// Jalal: add new vehicle type
		/*
		VehicleType escooter = VehicleUtils.getFactory().createVehicleType(Id.create("bike", VehicleType.class));
		// values below are from Prof.Virginia
		escooter.setMaximumVelocity(5); // 18.6 mph //8.314944
		escooter.setPcuEquivalents(0.20);
		escooter.setLength(1.15824); // 3.8 ft
		escooter.setWidth(0.3048); // 1 ft
		// set capacity to 1
		escooter.getCapacity().setSeats(1);
//		bicycle.setNetworkMode("bicycle");
		modeVehicleTypes.put("twoway", escooter);
		modeVehicleTypes.put("freefloating", escooter);
		modeVehicleTypes.put("oneway", escooter);
		*/
		
		
	}

	@Override
	public void insertAgentsIntoMobsim() {
		Map<CSVehicle, Link> allVehicleLocations = this.carsharingSupply.getAllVehicleLocations();
		VehiclesFactory factory = this.qsim.getScenario().getVehicles().getFactory();

		for (CSVehicle vehicle : allVehicleLocations.keySet()) {
			final Vehicle basicVehicle = factory.createVehicle( Id.create( vehicle.getVehicleId(), Vehicle.class ),
					modeVehicleTypes.get( vehicle.getCsType() ) );

			final QVehicleImpl qvehicle = new QVehicleImpl( basicVehicle );
			// yyyyyy should react to new QVehicleFactory!  kai, nov'18
			
//			qsim.createAndParkVehicleOnLink(, allVehicleLocations.get(vehicle).getId());
			qsim.addParkedVehicle( qvehicle, allVehicleLocations.get(vehicle).getId() );
		}
		
		
	}

}
