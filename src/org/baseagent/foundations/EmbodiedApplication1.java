package org.baseagent.foundations;

import java.io.IOException;

import org.baseagent.Agent;
import org.baseagent.Sensor;
import org.baseagent.behaviors.Behavior;
import org.baseagent.embodied.EmbodiedAgent;
import org.baseagent.embodied.effectors.Effector;
import org.baseagent.grid.Grid;
import org.baseagent.grid.GridAgent;
import org.baseagent.grid.ui.GridCanvas;
import org.baseagent.grid.ui.GridCanvasForSimulation;
import org.baseagent.sim.Simulation;
import org.baseagent.ui.defaults.VisualizationLibrary;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class EmbodiedApplication1 extends Application {
	
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
		simulation.endWhen(sim -> sim.getStepTime() == 15000);
		simulation.setDelayAfterEachStep(100);
		
		// A Universe is the place where simulations take place. There is one Universe for a Sim.
		// A Grid is a 2D area in which things may be displayed. This includes a Universe; it could 
		// include other things as well, such as a small Grid that represents the placement of sensors
		// on an agent body, or an agent's mental image of its grid world. 
		// Any Grid may be displayed through a GridCanvas. The Grid used as the Universe has a special
		// visualization component, GridCanvasForUniverse, which handles visualization updates
		// as the simulation runs.
		Grid grid = new Grid(100, 100);
		simulation.setUniverse(grid);
		
		GridCanvas canvas = new GridCanvasForSimulation(simulation, grid, 8, 8, 2, 2); // DMK - GridCanvasForSimulation different from GridCanvas? This is awkward
		
		GridCanvas gridCanvas = new GridCanvas(grid, 8, 8, 2, 2);
		
		// DMK - There could be pre-canned, beautiful GridCellRenderers that you can use instead of making your own. FIELD, LAB, CHECKER
		canvas.setBackgroundRenderer(VisualizationLibrary.checkerboardGrid(Color.DARKGRAY, Color.LIGHTGRAY));
//		canvas.addGridLayerRenderer(Grid.DEFAULT_GRID_LAYER, new GridCellRenderer() {
//			// DMK - caution against setGridRenderer which overtakes agent drawing
//			// DMK - I shouldn't have had to say DEFAULT_GRID_LAYER if I'm not naming layers
//			@Override
//			public void drawCell(GridCanvasContext gcc, GridLayer layer, Object value, double xInPixels, double yInPixels, double widthInPixels, double heightInPixels) {
//				GraphicsContext gc = gcc.getGraphicsContext();
//				// DMK I want to get the CellX and CellY that we're on so I can do math for a checker pattern, but that's not in GCC
//				gc.setFill(Color.LIGHTGREEN);
//				gc.fillRect(xInPixels, yInPixels, widthInPixels, heightInPixels);
//				gc.setFill(Color.LIGHTGREEN.darker());
//				gc.strokeRect(xInPixels, yInPixels, widthInPixels, heightInPixels);
//			} 
//		});

		EmbodiedAgent agent = new EmbodiedAgent(3, 3);
		agent.addBehavior(new Behavior() {
			@Override
			public void executeBehavior(Agent agent) {
				// TODO Auto-generated method stub
				
			}
		});
		
		Sensor leftSensor = new SignalSensor("light");
		Sensor rightSensor = new SignalSensor("light");
		agent.place(0, 0, leftSensor);
		agent.place(2, 0, rightSensor);
		
		Effector leftWheel = new Effector();
		Effector rightWheel = new Effector();
		agent.place(0, 0, leftWheel);
		agent.place(2, 0, rightWheel);

		leftSensor.intensity().connectTo(rightWheel.intensity());
		rightSensor.intensity().connectTo(leftWheel.intensity());
		
		GridAgent beacon = new GridAgent() {
			@Override
			public void step(Simulation sim) {
				generateSignal("light", 100);
			}
		};
		beacon.setDrawable(VisualizationLibrary.reactiveCircle("light", 0, Color.BLACK, 100, Color.WHITE));
		beacon.movable(); // Can be dragged and moved by user with mouse
		
		grid.place(beacon, 50, 50);
		grid.place(agent, 10, 10);

		// The code below is JavaFX code for displaying the simulation - which is mostly showing the GridCanvas
		BorderPane border = new BorderPane();
		border.setCenter(canvas);
		
		primaryStage.setTitle("Foundations");
		primaryStage.setScene(new Scene(new ScrollPane(border), 1000, 1000)); // DMK It would be nice for the 1000's to be automatic
		primaryStage.setWidth(1000);
		primaryStage.setHeight(1000);
		primaryStage.setX(350);
		primaryStage.setY(100);
		primaryStage.show();
		
		// Start the simulation!
		simulation.start();
	}

}
