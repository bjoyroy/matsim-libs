package org.matsim.contrib.carsharing.helper;

import java.util.Stack;

import org.matsim.core.utils.io.MatsimXmlParser;
import org.xml.sax.Attributes;

public class VehicleTypeReader extends MatsimXmlParser{
	private VehicleTypeContainer vehicleTypeContainer = new VehicleTypeContainer();
	CustomVehicleType vehicleType = null;

	@Override
	public void startTag(String name, Attributes atts, Stack<String> context) {	
		if(name=="vehicleType") {
			// 
			String type = atts.getValue("name");
			System.out.println("StartTag. name: " + name + " attr: " + type);
			vehicleType = new CustomVehicleType();
			vehicleType.setVehicleType(type);
		}
	}

	@Override
	public void endTag(String name, String content, Stack<String> context) {
		System.out.println("EndTag. name: " + name + " content: " + content);
		
		if(name == "maxVelocity") {
			vehicleType.setMaxVelocity(Double.parseDouble(content));
		}
		
		if(name == "pcuEquivalent") {
			vehicleType.setPcuEvuivalents(Double.parseDouble(content));
		}
		
		if(name == "length") {
			vehicleType.setLength(Double.parseDouble(content));
		}
		
		if(name == "width") {
			vehicleType.setWidth(Double.parseDouble(content));
		}
		
		if(name == "capacity") {
			vehicleType.setCapacity(Integer.parseInt(content));
		}
		
		if(name == "vehicleType") {
			vehicleTypeContainer.addVehicleType(vehicleType);
			System.out.println(vehicleType);
		}
		
	}
	
	public VehicleTypeContainer getVehicleTypeContainer() {
		return vehicleTypeContainer;
	}

}
