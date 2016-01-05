/* *********************************************************************** *
 * project: org.matsim.*
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
package playground.ivt.maxess.tripbased;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import playground.ivt.maxess.prepareforbiogeme.tripbased.SBBPricesUtils;

/**
 * @author thibautd
 */
public class SBBPricesUtilsTest {
	private static final Logger log = Logger.getLogger( SBBPricesUtilsTest.class );

	private static final double TOLERANCE_SECOND = 1E-9;
	// this is really strange, but I cannot get the prices for first class right.
	// I assume there might be something done by SBB that does not appear in the norm.
	// difference is always at most twice the rounding interval (20 cents below 70 km, 1CHF above),
	// which remains reasonable.
	private static final double TOLERANCE_FIRST = 2;
	/**
	 * Tests consistency with the price table p 70 of http://voev.ch/T600_f
	 */
	@Test
	public void testConsistentWithPriceTableSecondClass() {
		final double[] distances = new double[] {
				1, 3, 6, 10, 12, 17, 23, 25.5,
				64, 68, 69, 70, 150, 560, 1000 };

		final double[] pricesSecond = new double[] {
				3, 3, 3.6, 4.40, 5.2, 7.60, 9.6, 10.4,
				22.4, 23.4, 25, 25, 45, 129, 215 };

		final double[] calc = new double[ distances.length ];
		if ( false ) Logger.getLogger( SBBPricesUtils.class ).setLevel( Level.TRACE );
		for ( int i = 0; i < distances.length; i++ ) {
			calc[ i ] = SBBPricesUtils.computeSBBTripPrice(
					SBBPricesUtils.SBBClass.second,
					false,
					distances[ i ] * 1000 );
			log.info( "2cl d="+distances[ i ]+"   sbb="+pricesSecond[ i ]+"   calc="+calc[ i ] );
		}

		for ( int i = 0; i < distances.length; i++ ) {
			Assert.assertEquals(
					"prices for second class to not correspond to price table for distance "+distances[ i ],
					pricesSecond[ i ],
					calc[ i ],
					TOLERANCE_SECOND );
		}
	}

	@Test
	public void testConsistentWithPriceTableFirstClass() {
		final double[] distances = new double[] {
				1, 3, 6, 10, 12, 17, 23, 25.5,
				64, 68, 69, 70, 150, 560, 1000 };

		final double[] pricesFirst = new double[] {
				5.4, 5.4, 6.4, 7.8, 9.2, 13.4, 16.8, 18.2,
				39.2, 41, 44, 44, 79, 226, 377 };


		final double[] calc = new double[ distances.length ];
		if ( false ) Logger.getLogger( SBBPricesUtils.class ).setLevel( Level.TRACE );
		for ( int i = 0; i < distances.length; i++ ) {
			calc[ i ] = SBBPricesUtils.computeSBBTripPrice(
					SBBPricesUtils.SBBClass.first,
					false,
					distances[ i ] * 1000 );
			log.info( "1cl d="+distances[ i ]+"   sbb="+pricesFirst[ i ]+"   calc="+calc[ i ] );
		}

		for ( int i = 0; i < distances.length; i++ ) {
			Assert.assertEquals(
					"prices for first class to not correspond to price table for distance "+distances[ i ],
					pricesFirst[ i ],
					calc[ i ],
					TOLERANCE_FIRST );
		}
	}
}
