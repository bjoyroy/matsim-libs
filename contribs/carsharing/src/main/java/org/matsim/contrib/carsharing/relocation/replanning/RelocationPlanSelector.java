package org.matsim.contrib.carsharing.relocation.replanning;

import org.matsim.api.core.v01.population.HasPlansAndId;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.replanning.selectors.PlanSelector;

/**
 * @author nagel
 */
public class RelocationPlanSelector implements PlanSelector<Plan, Person>
{

	@Override
	public Plan selectPlan(HasPlansAndId<Plan, Person> person) {
		/*
		double maxScore = Double.NEGATIVE_INFINITY;
		Plan bestPlan = null;

		for (Plan plan : person.getPlans()) {
			Double score = plan.getScore();
			if ((score != null) && (score.doubleValue() > maxScore) && !score.isNaN() ) {
				maxScore = plan.getScore().doubleValue();
				bestPlan = plan;
			}
		}

		if (bestPlan == null && person.getPlans().size() > 0) {
			// it seems none of the plans has a real score... so just return the first one (if there is one)
			return person.getPlans().get(0);
		}
		return bestPlan;
		*/
		return person.getSelectedPlan();
	}

}
