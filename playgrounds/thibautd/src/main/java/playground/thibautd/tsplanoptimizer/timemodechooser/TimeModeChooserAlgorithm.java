/* *********************************************************************** *
 * project: org.matsim.*
 * TimeModeChooserAlgorithm.java
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
package playground.thibautd.tsplanoptimizer.timemodechooser;

import static playground.thibautd.tsplanoptimizer.framework.TabuSearchRunner.runTabuSearch;

import java.util.Arrays;
import java.util.List;

import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.controler.Controler;
import org.matsim.core.population.PopulationFactoryImpl;
import org.matsim.core.population.PopulationImpl;
import org.matsim.core.router.TripRouterFactory;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.trafficmonitoring.DepartureDelayAverageCalculator;
import org.matsim.population.algorithms.PlanAlgorithm;

import playground.thibautd.tsplanoptimizer.framework.CompositeMoveGenerator;
import playground.thibautd.tsplanoptimizer.framework.CompositeTabuChecker;
import playground.thibautd.tsplanoptimizer.framework.EvolutionPlotter;
import playground.thibautd.tsplanoptimizer.framework.FitnessFunction;
import playground.thibautd.tsplanoptimizer.framework.ImprovementDelayMonitor;
import playground.thibautd.tsplanoptimizer.framework.Solution;
import playground.thibautd.tsplanoptimizer.framework.TabuSearchConfiguration;
import playground.thibautd.tsplanoptimizer.timemodechooser.traveltimeestimation.EstimatorTripRouterFactory;

/**
 * @author thibautd
 */
public class TimeModeChooserAlgorithm implements PlanAlgorithm {
	// if true, graphs of score evolution across TS iterations will
	// be created. This takes a LOT of ressources and creates a lot
	// of files. Use with care!
	private static final boolean DEBUG = false;

	// fixed parameters of the algorithm
	private static final List<Integer> STEPS = Arrays.asList( new Integer[]{ 1 * 60 , 5 * 60 , 30 * 60, 2 * 3600 });
	private static final List<String> POSSIBLE_MODES = Arrays.asList( new String[]{ "car" , "pt" , "walk" , "bike" } );
	private static final int N_ITER = 1000;
	// TODO: function of the number of activities
	private static final int N_TABU_DIRECTION = 10;
	private static final int N_TABU_MODE = N_TABU_DIRECTION;
	// TODO: function of the length of the tabu list
	private static final int IMPROVEMENT_DELAY = 100;

	private final Controler controler;
	private final DepartureDelayAverageCalculator delay;

	public TimeModeChooserAlgorithm(
			final Controler controler,
			final DepartureDelayAverageCalculator delay ) {
		this.controler = controler;
		this.delay = delay;
	}

	@Override
	public void run(final Plan plan) {
		TripRouterFactory tripRouterFactory =
			getAndTuneTripRouterFactory(
					plan,
					delay,
					controler );

		ScoringFunctionFactory scoringFunctionFactory = controler.getScoringFunctionFactory();

		//ConfigurationBuilder builder =
		//	new TimeModeChooserConfigBuilder(
		//			plan,
		//			scoringFunctionFactory,
		//			tripRouterFactory,
		//			DEBUG ? controler.getControlerIO().getIterationPath( controler.getIterationNumber() ) : null);

		// tune the configuration
		TabuSearchConfiguration configuration = new TabuSearchConfiguration();
		Solution initialSolution =
			new TimeModeChooserSolution(
					plan,
					tripRouterFactory.createTripRouter() );

		FitnessFunction fitness = new BasicFitness( scoringFunctionFactory );
		configuration.setFitnessFunction( fitness );

		configuration.setEvolutionMonitor(
				new ImprovementDelayMonitor(
					IMPROVEMENT_DELAY,
					N_ITER ));

		CompositeMoveGenerator generator = new CompositeMoveGenerator();
		generator.add( new FixedStepsIntegerMovesGenerator(
					initialSolution,
					STEPS,
					true));
		generator.add( new AllPossibleModesMovesGenerator(
					initialSolution,
					POSSIBLE_MODES) );
		configuration.setMoveGenerator( generator );

		CompositeTabuChecker tabuChecker = new CompositeTabuChecker();
		tabuChecker.add( new DirectionTabuList( N_TABU_DIRECTION ) );
		tabuChecker.add( new InvalidSolutionsTabuList() );
		tabuChecker.add( new ModeMovesTabuList( N_TABU_MODE ) );
		configuration.setTabuChecker( tabuChecker );

		if ( DEBUG ) {
			configuration.addListener(
					new EvolutionPlotter(
						"score evolution, agent "+plan.getPerson().getId(),
						controler.getControlerIO().getIterationPath( controler.getIterationNumber() )
						+"/"+plan.getPerson().getId()+"-fitness.png" ) );
		}

		// run the algo
		Solution bestSolution = runTabuSearch( configuration , initialSolution );

		// two goals here:
		// 1- the side effect: getRepresentedPlan sets the plan to the represented state
		// 2- the obvious check
		if (bestSolution.getRepresentedPlan() != plan) {
			throw new RuntimeException( "the returned plan is not the input plan" );
		}
	}

	private static TripRouterFactory getAndTuneTripRouterFactory(
			final Plan plan,
			final DepartureDelayAverageCalculator delay,
			final Controler controler ) {
		return new EstimatorTripRouterFactory(
				plan,
				((PopulationImpl) controler.getPopulation()).getFactory(),
				controler.getNetwork(),
				controler.getLinkTravelTimes(),
				controler.getLeastCostPathCalculatorFactory(),
				((PopulationFactoryImpl) ((PopulationImpl) controler.getPopulation()).getFactory()).getModeRouteFactory(),
				controler.getConfig().scenario().isUseTransit() ?
					controler.getScenario().getTransitSchedule() :
					null,
				controler.getConfig().plansCalcRoute(),
				delay,
				controler.getTripRouterFactory());
	}
}
