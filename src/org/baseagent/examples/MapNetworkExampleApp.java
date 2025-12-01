// filepath: p:/Projects/BaseAgent/baseagent_java/src/org/baseagent/examples/MapNetworkExampleApp.java
package org.baseagent.examples;

import org.baseagent.map.Map;
import org.baseagent.map.MapLayer;
import org.baseagent.network.Edge;
import org.baseagent.network.MapNetworkRenderer;
import org.baseagent.network.Network;
import org.baseagent.sim.MapAgent;
import org.baseagent.sim.PathAgent;
import org.baseagent.sim.Simulation;
import org.baseagent.ui.MapCanvas;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

/**
 * Example application showing a planetary communications network with nodes in London, Paris, and New York.
 * A PathAgent cycles between the three nodes and can be visualized moving on the map.
 */
public class MapNetworkExampleApp extends Application {
    private MapCanvas mapCanvas;
    private Simulation simulation;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Create a Map and canvas
        Map map = new Map(8, 6);
        map.createMapLayer("network"); // a dummy layer to attach our MapNetworkRenderer

        mapCanvas = new MapCanvas(map, 256, 256, 0, 0);
        mapCanvas.setWidth(1024);
        mapCanvas.setHeight(768);

        // Center to show Europe and North Atlantic where London/Paris/NY are visible
        mapCanvas.centerOn(48.0, -20.0, 2);

        // Create simulation and attach universe/map
        simulation = new Simulation();
        simulation.setUniverse(map);
        mapCanvas.setSimulation(simulation);

        // Build network with nodes at London, Paris, New York
        Network<MapAgent, Object> network = new Network<>();

        MapAgent london = new MapAgent();
        london.setLatLon(51.5074, -0.1278);
        london.setColor(Color.DARKGREEN);

        MapAgent paris = new MapAgent();
        paris.setLatLon(48.8566, 2.3522);
        paris.setColor(Color.DARKBLUE);

        MapAgent newyork = new MapAgent();
        newyork.setLatLon(40.7128, -74.0060);
        newyork.setColor(Color.DARKRED);

        // Add agents both to the simulation (drawn on map) and to the network
        simulation.add(london);
        simulation.add(paris);
        simulation.add(newyork);

        network.addNode(london);
        network.addNode(paris);
        network.addNode(newyork);

        // Connect nodes (undirected visual links — we add both directions for visibility)
        Edge<MapAgent, Object> e1 = new Edge<>("lon-par", network.getNode(london), network.getNode(paris));
        Edge<MapAgent, Object> e2 = new Edge<>("par-ny", network.getNode(paris), network.getNode(newyork));
        Edge<MapAgent, Object> e3 = new Edge<>("ny-lon", network.getNode(newyork), network.getNode(london));
        network.addEdge(e1);
        network.addEdge(e2);
        network.addEdge(e3);

        // Add MapNetworkRenderer to draw the network on the map layer
        mapCanvas.addMapLayerRenderer("network", new MapNetworkRenderer<>(network));

        // Create a PathAgent that will follow the three nodes in a loop
        PathAgent courier = new PathAgent();
        courier.setColor(Color.ORANGERED);
        courier.setSpeedDegreesPerStep(0.25); // tune for pleasant animation
        courier.setWaypoints(london, paris, newyork);

        simulation.add(courier);

        // Start the simulation thread to run agent steps
        simulation.setDelayAfterEachStep(40); // milliseconds pause per step for visible movement
        simulation.start();

        BorderPane root = new BorderPane();
        root.setCenter(mapCanvas);

        Scene scene = new Scene(root, 1024, 768);
        primaryStage.setScene(scene);
        primaryStage.setTitle("BaseAgent Map Network Example — London / Paris / New York");
        primaryStage.show();

        // Clean shutdown
        primaryStage.setOnCloseRequest(evt -> {
            mapCanvas.shutdown();
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
