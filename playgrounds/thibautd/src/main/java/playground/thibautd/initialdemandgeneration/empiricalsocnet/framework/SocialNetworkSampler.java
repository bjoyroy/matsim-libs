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
package playground.thibautd.initialdemandgeneration.empiricalsocnet.framework;

import com.google.inject.Inject;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.contrib.socnetsim.framework.population.SocialNetwork;
import org.matsim.core.gbl.MatsimRandom;
import org.matsim.core.utils.misc.Counter;
import playground.thibautd.utils.KDTree;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author thibautd
 */
public class SocialNetworkSampler {
	private static final Logger log = Logger.getLogger( SocialNetworkSampler.class );
	private final Random random = MatsimRandom.getLocalInstance();

	private final Population population;
	private final DegreeDistribution degreeDistribution;
	private final CliquesFiller cliquesFiller;
	private final EgoLocator egoLocator;

	private final Consumer<Set<Ego>> cliquesListenner;

	@Inject
	public SocialNetworkSampler(
			final Population population,
			final DegreeDistribution degreeDistribution,
			final CliquesFiller cliquesFiller,
			final EgoLocator egoLocator,
			final Consumer<Set<Ego>> cliquesListenner ) {
		this.population = population;
		this.degreeDistribution = degreeDistribution;
		this.cliquesFiller = cliquesFiller;
		this.egoLocator = egoLocator;
		this.cliquesListenner = cliquesListenner;
	}

	public SocialNetwork sampleSocialNetwork() {
		final Map<Id<Person>,Ego> egos = new HashMap<>();
		for ( Person p : population.getPersons().values() ) {
			final Ego ego = new Ego( p , degreeDistribution.sampleDegree( p ) );
			egos.put( p.getId() , ego );
		}
		final KDTree<Ego> egosWithFreeStubs = new KDTree<>( egoLocator.getDimensionality() , egoLocator );
		egosWithFreeStubs.add( egos.values() );

		log.info( "Start sampling with "+egosWithFreeStubs.size()+" egos with free stubs" );
		final Counter counter = new Counter( "Sample clique # " );
		while ( egosWithFreeStubs.size() > 1 ) {
			counter.incCounter();

			final Ego ego = egosWithFreeStubs.get( random.nextInt( egosWithFreeStubs.size() ) );

			final Set<Ego> clique = cliquesFiller.sampleClique( ego , egosWithFreeStubs );
			cliquesListenner.accept( clique );

			for ( Ego member : clique ) {
				if ( member.getDegree() <= member.getAlters().size() ) {
					egosWithFreeStubs.remove( member );
				}
			}
		}
		counter.printCounter();

		// to assess what kind of damage the resolution of "conflicts" did
		final int sumPlannedDegrees =
				egos.values().stream()
					.mapToInt( Ego::getDegree )
					.sum();
		final int sumActualDegrees =
				egos.values().stream()
						.mapToInt( e -> e.getAlters().size() )
						.sum();

		log.info( "Average planned degree was "+((double) sumPlannedDegrees / egos.size()) );
		log.info( "Average actual degree is "+((double) sumActualDegrees / egos.size()) );
		log.info( "Number of excedentary ties: "+(sumActualDegrees - sumPlannedDegrees) );

		return new SocialNetwork() {
			@Override
			public void addEgo( final Id<Person> id ) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void addEgos( final Iterable<? extends Id<Person>> ids ) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void addBidirectionalTie( final Id<Person> id1, final Id<Person> id2 ) {
				throw new UnsupportedOperationException();

			}

			@Override
			public void addMonodirectionalTie( final Id<Person> ego, final Id<Person> alter ) {
				throw new UnsupportedOperationException();

			}

			@Override
			public Set<Id<Person>> getAlters( final Id<Person> ego ) {
				return egos.get( ego ).getAlters()
						.stream()
						.map( Ego::getId )
						.collect( Collectors.toSet() );
			}

			@Override
			public Set<Id<Person>> getEgos() {
				return egos.keySet();
			}

			@Override
			public Map<Id<Person>, Set<Id<Person>>> getMapRepresentation() {
				return egos.values().stream()
						.collect( Collectors.toMap(
								Ego::getId,
								e -> e.getAlters().stream()
										.map( Ego::getId )
										.collect( Collectors.toSet() ) ));
			}

			@Override
			public boolean isReflective() {
				return true;
			}

			private final Map<String, String> metadata = new HashMap<>();
			@Override
			public Map<String, String> getMetadata() {
				return metadata;
			}

			@Override
			public void addMetadata( final String att, final String value ) {
				metadata.put( att , value );
			}
		};
	}

}
