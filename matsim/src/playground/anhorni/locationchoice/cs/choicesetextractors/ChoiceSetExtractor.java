package playground.anhorni.locationchoice.cs.choicesetextractors;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.apache.log4j.Logger;
import org.matsim.basic.v01.IdImpl;
import org.matsim.controler.Controler;
import playground.anhorni.locationchoice.cs.helper.ChoiceSet;
import playground.anhorni.locationchoice.cs.helper.SpanningTree;
import playground.anhorni.locationchoice.cs.helper.ZHFacilities;

public abstract class ChoiceSetExtractor {
		
	protected ZHFacilities facilities;
	protected Controler controler;
	private List<ChoiceSet> choiceSets;
	
	private final static Logger log = Logger.getLogger(ChoiceSetExtractor.class);
	
	public ChoiceSetExtractor(Controler controler, List<ChoiceSet> choiceSets) {
		this.controler = controler;
		this.choiceSets = choiceSets;
	} 
	
	
	protected void computeChoiceSets() {

		SpanningTree spanningTree = new SpanningTree(this.controler.getLinkTravelTimes(), this.controler.getTravelCostCalculator());
		String type ="s";
		
		int index = 0;
		List<ChoiceSet> choiceSets2Remove = new Vector<ChoiceSet>();
		
		Iterator<ChoiceSet> choiceSet_it = choiceSets.iterator();
		while (choiceSet_it.hasNext()) {
			ChoiceSet choiceSet = choiceSet_it.next();										
			this.computeChoiceSet(choiceSet, spanningTree, type, this.controler);
			log.info(index + ": Choice set " + choiceSet.getId().toString() + ": " + choiceSet.getFacilities().size() + " alternatives");
			index++;
			
			if (choiceSet.getTravelTime2ChosenFacility() > 8 * choiceSet.getTravelTimeBudget()) {	
				choiceSets2Remove.add(choiceSet);			
			}
			
			// remove the trips which end outside of canton ZH:
			/*
			 * change choice set list to TreeMap or similar
			 */
			if (choiceSet.getId().equals(new IdImpl("8160012")) ||
				choiceSet.getId().equals(new IdImpl("58690014")) ||
				choiceSet.getId().equals(new IdImpl("30195012")) ||
				choiceSet.getId().equals(new IdImpl("31953012")) ||
				choiceSet.getId().equals(new IdImpl("55926012")) ||
				choiceSet.getId().equals(new IdImpl("58650012")) ||
				choiceSet.getId().equals(new IdImpl("55443011")) ||
				choiceSet.getId().equals(new IdImpl("44971012")) ) {
				
				choiceSets2Remove.add(choiceSet);				
			}
			
			// remove trips with a walk TTB >= 7200 s:
			if (choiceSet.getId().equals(new IdImpl("27242011")) ||
				choiceSet.getId().equals(new IdImpl("27898011")) ||
				choiceSet.getId().equals(new IdImpl("42444011")) ||
				choiceSet.getId().equals(new IdImpl("65064011")) ||
				choiceSet.getId().equals(new IdImpl("15359011")) ||
				choiceSet.getId().equals(new IdImpl("27691011")) ||
				choiceSet.getId().equals(new IdImpl("65679015"))) {
				
				choiceSets2Remove.add(choiceSet);
			}
			
			
		}
		
		Iterator<ChoiceSet> choiceSets2Remove_it = choiceSets2Remove.iterator();
		while (choiceSets2Remove_it.hasNext()) {
			ChoiceSet choiceSet = choiceSets2Remove_it.next();
			this.choiceSets.remove(choiceSet);
			log.info("Removed choice set: " + choiceSet.getId() + " as travel time was implausible or trip ended outside canton ZH");
		}		
	}
		
	protected abstract void computeChoiceSet(ChoiceSet choiceSet, SpanningTree spanningTree, String type,
			Controler controler);
		
	public List<ChoiceSet> getChoiceSets() {
		return choiceSets;
	}

	public void setChoiceSets(List<ChoiceSet> choiceSets) {
		this.choiceSets = choiceSets;
	}

}
