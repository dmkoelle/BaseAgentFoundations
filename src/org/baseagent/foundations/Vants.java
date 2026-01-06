package org.baseagent.foundations;


import java.io.IOException;

import org.baseagent.Agent;
import org.baseagent.behaviors.Behavior;
import org.baseagent.grid.Grid;
import org.baseagent.grid.GridAgent;
import org.baseagent.grid.GridLayer;
import org.baseagent.grid.ui.GridCanvas;
import org.baseagent.grid.ui.GridCanvasContext;
import org.baseagent.grid.ui.GridCellRenderer;
import org.baseagent.sim.Simulation;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Vants extends Application {
	
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
		Grid grid = new Grid(300, 300);
		grid.getGridLayer(Grid.DEFAULT_GRID_LAYER).fill("0");
		simulation.setUniverse(grid);
		
		GridCanvas gridCanvas = new GridCanvas(grid, 2, 2);
		
		createExample(simulation, gridCanvas);
		
		// The code below is JavaFX code for displaying the simulation - which is mostly showing the GridCanvas
		BorderPane borderPane = new BorderPane();
		borderPane.setCenter(gridCanvas);
		
		primaryStage.setTitle("Foundations");
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
//		gridCanvas.whenCellIs("1").color(Color.WHITE); // DMK - default layer, default property key
//		gridCanvas.whenCellIs("0").color(Color.BLACK); // DMK - Stackable draw-ers, color().shape().etc
		
		// Vant agent
		GridAgent vant = new GridAgent();
		vant.addBehavior(new Behavior() {
			@Override
			public void executeBehavior(Agent agent) {
				GridAgent vant = (GridAgent)agent;
				if (vant.isOn("1")) { // DMK - default layer, default property key
					vant.setCell("0"); // DMK - 'cell' is a Turtle method to get the cell you're on, does a grid.getCell() behind the scenes, could take layerName as a param
					vant.turnLeft();
				}
				else if (vant.isOn("0")) {
					vant.setCell("1");
					vant.turnRight();
				}
				vant.moveForward();
			}
		});
		vant.setColor(Color.RED);
		simulation.add(vant);
		vant.placeAt(150, 150);
	}
}
