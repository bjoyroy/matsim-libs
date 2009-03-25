/* *********************************************************************** *
 * project: org.matsim.*
 * NodeCarRouteTest.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
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

package org.matsim.core.population.routes;

import org.matsim.core.api.network.Link;
import org.matsim.core.api.population.NetworkRoute;
import org.matsim.core.population.routes.NodeNetworkRoute;

/**
 * @author mrieser
 */
public class NodeCarRouteTest extends AbstractCarRouteTest {

	public NetworkRoute getCarRouteInstance(final Link fromLink, final Link toLink) {
		return new NodeNetworkRoute(fromLink, toLink);
	}
	
}
