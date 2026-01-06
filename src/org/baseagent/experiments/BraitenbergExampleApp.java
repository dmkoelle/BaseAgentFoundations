// filepath: p:/Projects/BaseAgent Projects/Foundations/src/org/baseagent/experiments/BraitenbergExampleApp.java
package org.baseagent.experiments;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import org.baseagent.sim.Simulation;
import org.baseagent.grid.Grid;
import org.baseagent.grid.GridAgent;
import org.baseagent.Beacon;
import org.baseagent.embodied.EmbodiedAgent;
import org.baseagent.embodied.sensors.MaxSignalSensor;
import org.baseagent.embodied.effectors.ForceEffector;
import org.baseagent.signals.Signal;
import org.baseagent.grid.GridLayer;
import org.baseagent.grid.GridLayer.GridLayerUpdateOption;
import org.baseagent.grid.ui.GridCanvasForSimulation;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Simple Braitenberg-style vehicle demo using EmbodiedAgent, sensors and effectors.
 */
public class BraitenbergExampleApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Create simulation and grid
        Simulation simulation = new Simulation();
        Grid grid = new Grid(60, 40);
        simulation.setUniverse(grid);

        // Create a dedicated grid layer for lights (beacons)
        grid.createGridLayer("lights", GridLayerUpdateOption.NO_SWITCH);

        // Canvas to visualize grid & agents
        GridCanvasForSimulation canvas = new GridCanvasForSimulation(simulation, grid, 12, 12, 0, 0);
        canvas.setWidth(900);
        canvas.setHeight(600);

        // Create a light beacon (anonymous subclass to provide distance-based intensity)
        Beacon light = new Beacon("lights") {
            @Override
            public boolean reaches(org.baseagent.grid.HasGridPosition p2) {
                // simple radius
                int dx = Math.abs(getCellX() - p2.getCellX());
                int dy = Math.abs(getCellY() - p2.getCellY());
                return (dx*dx + dy*dy) <= (20*20);
            }

            @Override
            public double getSignalValueAt(org.baseagent.grid.HasGridPosition p2) {
                double dx = getCellX() - p2.getCellX();
                double dy = getCellY() - p2.getCellY();
                double dist = Math.sqrt(dx*dx + dy*dy);
                if (dist < 1e-6) return 1.0;
                // simple inverse-square-ish falloff normalized to [0,1]
                double v = 1.0 / (1.0 + 0.1 * dist * dist);
                if (v > 1.0) v = 1.0;
                return v;
            }
        };
        light.placeAt(40, 10);
        light.setColor(Color.YELLOW);
        simulation.add(light);

        // Create embodied vehicle (5x5 body)
        EmbodiedAgent vehicle = new EmbodiedAgent(5, 5);
        vehicle.placeAt(10, 20);
        vehicle.setColor(Color.DARKGREEN);
        simulation.add(vehicle);

        // Create sensors: front-left and front-right positions on the body grid
        // Body coordinates: width=5 (cols 0..4), height=5 (rows 0..4), front = row 0
        int frontRow = 0;
        int leftCol = 1;
        int rightCol = 3;

        // Left sensor
        MaxSignalSensor leftSensor = new MaxSignalSensor("lights", null) {
            @Override
            public void sense(org.baseagent.Agent a) {
                // copy of MaxSignalSensor logic but set direction port to leftwards
                org.baseagent.grid.GridAgent agent = (org.baseagent.grid.GridAgent)a;
                List<Beacon> beacons = agent.getSimulation().getBeacons().stream()
                    .filter(b -> { GridLayer gl = b.getGridLayer(); return gl != null && gl.getLayerName().equals(getLayerName()); })
                    .collect(Collectors.toList());
                Beacon maxBeacon = null;
                double maxIntensity = 0.0d;
                for (Beacon b : beacons) {
                    if (b.reaches(agent)) {
                        double candidate = b.getSignalValueAt(agent);
                        if (candidate > maxIntensity) { maxIntensity = candidate; maxBeacon = b; }
                    }
                }
                if (maxBeacon != null) {
                    // set intensity and set a leftward direction
                    this.getIntensityPort().setOutputValue(maxIntensity);
                    // leftward in grid coordinates -> pi radians
                    this.getDirectionPort().setOutputValue(Math.PI);
                } else {
                    this.getIntensityPort().setOutputValue(0.0);
                    this.getDirectionPort().setOutputValue(Math.PI);
                }
            }
        };

        // Right sensor
        MaxSignalSensor rightSensor = new MaxSignalSensor("lights", null) {
            @Override
            public void sense(org.baseagent.Agent a) {
                org.baseagent.grid.GridAgent agent = (org.baseagent.grid.GridAgent)a;
                List<Beacon> beacons = agent.getSimulation().getBeacons().stream()
                    .filter(b -> { GridLayer gl = b.getGridLayer(); return gl != null && gl.getLayerName().equals(getLayerName()); })
                    .collect(Collectors.toList());
                Beacon maxBeacon = null;
                double maxIntensity = 0.0d;
                for (Beacon b : beacons) {
                    if (b.reaches(agent)) {
                        double candidate = b.getSignalValueAt(agent);
                        if (candidate > maxIntensity) { maxIntensity = candidate; maxBeacon = b; }
                    }
                }
                if (maxBeacon != null) {
                    this.getIntensityPort().setOutputValue(maxIntensity);
                    // rightward -> 0 radians
                    this.getDirectionPort().setOutputValue(0.0);
                } else {
                    this.getIntensityPort().setOutputValue(0.0);
                    this.getDirectionPort().setOutputValue(0.0);
                }
            }
        };

        // Create effectors left and right (force effectors) and place on body
        ForceEffector leftEff = new ForceEffector("body");
        ForceEffector rightEff = new ForceEffector("body");

        // Place sensors and effectors onto the vehicle body
        vehicle.place(leftCol, frontRow, leftSensor);
        vehicle.place(rightCol, frontRow, rightSensor);

        int leftEffCol = 0; int effRow = 2; // left-middle
        int rightEffCol = 4;
        vehicle.place(leftEffCol, effRow, leftEff);
        vehicle.place(rightEffCol, effRow, rightEff);

        // Connect sensors to the effectors
        leftSensor.connectTo(leftEff);
        rightSensor.connectTo(rightEff);

        // Start simulation
        simulation.setDelayAfterEachStep(120);
        simulation.start();

        BorderPane root = new BorderPane();
        root.setCenter(canvas);
        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Braitenberg Vehicle — EmbodiedAgent Demo");
        primaryStage.show();

        primaryStage.setOnCloseRequest(evt -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
