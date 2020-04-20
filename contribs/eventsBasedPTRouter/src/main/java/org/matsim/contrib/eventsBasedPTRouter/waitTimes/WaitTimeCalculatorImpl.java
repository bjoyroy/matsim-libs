/* *********************************************************************** *
 * project: org.matsim.*
 * WaitTimeCalculator.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.contrib.eventsBasedPTRouter.waitTimes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonEntersVehicleEvent;
import org.matsim.api.core.v01.events.TransitDriverStartsEvent;
import org.matsim.api.core.v01.events.handler.PersonDepartureEventHandler;
import org.matsim.api.core.v01.events.handler.PersonEntersVehicleEventHandler;
import org.matsim.api.core.v01.events.handler.TransitDriverStartsEventHandler;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.api.experimental.events.VehicleArrivesAtFacilityEvent;
import org.matsim.core.api.experimental.events.handler.VehicleArrivesAtFacilityEventHandler;
import org.matsim.core.config.Config;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.misc.Time;
import org.matsim.pt.transitSchedule.api.Departure;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitRouteStop;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt.transitSchedule.api.TransitStopFacility;
import org.matsim.vehicles.Vehicle;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Save waiting times of agents while mobsim is running
 * 
 * @author sergioo
 */

@Singleton
public class WaitTimeCalculatorImpl implements WaitTimeCalculator, PersonDepartureEventHandler, PersonEntersVehicleEventHandler, TransitDriverStartsEventHandler, VehicleArrivesAtFacilityEventHandler, Provider<WaitTime> {

	//Attributes
	private final double timeSlot;
	private final Map<Tuple<Id<TransitLine>, Id<TransitRoute>>, Map<Id<TransitStopFacility>, WaitTimeData>> waitTimes = new HashMap<Tuple<Id<TransitLine>, Id<TransitRoute>>, Map<Id<TransitStopFacility>, WaitTimeData>>(1000);
	private final Map<Tuple<Id<TransitLine>, Id<TransitRoute>>, Map<Id<TransitStopFacility>, double[]>> scheduledWaitTimes = new HashMap<Tuple<Id<TransitLine>, Id<TransitRoute>>, Map<Id<TransitStopFacility>, double[]>>(1000);
	private final Map<Id<Person>, Double> agentsWaitingData = new HashMap<Id<Person>, Double>();
	private Map<Id<Vehicle>, Tuple<Id<TransitLine>, Id<TransitRoute>>> linesRoutesOfVehicle = new HashMap<Id<Vehicle>, Tuple<Id<TransitLine>, Id<TransitRoute>>>();
	private Map<Id<Vehicle>, Id<TransitStopFacility>> stopOfVehicle = new HashMap<Id<Vehicle>, Id<TransitStopFacility>>();
	
	//Constructors
	@Inject
	public WaitTimeCalculatorImpl(final TransitSchedule transitSchedule, final Config config, EventsManager eventsManager) {
		this(transitSchedule, config.travelTimeCalculator().getTraveltimeBinSize(), (int) (config.qsim().getEndTime().seconds()-config.qsim().getStartTime().seconds()));
		eventsManager.addHandler(this);
	}
	public WaitTimeCalculatorImpl(final TransitSchedule transitSchedule, final int timeSlot, final int totalTime) {
		this.timeSlot = timeSlot;
		for(TransitLine line:transitSchedule.getTransitLines().values())
			for(TransitRoute route:line.getRoutes().values()) {
				double[] sortedDepartures = new double[route.getDepartures().size()];
				int d=0;
				for(Departure departure:route.getDepartures().values())
					sortedDepartures[d++] = departure.getDepartureTime();
				Arrays.sort(sortedDepartures);
				Map<Id<TransitStopFacility>, WaitTimeData> stopsMap = new HashMap<Id<TransitStopFacility>, WaitTimeData>(100);
				Map<Id<TransitStopFacility>, double[]> stopsScheduledMap = new HashMap<Id<TransitStopFacility>, double[]>(100);
				for(TransitRouteStop stop:route.getStops()) {
					stopsMap.put(stop.getStopFacility().getId(), new WaitTimeDataArray((int) (totalTime/timeSlot)+1));
					double[] cacheWaitTimes = new double[(int) (totalTime/timeSlot)+1];
					for(int i=0; i<cacheWaitTimes.length; i++) {
						double endTime = timeSlot*(i+1);
						if(endTime>24*3600)
							endTime-=24*3600;
						cacheWaitTimes[i] = Time.getUndefinedTime();
						SORTED_DEPARTURES:
						for(double departure:sortedDepartures) {
							double arrivalTime = departure+(!Time.isUndefinedTime(stop.getArrivalOffset())?stop.getArrivalOffset():stop.getDepartureOffset());
							if(arrivalTime>=endTime) {
								cacheWaitTimes[i] = arrivalTime-endTime;
								break SORTED_DEPARTURES;
							}
						}
						if(Time.isUndefinedTime(cacheWaitTimes[i]))
							cacheWaitTimes[i] = sortedDepartures[0]+24*3600+(!Time.isUndefinedTime(stop.getArrivalOffset())?stop.getArrivalOffset():stop.getDepartureOffset())-endTime;
					}
					stopsScheduledMap.put(stop.getStopFacility().getId(), cacheWaitTimes);
				}
				Tuple<Id<TransitLine>, Id<TransitRoute>> key = new Tuple<Id<TransitLine>, Id<TransitRoute>>(line.getId(), route.getId());
				waitTimes.put(key, stopsMap);
				scheduledWaitTimes.put(key, stopsScheduledMap);
			}
	}

	//Methods
	@Override
	public WaitTime get() {
		return new WaitTime() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public double getRouteStopWaitTime(Id<TransitLine> lineId, Id<TransitRoute> routeId, Id<TransitStopFacility> stopId, double time) {
				return WaitTimeCalculatorImpl.this.getRouteStopWaitTime(lineId, routeId, stopId, time);
			}
		};
	}
	@Override
	public double getRouteStopWaitTime(Id<TransitLine> lineId, Id<TransitRoute> routeId, Id<TransitStopFacility> stopId, double time) {
		Tuple<Id<TransitLine>, Id<TransitRoute>> key = new Tuple<Id<TransitLine>, Id<TransitRoute>>(lineId, routeId);
		WaitTimeData waitTimeData = waitTimes.get(key).get(stopId);
		if(waitTimeData.getNumData((int) (time/timeSlot))==0) {
			double[] waitTimes = scheduledWaitTimes.get(key).get(stopId);
			return waitTimes[(int) (time/timeSlot)<waitTimes.length?(int) (time/timeSlot):(waitTimes.length-1)];
		}
		else
			return waitTimeData.getWaitTime((int) (time/timeSlot));
	}
	@Override
	public void reset(int iteration) {
		for(Map<Id<TransitStopFacility>, WaitTimeData> routeData:waitTimes.values())
			for(WaitTimeData waitTimeData:routeData.values())
				waitTimeData.resetWaitTimes();
		agentsWaitingData.clear();
		linesRoutesOfVehicle.clear();
		stopOfVehicle.clear();
	}
	@Override
	public void handleEvent(PersonDepartureEvent event) {
		if(event.getLegMode().equals("pt") && agentsWaitingData.get(event.getPersonId())==null)
			agentsWaitingData.put(event.getPersonId(), event.getTime());
		else if(agentsWaitingData.get(event.getPersonId())!=null)
			new RuntimeException("Departing with old data");
	}
	@Override
	public void handleEvent(PersonEntersVehicleEvent event) {
		Double startWaitingTime = agentsWaitingData.get(event.getPersonId());
		if(startWaitingTime!=null) {
			Tuple<Id<TransitLine>, Id<TransitRoute>> lineRoute = linesRoutesOfVehicle.get(event.getVehicleId());
			WaitTimeData data = waitTimes.get(lineRoute).get(stopOfVehicle.get(event.getVehicleId()));
			data.addWaitTime((int) (startWaitingTime/timeSlot), event.getTime()-startWaitingTime);
			agentsWaitingData.remove(event.getPersonId());
		}
	}

	@Override
	public void handleEvent(VehicleArrivesAtFacilityEvent event) {
		if(linesRoutesOfVehicle.get(event.getVehicleId())!=null)
			stopOfVehicle.put(event.getVehicleId(), event.getFacilityId());
	}

	@Override
	public void handleEvent(TransitDriverStartsEvent event) {
		linesRoutesOfVehicle.put(event.getVehicleId(), new Tuple<Id<TransitLine>, Id<TransitRoute>>(event.getTransitLineId(), event.getTransitRouteId()));
	}

}
