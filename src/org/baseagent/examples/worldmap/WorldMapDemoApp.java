package org.baseagent.examples.worldmap;

import org.baseagent.behaviors.map.MoveBehavior;
import org.baseagent.grid.Grid;
import org.baseagent.grid.GridLayer;
import org.baseagent.grid.ui.GridOverlayRenderer;
import org.baseagent.sim.Simulation;
import org.baseagent.worldmap.WorldMap;
import org.baseagent.worldmap.WorldMapAgent;
import org.baseagent.worldmap.WorldMapGridLayer;
import org.baseagent.worldmap.ui.WorldMapCanvas;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Simple demo application showing agents on Esri World Imagery tiles, with a grid overlay.
 * Controls:
 * - Middle mouse drag or Shift+drag to pan
 * - Ctrl+wheel to change slippy zoom (cursor stays fixed)
 * - Scroll wheel to pan vertically
 */
public class WorldMapDemoApp extends Application {
    private WorldMapCanvas mapCanvas;
    private Simulation simulation;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Create a Map (tile grid dimensions are only used for Canvas sizing here)
        WorldMap map = new WorldMap(8, 6);

        // Create MapCanvas (tileWidth, tileHeight used only for initial sizing)
        mapCanvas = new WorldMapCanvas(map, 256, 256, 0, 0);
        mapCanvas.setWidth(1024);
        mapCanvas.setHeight(768);

        // Center on a location in the Pacific ocean
        mapCanvas.centerOn(0.0, -160.0, 3);

        // Create simulation and attach to canvas
        simulation = new Simulation();
        simulation.setUniverse(map);
        mapCanvas.setSimulation(simulation);

        // Create a backing Grid and attach it as a map overlay. This reuses the Grid/GridLayer
        // infrastructure (load/save, step policies, HasGridPosition semantics, etc.).
        // Define a grid bounding box somewhere in the Pacific
        double topLat = 10.0;
        double leftLon = -170.0;
        double bottomLat = -10.0;
        double rightLon = -150.0;
        int rows = 8;
        int cols = 8;
        // Create backing Grid and a GridLayer named 'minerals', attach the GridLayer to the Map
        Grid grid = new Grid(cols, rows);
        GridLayer gl = grid.createGridLayer("minerals", GridLayer.GridLayerUpdateOption.NO_SWITCH);
        // Attach the GridLayer to the Map and set geographic bounds
        WorldMapGridLayer minerals = map.addGridOverlay("minerals", gl, topLat, leftLon, bottomLat, rightLon);
        // Populate the backing GridLayer
        for (int r=0; r<rows; r++) {
            for (int c=0; c<cols; c++) {
                if ((r + c) % 7 == 0) gl.set(c, r, "X");
            }
        }

        // Add grid overlay renderer for the minerals layer
        mapCanvas.addMapLayerRenderer("minerals", new GridOverlayRenderer());

        // Create some agents in the Pacific
        WorldMapAgent a1 = new WorldMapAgent();
        a1.setLatLon(0.0, -160.0);
        a1.setColor(Color.CORNFLOWERBLUE);
        simulation.add(a1);

        WorldMapAgent a2 = new WorldMapAgent();
        a2.setLatLon(-5.0, -155.0);
        a2.setColor(Color.ORANGE);
        simulation.add(a2);

        // Replace the previous AnimationTimer with Behavior-driven movement.
        // Create MoveBehaviors and attach to agents so movement happens during simulation.step()
        MoveBehavior mb1 = new MoveBehavior(0.2); // degrees per step
        mb1.addWaypoint(0.0, -160.0);
        mb1.addWaypoint(0.0, -155.0);
        mb1.setLoop(true);
        a1.addBehavior(mb1);

        MoveBehavior mb2 = new MoveBehavior(0.05);
        mb2.addWaypoint(-5.0, -155.0);
        mb2.addWaypoint(0.0, -155.0);
        mb2.setLoop(true);
        a2.addBehavior(mb2);

        // Start the simulation Ã¢â‚¬â€� Simulation will call each Agent.step(), which executes behaviors
        simulation.setDelayAfterEachStep(40); // small pause so movement is visible
        simulation.start();

        BorderPane root = new BorderPane();
        root.setCenter(mapCanvas);

        Scene scene = new Scene(root, 1024, 768);
        primaryStage.setScene(scene);
        primaryStage.setTitle("BaseAgent Map Demo Ã¢â‚¬â€� Esri World Imagery");
        primaryStage.show();

        // Clean shutdown hook to stop background tile threads and stop simulation when application exits
        primaryStage.setOnCloseRequest(evt -> {
            mapCanvas.shutdown();
            simulation.stop();
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}