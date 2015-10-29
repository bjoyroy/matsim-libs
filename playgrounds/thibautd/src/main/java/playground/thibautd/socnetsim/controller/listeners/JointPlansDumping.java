/* *********************************************************************** *
 * project: org.matsim.*
 * JointPlansDumping.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2013 by the members listed in the COPYING,        *
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
package playground.thibautd.socnetsim.controller.listeners;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.events.BeforeMobsimEvent;
import org.matsim.core.controler.listener.BeforeMobsimListener;

import playground.thibautd.socnetsim.population.JointPlans;
import playground.thibautd.socnetsim.population.JointPlansXmlWriter;

/**
 * @author thibautd
 */
public class JointPlansDumping implements BeforeMobsimListener {
	private static final Logger log =
		Logger.getLogger(JointPlansDumping.class);

	private Scenario sc;
	private JointPlans jointPlans;
	private int writePlansInterval, firstIteration ;
	private OutputDirectoryHierarchy controlerIO;

	public JointPlansDumping(
			final Scenario sc,
			final JointPlans jointPlans,
			final int firstIteration,
			final int writePlansInterval,
			final OutputDirectoryHierarchy controlerIO ) {
		this.sc = sc ;
		this.jointPlans = jointPlans;
		this.firstIteration = firstIteration ;
		this.writePlansInterval = writePlansInterval ;
		this.controlerIO = controlerIO ;
	}

	@Override
	public void notifyBeforeMobsim(final BeforeMobsimEvent event) {
		if (writePlansInterval <= 0) return;
		final boolean dump =
			(event.getIteration() % writePlansInterval== 0)
				|| (event.getIteration() == (firstIteration + 1));
		if (!dump) return;
		log.info("dumping joint plans...");
		JointPlansXmlWriter.write(
				sc.getPopulation(),
				jointPlans,
				controlerIO.getIterationFilename(
					event.getIteration(),
					"jointPlans.xml.gz" ));
		log.info("finished joint plans dump.");
	}

}