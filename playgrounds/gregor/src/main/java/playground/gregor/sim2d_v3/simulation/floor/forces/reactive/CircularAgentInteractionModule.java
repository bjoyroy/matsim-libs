/* *********************************************************************** *
 * project: org.matsim.*
 * AgentInteractionModule.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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
package playground.gregor.sim2d_v3.simulation.floor.forces.reactive;

import java.util.Collection;

import org.matsim.api.core.v01.Scenario;
import org.matsim.core.utils.collections.QuadTree;

import playground.gregor.sim2d_v3.scenario.MyDataContainer;
import playground.gregor.sim2d_v3.simulation.floor.Agent2D;
import playground.gregor.sim2d_v3.simulation.floor.PhysicalFloor;
import playground.gregor.sim2d_v3.simulation.floor.forces.DynamicForceModule;


/**
 * Agent interaction forces according to: D. Helbing, I. Farkas, T. Vicsek,
 * Nature 407, 487-490 (2000)
 * 
 * @author laemmel
 * 
 */
public class CircularAgentInteractionModule implements DynamicForceModule {

	protected final PhysicalFloor floor;
	protected final Scenario sc;

	private final double quadUpdateInterval = 0.1;
	private double lastQuadUpdate = Double.NEGATIVE_INFINITY;
	private QuadTree<Agent2D> coordsQuad;

	//Helbing constant
	private static final double Bi=0.08;
	private static final double Ai=2000;
	private static final double k = 1.2 * 100000;
	private static final double kappa = 2.4 * 100000;

	//Laemmel constant
	private static final double neighborhoodSensingRange = 5;

	/**
	 * @param floor
	 * @param sceanrio
	 */
	public CircularAgentInteractionModule(PhysicalFloor floor, Scenario scenario) {
		this.floor = floor;
		this.sc = scenario;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * playground.gregor.sim2_v2.simulation.floor.ForceModule#run(playground
	 * .gregor.sim2_v2.simulation.Agent2D)
	 */
	@Override
	public void run(Agent2D agent,double time) {
		updateForces(agent);
	}

	/**
	 * @param agent
	 * @param neighbors
	 */
	/* package */void updateForces(Agent2D agent) {//, List<Coordinate> neighbors) {
		double fx = 0;
		double fy = 0;

		Collection<Agent2D> l = this.coordsQuad.get(agent.getPosition().x, agent.getPosition().y, neighborhoodSensingRange);

		for (Agent2D other : l) {
			if (other == agent) {
				continue;
			}

			double dist = other.getPosition().distance(agent.getPosition());
			if (dist > neighborhoodSensingRange) {
				continue;
			}
			double dx = (agent.getPosition().x - other.getPosition().x) / dist;
			double dy = (agent.getPosition().y - other.getPosition().y) / dist;

			double bounderyDist = agent.getPhysicalAgentRepresentation().getAgentDiameter() - dist;
			double g = bounderyDist > 0 ? bounderyDist : 0;

			double tanDvx = (other.getVx() - agent.getVx()) * dx;
			double tanDvy = (other.getVy() - agent.getVy()) * dy;

			double tanX = tanDvx * -dy;
			double tanY = tanDvy * dx;

			double xc = (Ai * Math.exp((bounderyDist) / Bi) + k*g)* dx+ kappa * g * tanX;
			double yc = (Ai * Math.exp((bounderyDist) / Bi) + k*g)* dy + kappa * g * tanY;
			//			double xc = ( k*g)* dx;
			//			double yc = (k*g)* dy ;

			fx += xc;
			fy += yc;


		}


		agent.getForce().incrementX(fx);
		agent.getForce().incrementY(fy);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see playground.gregor.sim2d_v2.simulation.floor.ForceModule#init()
	 */
	@Override
	public void init() {
		double maxX = this.sc.getScenarioElement(MyDataContainer.class).getDenseCoordsQuadTree().getMaxEasting();
		double minX = this.sc.getScenarioElement(MyDataContainer.class).getDenseCoordsQuadTree().getMinEasting();
		double maxY = this.sc.getScenarioElement(MyDataContainer.class).getDenseCoordsQuadTree().getMaxNorthing();
		double minY = this.sc.getScenarioElement(MyDataContainer.class).getDenseCoordsQuadTree().getMinNorthing();
		this.coordsQuad = new QuadTree<Agent2D>(minX, minY, maxX, maxY);
	}

	/**
	 * 
	 */
	protected void updateAgentQuadtree() {
		this.coordsQuad.clear();
		for (Agent2D agent : this.floor.getAgents()) {
			this.coordsQuad.put(agent.getPosition().x, agent.getPosition().y, agent);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * playground.gregor.sim2d_v2.simulation.floor.DynamicForceModule#update
	 * (double)
	 */
	@Override
	public void update(double time) {
		if (time >= this.lastQuadUpdate + this.quadUpdateInterval) {

			updateAgentQuadtree();

			this.lastQuadUpdate = time;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * playground.gregor.sim2d_v2.simulation.floor.DynamicForceModule#forceUpdate
	 * (double)
	 */
	@Override
	public void forceUpdate() {
		updateAgentQuadtree();
	}

}