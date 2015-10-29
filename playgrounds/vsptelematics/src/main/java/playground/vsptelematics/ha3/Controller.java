/* *********************************************************************** *
 * project: org.matsim.*
 * Controller
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
package playground.vsptelematics.ha3;

import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.events.StartupEvent;
import org.matsim.core.controler.listener.StartupListener;


/**
 * @author dgrether
 *
 */
public class Controller {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Config config = ConfigUtils.loadConfig(args[0]);
		run(config);
//		computeSolution(args[0]);
	}
	
	private static void computeSolution(String c){
		for (int i = 0; i <= 10; i = i + 1){
			Config config = ConfigUtils.loadConfig(c);
			String rate;
			if ( i < 10)
				rate = "0." + Integer.toString(i);
			else 
				rate = "1.0";
			config.getModule("telematics").addParam("equipmentRate", rate);
			String outdir = config.controler().getOutputDirectory();
			outdir = outdir + "rate_" + rate + "/";
			config.controler().setOutputDirectory(outdir);
			run(config);
		}
		
	}
	
	public static void run(Config config){
		Controler c = new Controler(config);
		c.setOverwriteFiles(true);
		c.setCreateGraphs(false);
		addListener(c);
		c.run();
	}

	private static void addListener(Controler c){
		c.addControlerListener(new StartupListener(){
			@Override
			public void notifyStartup(StartupEvent event) {
				Controler con = event.getControler();
				double equipmentFraction = Double.parseDouble(event.getControler().getConfig().getParam("telematics", "equipmentRate"));
				String type = event.getControler().getConfig().getParam("telematics", "infotype");
				final GuidanceRouteTTObserver observer = new GuidanceRouteTTObserver(con.getControlerIO().getOutputFilename("routeTravelTimes.txt"));
				con.addControlerListener(observer);
				con.getEvents().addHandler(observer);
				GuidanceMobsimFactory mobsimFactory = new GuidanceMobsimFactory(type, equipmentFraction, event.getControler().getControlerIO().getOutputFilename("guidance.txt"), observer);
				event.getControler().addControlerListener(mobsimFactory);
				event.getControler().setMobsimFactory(mobsimFactory);
			}});
	}

	
}