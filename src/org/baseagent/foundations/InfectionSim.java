package org.baseagent.foundations;


import java.io.IOException;

import org.baseagent.Agent;
import org.baseagent.grid.Grid;
import org.baseagent.grid.GridAgent;
import org.baseagent.grid.GridLayer;
import org.baseagent.grid.ui.GridCanvas;
import org.baseagent.grid.ui.GridCanvasContext;
import org.baseagent.grid.ui.GridCellRenderer;
import org.baseagent.sim.Simulation;
import org.baseagent.statemachine.StateMachine;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class InfectionSim extends Application {
	
	/** JavaFX Application launch */
	public static void main(String[] args) {
		launch(args);
	}
	
	/** JavaFX Application start method */
	@Override
	public void start(Stage primaryStage) throws IOException {
		
		// A Simulation object is the core of a BaseAgent simulation.
		// Everything that will be simulated is added to the simulation.
		// The following settings indicate the end condition for the simulation,
		// and the delay between simulation steps, which helps make the visual display
		// proceed slowly enough for the human observer to notice what is happening.
		Simulation simulation = new Simulation();
		simulation.endWhen(sim -> sim.getStepTime() == 300000);
//		simulation.setDelayAfterEachStep(1);
		
		// A Grid is the field that contains things.
		// While a single Simulation may have multiple Grid objects, a Simulation has
		// only one Universe, which is the primary space in which the simulation
		// takes place. 
		Grid grid = new Grid(100, 100);
		grid.getGridLayer(Grid.DEFAULT_GRID_LAYER).fill("0");
		simulation.setUniverse(grid);
		
		GridCanvas gridCanvas = new GridCanvas(grid, 10, 10);

		Grid hospitalGrid = new Grid(10, 10);
		GridCanvas hospitalGridCanvas = new GridCanvas(hospitalGrid, 10, 10);
		
		Grid cemeteryGrid = new Grid(10, 10);
		GridCanvas cemeteryGridCanvas = new GridCanvas(cemeteryGrid, 10, 10);
		
		Slider slider = new Slider(); 
		
		Field field = new Field("Population", 800, 600);
		field.setColor(Color.TAN);

		field.addText("Population", 20, 10, 20);
		field.add(gridCanvas, 20, 20);
		field.addText("Hospital", 220, 10, 20);
		field.add(hospitalGridCanvas, 220, 20);
		field.addText("Cemetery", 220, 110, 20);
		field.add(cemeteryGridCanvas, 220, 120);
		
		
		
		createExample(simulation, field);
		
		// The code below is JavaFX code for displaying the simulation - which is mostly showing the GridCanvas
		BorderPane borderPane = new BorderPane();
		borderPane.setCenter(field);
		
		primaryStage.setTitle("Infection Simulation");
		primaryStage.setScene(new Scene(new ScrollPane(borderPane), 1000, 1000)); // DMK It would be nice for the 1000's to be automatic
		primaryStage.setWidth(1000);
		primaryStage.setHeight(1000);
		primaryStage.setX(350);
		primaryStage.setY(100);
		primaryStage.show();
		
		// Start the simulation!
		simulation.start();
	}

	private void createExample(Simulation simulation, GridCanvas gridCanvas) {
		gridCanvas.addGridLayerRenderer(Grid.DEFAULT_GRID_LAYER, new GridCellRenderer() {
			@Override
			public void drawCell(GridCanvasContext gcc, GridLayer layer, Object value, double xInPixels, double yInPixels, double widthInPixels, double heightInPixels) {
				Color color = Color.BLACK;
				if (value.equals("1")) color = Color.WHITE;
				gcc.getGraphicsContext().setFill(color);
				gcc.getGraphicsContext().fillRect(xInPixels, yInPixels, widthInPixels, heightInPixels);
			}
		});

		GridAgent person = new GridAgent() {
			@Override
			public void onCollision(Agent thisAgent, Agent collidingAgent) {
				if (thisAgent.getKnowledge().getOrDefault("IS_SICK", Boolean.FALSE).equals(Boolean.FALSE) && collidingAgent.getKnowledge().get("IS_SICK").equals(Boolean.TRUE)) {
					if (Math.random() <= (double)thisAgent.getSimulation().getProperties().get("CHANCE_OF_INFECTION")) {
						thisAgent.getKnowledge().put("SICK_ONSET", thisAgent.getSimulation().getStepTime());
					}
				}
			}
			
			@Override
			public void drawBefore(GridCanvasContext gcc) {
				Color agentColor = Color.WHITE;
				switch (((StateMachine)getBehavior("DISEASE")).getCurrentState().getStateName()) {
				case "HEALTHY" : agentColor = Color.BLUE; break;
				case "ONSET" : agentColor = Color.ORANGE; break;
				case "SICK" : agentColor = Color.RED; break;
				case "IN_HOSPITAL" : agentColor = Color.RED; break;
				case "DEAD" : agentColor = Color.DARKGRAY; break;
				}
				gcc.getGraphicsContext().setFill(agentColor);
			}
		};
		
		person.addBehavior(new WanderWithCollisionBehavior(10));

		StateMachine diseaseProgression = new StateMachine();
		diseaseProgression.addStates("HEALTHY", "ONSET", "SICK", "IN_HOSPITAL", "DEAD");
		diseaseProgression.addTransition("HEALTHY", "ONSET", agent -> agent.getKnowledge().containsKey("SICK_ONSET"));
		diseaseProgression.addTransition("ONSET", "SICK", agent -> agent.getSimulation().getStepTime() - (long)agent.getKnowledge().get("SICK_ONSET") >= (long)agent.getSimulation().getProperties().get("INCUBATION_PERIOD"));
		diseaseProgression.addTransition("SICK", "IN_HOSPITAL", agent -> Math.random() < (double)agent.getSimulation().getProperties().get("CHANCE_OF_HOSPITAL"), agent -> ((GridAgent)agent).warpTo("HOSPITAL_GRID"));
		diseaseProgression.addTransition("IN_HOSPITAL", "DEAD", agent -> Math.random() < (double)agent.getSimulation().getProperties().get("CHANCE_OF_DEATH"), agent -> ((GridAgent)agent).warpTo("DEAD_GRID"));
		diseaseProgression.addTransition("IN_HOSPITAL", "HEALTHY", agent -> agent.getSimulation().getStepTime() - (long)agent.getKnowledge().get("SICK_ONSET") >= (long)agent.getSimulation().getProperties().get("DURATION_OF_DISEASE"), agent -> ((GridAgent)agent).warpTo("POPULATION_GRID"));
		person.addBehavior("DISEASE", diseaseProgression);

//		StateMachine diseaseProgression2 = new StateMachine();
//		diseaseProgression2.addStates("HEALTHY", "ONSET", "SICK", "IN_HOSPITAL", "DEAD");
//		diseaseProgression2.addTransition("HEALTHY", "ONSET", k -> k.has("SICK_ONSET"));
//		diseaseProgression2.addTransition("ONSET", "SICK", k -> k.duration("SICK_ONSET") >= k.get("INCUBATION_PERIOD"));
//		diseaseProgression2.addTransition("SICK", "IN_HOSPITAL", k -> k.chance("CHANCE_OF_HOSPITAL"), agent -> agent.warpTo("HOSPITAL_GRID"));
//		diseaseProgression2.addTransition("IN_HOSPITAL", "DEAD", k -> k.chance("CHANCE_OF_DEATH"), agent -> agent.warpTo("DEAD_GRID"));
//		diseaseProgression2.addTransition("IN_HOSPITAL", "HEALTHY", k -> k.duration("SICK_ONSET") >= k.get("DURATION_OF_DISEASE"), agent -> agent.warpTo("POPULATION_GRID"));
//		person.addBehavior("DISEASE", diseaseProgression2);


		simulation.add(person);
		person.placeRandomly();
	}
}
