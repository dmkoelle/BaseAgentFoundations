package org.baseagent.foundations;

import java.io.IOException;

import org.baseagent.Patch;
import org.baseagent.grid.Grid;
import org.baseagent.grid.GridLayer;
import org.baseagent.grid.GridLayer.GridLayerUpdateOption;
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

public class Application3 extends Application {
	
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
		
		// A Grid is the field that contains things.
		// While a single Simulation may have multiple Grid objects, a Simulation has
		// only one Universe, which is the primary space in which the simulation
		// takes place. 
		Grid grid = new Grid(100, 100);
		grid.setUpdateOption(GridLayerUpdateOption.NEXT_BECOMES_CURRENT);
		simulation.setUniverse(grid);
		grid.fill("0");
		grid.form("1", 20, 20, "OOO", "O..", ".O.");
		
		GridCanvas gridCanvas = new GridCanvas(grid, 5, 5, 1, 1);

		
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
//		gridCanvas.style.is("0").color(Color.BLACK);
//		gridCanvas.style.is("1").color(Color.RED);
		
		gridCanvas.addGridLayerRenderer(new GridCellRenderer() {
			@Override
			public void drawCell(GridCanvasContext gcc, GridLayer layer, Object value, double xInPixels, double yInPixels, double widthInPixels, double heightInPixels) {
				Color color = Color.LIGHTGRAY;
				if ((value != null) && (value.equals("1"))) color = Color.RED;
				gcc.getGraphicsContext().setFill(color);
				gcc.getGraphicsContext().fillRect(xInPixels, yInPixels, widthInPixels, heightInPixels);
			}
		});
		
		// Game of Life patch
		Patch patch = new Patch() {
			@Override
			public void applyPatch(Grid grid, int x, int y) {
				int numAliveNeighbors = grid.count8Neighbors(x, y, value -> value.equals("1"));
				
				// If the cell is currently alive, it stays alive if it currently has 2 or 3 neighbors.
				if (grid.get(x, y).equals("1")) {
					if ((numAliveNeighbors == 2) || (numAliveNeighbors == 3)) {
						grid.set(x, y, "1");
					} else {
						grid.set(x, y, "0");
					}
				}
				
				// Otherwise, the cell becomes alive if it currently has 3 neighbors.
				else {
					if (numAliveNeighbors == 3) {
						grid.set(x, y, "1"); // DMK 'get' should be 'getCell'
					} else {
						grid.set(x, y, "0");
					}
				}
			}
		};
		simulation.add(patch);
		
	}
		
}
