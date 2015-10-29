/* *********************************************************************** *
 * project: org.matsim.*
 * EndActivityAndEvacuateReplanner.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2010 by the members listed in the COPYING,        *
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

package playground.christoph.evacuation.withinday.replanning.replanners;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.PopulationFactory;
import org.matsim.core.mobsim.qsim.InternalInterface;
import org.matsim.core.mobsim.qsim.agents.ExperimentalBasicWithindayAgent;
import org.matsim.core.mobsim.qsim.agents.PersonDriverAgentImpl;
import org.matsim.core.mobsim.qsim.agents.PlanBasedWithinDayAgent;
import org.matsim.core.population.ActivityImpl;
import org.matsim.core.population.PlanImpl;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.scenario.ScenarioImpl;
import org.matsim.core.utils.collections.Tuple;
import org.matsim.core.utils.misc.RouteUtils;
import org.matsim.withinday.replanning.replanners.interfaces.WithinDayDuringActivityReplanner;
import org.matsim.withinday.utils.EditRoutes;

import playground.christoph.evacuation.trafficmonitoring.SwissPTTravelTime;

public class EndActivityAndEvacuateReplanner extends WithinDayDuringActivityReplanner {
	
	private static final Logger log = Logger.getLogger(EndActivityAndEvacuateReplanner.class);
	
	private final SwissPTTravelTime ptTravelTime;
	
	/*package*/ EndActivityAndEvacuateReplanner(Id id, Scenario scenario, InternalInterface internalInterface, 
			SwissPTTravelTime ptTravelTime) {
		super(id, scenario, internalInterface);
		this.ptTravelTime = ptTravelTime;
	}
	
	@Override
	public boolean doReplanning(PlanBasedWithinDayAgent withinDayAgent) {		
		
		// If we don't have a valid PersonAgent
		if (withinDayAgent == null) return false;
	
		PlanImpl executedPlan = (PlanImpl)withinDayAgent.getSelectedPlan();

		// If we don't have an executed plan
		if (executedPlan == null) return false;
		
		Activity currentActivity;
		
		/*
		 *  Get the current PlanElement and check if it is an Activity
		 */
		PlanElement currentPlanElement = withinDayAgent.getCurrentPlanElement();
		if (currentPlanElement instanceof Activity) {
			currentActivity = (Activity) currentPlanElement;
		} else return false;
				
		/*
		 * If the agent is already at the end of his scheduled plan then
		 * the simulation counter has been decreased by one. We re-enable the
		 * agent so we have to increase the counter again.
		 */
//		if (currentActivity.getEndTime() == Time.UNDEFINED_TIME) this.agentCounter.incLiving();
		
		// Set the end time of the current activity to the current time.
		currentActivity.setEndTime(this.time);
		
		// get the index of the currently performed activity in the selected plan
		int currentActivityIndex = executedPlan.getActLegIndex(currentActivity);

		// identify the TransportMode for the rescueLeg
		String transportMode = identifyTransportMode(currentActivityIndex, executedPlan);
		
		// Remove all legs and activities after the current activity.
		while (executedPlan.getPlanElements().size() - 1 > currentActivityIndex) {
			executedPlan.removeActivity(executedPlan.getPlanElements().size() - 1);
		}
		
		PopulationFactory factory = scenario.getPopulation().getFactory();
		
		/*
		 * Now we add a new Activity at the rescue facility.
		 * We add no endtime therefore the activity will last until the end of
		 * the simulation.
		 */
		Activity rescueActivity = factory.createActivityFromLinkId("rescue", scenario.createId("rescueLink"));
		((ActivityImpl)rescueActivity).setFacilityId(scenario.createId("rescueFacility"));
		
		Coord rescueCoord = ((ScenarioImpl)scenario).getActivityFacilities().getFacilities().get(scenario.createId("rescueFacility")).getCoord();
		((ActivityImpl)rescueActivity).setCoord(rescueCoord);
		
		// create a leg using the identified transport mode
		Leg legToRescue = factory.createLeg(transportMode);
		
		// add new activity
		int position = executedPlan.getActLegIndex(currentActivity) + 1;
		executedPlan.insertLegAct(position, legToRescue, rescueActivity);
		
		// calculate route for the leg to the rescue facility
		new EditRoutes().replanFutureLegRoute(executedPlan, position, routeAlgo);

		/*
		 * Identify the last non-rescue link and relocate rescue activity to it.
		 */
		NetworkRoute route = (NetworkRoute) legToRescue.getRoute();
		
		/*
		 * If the route is like LinkXY-RescueLinkXY-RescueLink the the activity coordinate seems to be
		 * affected but the link itself is not. Therefore end the route at the same link.
		 */
		Id endLinkId = null;
		if (route.getLinkIds().size() > 1) {
			endLinkId = route.getLinkIds().get(route.getLinkIds().size() - 2);			
		} else endLinkId = route.getStartLinkId();
		((ActivityImpl) rescueActivity).setFacilityId(scenario.createId("rescueFacility" + endLinkId.toString()));
		((ActivityImpl) rescueActivity).setLinkId(endLinkId);
		NetworkRoute subRoute2 = route.getSubRoute(route.getStartLinkId(), endLinkId);
		legToRescue.setRoute(subRoute2);
		
		// if the person has to walk, we additionally try pt
		if (transportMode.equals(TransportMode.walk)) {
			Tuple<Double, Coord> tuple = ptTravelTime.calcSwissPtTravelTime(currentActivity, rescueActivity, this.time, executedPlan.getPerson());
			double travelTimePT = tuple.getFirst();
			double travelTimeWalk = legToRescue.getTravelTime();
			
			// If using pt is faster than walking switch to pt.
			if (travelTimePT < travelTimeWalk) {
				legToRescue.setMode(TransportMode.pt);
				
				// calculate route for the leg to the rescue facility
				new EditRoutes().replanFutureLegRoute(executedPlan, position, routeAlgo);
				
				// set travel time
				legToRescue.getRoute().setTravelTime(travelTimePT);
				
				// set speed
				double routeLength = RouteUtils.calcDistance((NetworkRoute) legToRescue.getRoute(), scenario.getNetwork());
				ptTravelTime.setPersonSpeed(withinDayAgent.getId(), routeLength / travelTimePT);
			}
		}
		
		/*
		 * Reschedule the currently performed Activity in the Mobsim - there
		 * the activityEndsList has to be updated.
		 */
		// yyyy a method getMobsim in MobimAgent would be useful here. cdobler, Oct'10
		// Intuitively I would agree.  We should think about where to set this so that, under normal circumstances,
		// it can't become null.  kai, oct'10
		if (withinDayAgent instanceof PersonDriverAgentImpl) {
			((ExperimentalBasicWithindayAgent) withinDayAgent).calculateAndSetDepartureTime(currentActivity);
			this.internalInterface.rescheduleActivityEnd(withinDayAgent);
			return true;
		}
		else {
			log.warn("PersonAgent is no DefaultPersonDriverAgent - the new departure time cannot be calcualted!");
			return false;
		}		
	}

	/*
	 * By default we try to use a car. We can do this, if the previous or the next 
	 * Leg are performed with a car.
	 * The order is as following:
	 * car is preferred to ride is preferred to pt is preferred to bike if preferred to walk 
	 */
	private String identifyTransportMode(int currentActivityIndex, Plan selectedPlan) {
		
		boolean hasCar = false;
		boolean hasRide = false;
		boolean hasBike = false;
		
		if (currentActivityIndex > 0) {
			Leg previousLeg = (Leg) selectedPlan.getPlanElements().get(currentActivityIndex - 1);
			String transportMode = previousLeg.getMode();
			if (transportMode.equals(TransportMode.car)) hasCar = true;
			else if (transportMode.equals(TransportMode.bike)) hasBike = true;
			else if (transportMode.equals(TransportMode.ride)) hasRide = true;
		}
		
		if (currentActivityIndex + 1 < selectedPlan.getPlanElements().size()) {
			Leg nextLeg = (Leg) selectedPlan.getPlanElements().get(currentActivityIndex + 1);
			String transportMode = nextLeg.getMode();
			if (transportMode.equals(TransportMode.car)) hasCar = true;
			else if (transportMode.equals(TransportMode.bike)) hasBike = true;
			else if (transportMode.equals(TransportMode.ride)) hasRide = true;
		}
		
		if (hasCar) return TransportMode.car;
		else if (hasRide) return TransportMode.ride;
		else if (hasBike) return TransportMode.bike;
		else return TransportMode.walk;
	}	
}