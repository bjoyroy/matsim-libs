/* *********************************************************************** *
 * project: org.matsim.*
 * XYZEventsFactoryImpl.java
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
package playground.gregor.sim2d_v3.events;

import org.matsim.core.api.experimental.events.Event;
import org.matsim.core.api.experimental.events.EventsFactory;
import org.matsim.core.basic.v01.IdImpl;

/**
 * @author laemmel
 * 
 */
public class XYVxVyEventsFactoryImpl {

	private final EventsFactory factory;

	public XYVxVyEventsFactoryImpl(EventsFactory factory) {
		this.factory = factory;
	}

	public EventsFactory getFactory() {
		return this.factory;
	}


	/**
	 * 
	 * @param x
	 * @param y
	 * @param vx
	 * @param vy
	 * @param id
	 * @param time
	 * @return
	 */
	public Event createXYZAzimuthEvent(String x, String y, String vx, String vy, String id, String time) {
		XYVxVyEventImpl e = new XYVxVyEventImpl(new IdImpl(id), Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(vx), Double.parseDouble(vy), Double.parseDouble(time));
		return e;
	}

}