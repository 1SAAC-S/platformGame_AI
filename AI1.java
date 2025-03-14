import javafx.event.*;
import javafx.scene.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.canvas.*;
import javafx.scene.layout.*;
import javafx.animation.*;
import javafx.application.*;
import javafx.geometry.*;
import javafx.stage.*;
import java.util.*;
import javafx.scene.paint.Color;
import java.io.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;



//bi+uni directional CONNECTIONS
//SIMPLE COLLISION ERROR

public class AI1 {
    // Image for AI representation
    ImagePattern ptn;

    public AI1() {
        Image img = new Image("bubble.png");
        ptn = new ImagePattern(img);
    }

    // AI Name
    public String getName() {
        return "Jack";
    }

    Mood_GraphB theGraph;

    // Start method - initializes pathfinding
    public void start(Level.LevelIterator currentLevel, double px, double py) {
        theGraph = new Mood_GraphB(currentLevel);

        // Find start and end nodes
        Mood_Node startNode = null;
        Mood_Node endNode = null;

        currentLevel.resetIterator();
        while(currentLevel.hasNext()) {
            Level.TileWrapper tw = currentLevel.getNext();

            // Find start and end nodes
            if (tw.getIsStart()) {
                startNode = findCorrespondingNode(tw.getX()*30, tw.getY()*30);
            }

            if (tw.getIsEnd()) {
                endNode = findCorrespondingNode(tw.getX()*30, tw.getY()*30);
            }
        }

        // Calculate path if start and end nodes are found
        if (startNode != null && endNode != null) {
            theGraph.aStar(startNode, endNode);
        }
    }

    // Find corresponding graph node for a tile
    private Mood_Node findCorrespondingNode(int x, int y) {
        for (Mood_Node node : theGraph.theNodes) {
            if (node.getX() == x && node.getY() == y) {
                return node;
            }
        }
        return null;
    }

    // Run each game tick
    public void runEachTick(Level.LevelIterator currentLevel, double px, double py, double xv, double yv, double jump, boolean isGrounded) {
        theGraph.updateBreak(currentLevel);

        // Reset movement flags
        aDown = false;
        dDown = false;
        jumpDown = false;

        // If path has been calculated
        if (theGraph.start != null && theGraph.end != null) {
            // Find next node to move towards
            Mood_Node nextNode = findNextNodeToMove(px, py);

            if (nextNode != null) {
                // Movement tolerance
                double tolerance = 3;

                // Determine horizontal movement
                double xDifference = nextNode.getX() - px;

                // Horizontal movement
                if (xDifference < -tolerance) {
                    aDown = true;  // Move left
                }
                else if (xDifference > tolerance) {
                    dDown = true;  // Move right
                }

                // Advanced jump logic
                if (isGrounded) {
                    // Check if vertical jump is needed
                    if (isVerticalJumpRequired(px, py, nextNode)) {
                        jumpDown = true;
                    }
                    // Check if horizontal obstacle jump is needed
                    else if (isHorizontalObstacleJumpRequired(px, py, nextNode, currentLevel)) {
                        jumpDown = true;
                    }
                }
            }
        }
    }

    // Determine if vertical jump is required
    private boolean isVerticalJumpRequired(double px, double py, Mood_Node nextNode) {
        // Calculate vertical distance
        double verticalDistance = py - nextNode.getY();

        // Jump when next node is significantly higher
        return verticalDistance > 30;
    }

    // Determine if horizontal obstacle jump is required
    private boolean isHorizontalObstacleJumpRequired(double px, double py, Mood_Node nextNode, Level.LevelIterator currentLevel) {
        // Horizontal distance to next node
        double horizontalDistance = nextNode.getX() - px;
        int tileSize = 30;

        // Check if there's a significant horizontal gap
        if (Math.abs(horizontalDistance) > tileSize) {
            // Determine horizontal direction
            int xDirection = (int) Math.signum(horizontalDistance);
            int currentX = (int) (px / tileSize);
            int currentY = (int) (py / tileSize);

            // Simulate path checking
            while (currentX * tileSize != nextNode.getX()) {
                currentX += xDirection;

                // Check tile above and at current X for obstacles
                for (int yOffset = 0; yOffset <= 1; yOffset++) {
                    Level.TileWrapper obstacleTile = currentLevel.getSpecificTile(currentX, currentY + yOffset);

                    // If obstacle found that blocks movement
                    if (obstacleTile != null && !obstacleTile.getIsCollisionable()) {
                        return true;  // Jump required to overcome obstacle
                    }
                }
            }
        }
        return false;
    }

    // Find next node to move towards in the path
    private Mood_Node findNextNodeToMove(double px, double py) {
        Mood_Node current = theGraph.end;

        // Trace back through path
        while (current != null && current.getBackPointer() != null) {
            if (isNodeNearPosition(current, px, py)) {
                return current;
            }
            current = current.getBackPointer();
        }

        return null;
    }

    // Check if node is near current position
    private boolean isNodeNearPosition(Mood_Node node, double px, double py) {
        return true;  // Always return true to use all nodes in path
    }

    // Draw AI debugging information
    public void drawAIInfo(GraphicsContext gc) {
        theGraph.draw(gc);
    }

    // Key press state variables
    protected boolean aDown;
    protected boolean dDown;
    protected boolean jumpDown;

    // Key press state getters
    public boolean isADown() { return aDown; }
    public boolean isDDown() { return dDown; }
    public boolean isJumpDown() { return jumpDown; }

    // Jump event handler
    public void jumped() {
        jumpDown = false;
    }

    // Get AI image fill
    public ImagePattern getFill() {
        return ptn;
    }

    // Debug click handler
    public void clicked(int x, int y) {
        theGraph.clickPoint(x,y);
    }



    //I did these are inner classes, but you don't have to do.
    // you must put your team name + underscore (like P1_ as a prefix to whatever your classes are)
    public class Mood_GraphB {
        /*
        private Level.LevelIterator currentLevel;

        ArrayList<Mood_Node> theNodes = new ArrayList<Mood_Node>();
        ArrayList<Mood_Node> breakNodes = new ArrayList<Mood_Node>();

        // Creating the graph with collision detection
        public Mood_GraphB(Level.LevelIterator graphToCreate) {
           HashMap<String, String> isThereATileThere = new HashMap<String, String>();
           this.currentLevel = graphToCreate;
           // First pass: mark existing tiles
           graphToCreate.resetIterator();
           while (graphToCreate.hasNext()) {
              Level.TileWrapper tw = graphToCreate.getNext();
              isThereATileThere.put(tw.getX() + "_" + tw.getY(), "YES!");
           }

           // Second pass: create nodes
           graphToCreate.resetIterator();
           while (graphToCreate.hasNext()) {
              Level.TileWrapper tw = graphToCreate.getNext();
              if (isThereATileThere.get(tw.getX() + "_" + (tw.getY() - 1)) == null) {
                 if (!tw.getIsStart() && !tw.getIsEnd()) {
                    theNodes.add(new Mood_Node(tw.getX() * 30, tw.getY() * 30 - 30));
                 } else {
                    theNodes.add(new Mood_Node(tw.getX() * 30, tw.getY() * 30));
                 }

                 // Keep track of break nodes
                 if (tw.getIsBreak()) {
                    breakNodes.add(theNodes.get(theNodes.size() - 1));
                    theNodes.get(theNodes.size() - 1).setBreakMax(tw.getMaxBreakTimer());
                 } else {
                    theNodes.get(theNodes.size() - 1).setBreakAmount(-9);
                 }
              }
           }





           // Improved connection method with collision detection
           for (int i = 0; i < theNodes.size(); i++) {
              for (int j = 0; j < theNodes.size(); j++) {
                 if (i != j) {
                    Mood_Node n1 = theNodes.get(i);
                    Mood_Node n2 = theNodes.get(j);

                    // Check if nodes are within reasonable range
                    double horizontalDistance = Math.abs(n1.getX() - n2.getX());
                    double verticalDistance = n2.getY() - n1.getY();
                    boolean isHorizontallyClose = horizontalDistance < 35;
                    boolean isVerticalJumpPossible = verticalDistance > 0 && verticalDistance <= (5 * 30);

                    // Simulate collision detection along the path
                    boolean isHorizontalJumpPossible = Math.abs(horizontalDistance) <= (5 * 30) && verticalDistance == 0;

                    if (isHorizontalJumpPossible && !hasCollisionBetween(n1, n2, graphToCreate)) {
                       n1.addConnection(n2);
                       n2.addConnection(n1);
                    }

                 }
              }
           }
        }

            */
        private Level.LevelIterator currentLevel;

        ArrayList<Mood_Node> theNodes = new ArrayList<Mood_Node>();
        ArrayList<Mood_Node> breakNodes = new ArrayList<Mood_Node>();

        // Creating the graph with advanced collision detection
        public Mood_GraphB(Level.LevelIterator graphToCreate) {
            HashMap<String, String> isThereATileThere = new HashMap<String, String>();
            this.currentLevel = graphToCreate;

            // First pass: mark existing tiles
            graphToCreate.resetIterator();
            while (graphToCreate.hasNext()) {
                Level.TileWrapper tw = graphToCreate.getNext();
                isThereATileThere.put(tw.getX() + "_" + tw.getY(), "YES!");
            }

            // Second pass: create nodes
            graphToCreate.resetIterator();
            while (graphToCreate.hasNext()) {
                Level.TileWrapper tw = graphToCreate.getNext();
                if (isThereATileThere.get(tw.getX() + "_" + (tw.getY() - 1)) == null) {
                    if (!tw.getIsStart() && !tw.getIsEnd()) {
                        theNodes.add(new Mood_Node(tw.getX() * 30, tw.getY() * 30 - 30));
                    } else {
                        theNodes.add(new Mood_Node(tw.getX() * 30, tw.getY() * 30));
                    }

                    // Keep track of break nodes
                    if (tw.getIsBreak()) {
                        breakNodes.add(theNodes.get(theNodes.size() - 1));
                        theNodes.get(theNodes.size() - 1).setBreakMax(tw.getMaxBreakTimer());
                    } else {
                        theNodes.get(theNodes.size() - 1).setBreakAmount(-9);
                    }
                }
            }

            // New method to create advanced connections
            createConnections(graphToCreate);
        }

        private void createConnections(Level.LevelIterator graphToCreate) {
            Map<String, Mood_Node> nodeMap = new HashMap<>();
            for (Mood_Node node : theNodes) {
                nodeMap.put(node.getX() / 30 + "_" + node.getY() / 30, node);
            }

            for (Mood_Node currentNode : theNodes) {
                int currentTileX = currentNode.getX() / 30;
                int currentTileY = currentNode.getY() / 30;

                for (int dx = -13; dx <= 13; dx++) {
                    for (int dy = -20; dy <= 5; dy++) {  // Extended vertical range
                        if (dx == 0 && dy == 0) continue;

                        Mood_Node targetNode = nodeMap.get((currentTileX + dx) + "_" + (currentTileY + dy));
                        if (targetNode == null) continue;

                        ConnectionType connectionType = determineConnectionType(
                                graphToCreate, currentNode, targetNode, dx, dy
                        );

                        if (connectionType != null) {
                            currentNode.addConnection(targetNode, connectionType);
                        }
                    }
                }
            }
        }

        private ConnectionType determineConnectionType(
                Level.LevelIterator level,
                Mood_Node start,
                Mood_Node end,
                int dx,
                int dy
        ) {
            // Bidirectional connection (walk or jump in both directions)
            if (Math.abs(dy) <= 5 && Math.abs(dx) <= 11) {
                if (canWalkBetweenTiles(level, start.getX()/30, start.getY()/30, end.getX()/30, end.getY()/30) ||
                        canJumpHorizontally(level, start.getX()/30, start.getY()/30, end.getX()/30, end.getY()/30, dx, dy)) {
                    return ConnectionType.BIDIRECTIONAL;
                }
            }

            // Unidirectional fall connection
            if (dy < 0 && canFallBetweenTiles(level, start.getX()/30, start.getY()/30, end.getX()/30, end.getY()/30, dx, dy)) {
                return ConnectionType.UNIDIRECTIONAL;
            }

            return null;
        }

        private boolean isConnectionPossible(Level.LevelIterator level, Mood_Node start, Mood_Node end, int dx, int dy) {
            int startTileX = start.getX() / 30;
            int startTileY = start.getY() / 30;
            int endTileX = end.getX() / 30;
            int endTileY = end.getY() / 30;

            // Horizontal walking (same Y level)
            if (dy == 0 && Math.abs(dx) <= 1) {
                return canWalkBetweenTiles(level, startTileX, startTileY, endTileX, endTileY);
            }

            // Horizontal jump/fall scenarios
            // Can jump up 5 blocks vertically
            if (dy > 0 && dy <= 5) {
                // Horizontal jump
                if (Math.abs(dx) <= 11) {
                    return canJumpHorizontally(level, startTileX, startTileY, endTileX, endTileY, dx, dy);
                }
            }

            // Falling scenarios
            if (dy < 0) {
                // Falling distances based on horizontal travel
                int maxFallDistance = Math.abs(dx) >= 12 ? 3 :
                        Math.abs(dx) >= 13 ? 6 :
                                Integer.MAX_VALUE;

                if (Math.abs(dy) <= maxFallDistance) {
                    return canFallBetweenTiles(level, startTileX, startTileY, endTileX, endTileY, dx, dy);
                }
            }

            return false;
        }

        private boolean canWalkBetweenTiles(Level.LevelIterator level, int startX, int startY, int endX, int endY) {
            // Check if there's a clear path to walk between tiles
            int minX = Math.min(startX, endX);
            int maxX = Math.max(startX, endX);

            // Check ground tiles
            for (int x = minX; x <= maxX; x++) {
                // Check if ground is solid
                Level.TileWrapper groundTile = level.getSpecificTile(x, startY + 1);
                if (groundTile == null || !groundTile.getIsCollisionable()) {
                    return false;
                }

                // Check for obstacles at current and next level
                Level.TileWrapper currentLevelTile = level.getSpecificTile(x, startY);
                Level.TileWrapper nextLevelTile = level.getSpecificTile(x, startY - 1);

                if (currentLevelTile != null && !currentLevelTile.getIsCollisionable() ||
                        nextLevelTile != null && !nextLevelTile.getIsCollisionable()) {
                    return false;
                }
            }
            return true;
        }

        private boolean canJumpHorizontally(Level.LevelIterator level, int startX, int startY,
                                            int endX, int endY, int dx, int dy) {
            // Simulate jump arc
            int steps = Math.abs(dx);
            double horizontalStep = dx / (double) steps;

            for (int i = 1; i <= steps; i++) {
                double currentX = startX + (i * horizontalStep);
                double currentY = startY - calculateJumpHeight(i, steps, dy);

                // Check for obstacles
                Level.TileWrapper tile = level.getSpecificTile(
                        (int) Math.round(currentX),
                        (int) Math.round(currentY)
                );

                if (tile != null && !tile.getIsCollisionable()) {
                    return false;
                }
            }
            return true;
        }

        /*
        private boolean canFallBetweenTiles(Level.LevelIterator level, int startX, int startY,
                                            int endX, int endY, int dx, int dy) {
           // Simulate fall path
           int steps = Math.abs(dx);
           double horizontalStep = dx / (double) steps;

           for (int i = 1; i <= steps; i++) {
              double currentX = startX + (i * horizontalStep);
              double currentY = startY + Math.abs(dy * (i / (double) steps));

              // Check for obstacles
              Level.TileWrapper tile = level.getSpecificTile(
                      (int) Math.round(currentX),
                      (int) Math.round(currentY)
              );

              if (tile != null && !tile.getIsCollisionable()) {
                 return false;
              }
           }
           return true;
        }

         */
        private boolean canFallBetweenTiles(Level.LevelIterator level, int startX, int startY,
                                            int endX, int endY, int dx, int dy) {
            // Simulate fall path
            int steps = Math.abs(dx);

            // If horizontal distance is 0, no fall is possible
            if (steps == 0) return false;

            double horizontalStep = dx / (double) steps;

            // Simulate falling by checking collision at each horizontal step
            for (int i = 1; i <= steps; i++) {
                double currentX = startX + (i * horizontalStep);

                // Scale vertical position based on relative position and total vertical drop
                // This allows for nearly infinite drops while maintaining horizontal movement constraints
                double currentY = startY + (Math.abs(dy) * (i / (double) steps));

                // Check for obstacles at current position
                Level.TileWrapper tile = level.getSpecificTile(
                        (int) Math.round(currentX),
                        (int) Math.round(currentY)
                );

                // If any obstruction is found during the fall path, connection is not possible
                if (tile != null && !tile.getIsCollisionable()) {
                    return false;
                }
            }

            return true;
        }

        private double calculateJumpHeight(int currentStep, int totalSteps, int maxHeight) {
            // Parabolic jump height calculation
            double t = currentStep / (double) totalSteps;
            return maxHeight * (4 * t * (1 - t));
        }




        // Collision detection between two nodes
        private boolean hasCollisionBetween(Mood_Node n1, Mood_Node n2, Level.LevelIterator level) {
            int x1 = n1.getX() / 30;
            int y1 = n1.getY() / 30;
            int x2 = n2.getX() / 30;
            int y2 = n2.getY() / 30;

            // Calculate horizontal and vertical distances
            int dx = x2 - x1;
            int dy = y2 - y1;

            // If the horizontal gap requires a jump
            if (Math.abs(dx) > 1 && dy > -5 && dy < 5) {
                // Simulate a parabolic jump arc
                int steps = 10;
                for (int i = 0; i <= steps; i++) {
                    double t = i / (double) steps;
                    double xt = x1 + t * dx;
                    double yt = y1 - (4 * t * (1 - t) * 2); // Simulates a parabolic arc

                    Level.TileWrapper tile = level.getSpecificTile((int) Math.round(xt), (int) Math.round(yt));
                    if (tile != null && !tile.getIsCollisionable()) {
                        return true; // Collision detected in jump arc
                    }
                }
            } else if (dy == 0) {
                // Horizontal movement; check straight line
                int steps = Math.abs(dx);
                for (int i = 1; i <= steps; i++) {
                    int xt = x1 + (dx / steps) * i;

                    Level.TileWrapper tile = level.getSpecificTile(xt, y1);
                    if (tile != null && !tile.getIsCollisionable()) {
                        return true; // Collision detected
                    }
                }
            }
            return false; // No collision detected
        }




        //drawing the graph
        public void draw(GraphicsContext gc)
        {
            for(int i=0;i<theNodes.size();i++)
            {
                theNodes.get(i).draw(gc);
            }
        }

        public void updateBreak(Level.LevelIterator currentLevel)
        {
            //loop over all break nodes
            for(int i=0;i<breakNodes.size();i++)
            {
                //might have been better to leave the nodes in tileSpace.
                //I had to figure out what was wrong with my math.
                //System.out.println(breakNodes.get(i).getX()/30+" "+(breakNodes.get(i).getY()+30)/30);

                //get a particular breakNode's tile wrapper.
                Level.TileWrapper tw = currentLevel.getSpecificTile(breakNodes.get(i).getX()/30,(breakNodes.get(i).getY()+30)/30);

                if(tw == null) //so the node no longer exsits. this means it broke.
                {
                    breakNodes.remove(i);
                    i--;
                }
                else //otherwise update the timer.
                {
                    breakNodes.get(i).setBreakAmount(tw.getBreakTimer());
                }
            }
        }



        //takes in tilespace points
        public void clickPoint(int x, int y)
        {

            //this is because the the nodes are the upper left coords and the x and y are the center.
            x-=15;
            y-=15;

            for(int i=0;i<theNodes.size();i++)
            {
                Mood_Node n1 = theNodes.get(i);

                double d = Math.sqrt((n1.getX()-x)*(n1.getX()-x) + (n1.getY()-y)*(n1.getY()-y));


                //if distance is within 20 px of the clicked tile.
                if(d < 20)
                {


                    if(start == null)
                    {
                        start = n1;
                        n1.clicked(1);
                    }
                    else if(end == null)
                    {
                        end = n1;
                        n1.clicked(0);

                        aStar(start,end);
                    }
                    else
                    {
                        n1.clicked(-1);
                    }
                }
            }
        }


        Mood_Node start=null;
        Mood_Node end = null;

        public void aStar(Mood_Node start, Mood_Node end) {
            // Priority queue to store nodes to explore, sorted by f(n) = g(n) + h(n)
            PriorityQueue<Mood_Node> openSet = new PriorityQueue<>(
                    (a, b) -> Double.compare(a.getFScore(), b.getFScore())
            );

            // Set to keep track of explored nodes
            Set<Mood_Node> closedSet = new HashSet<>();

            // Reset node states
            resetNodes(start, end);

            // Initialize start node
            start.gScore = 0;
            start.hScore = calculateHeuristic(start, end);
            start.fScore = start.gScore + start.hScore;
            openSet.add(start);

            while (!openSet.isEmpty()) {
                Mood_Node current = openSet.poll();

                // Check if we've reached the goal
                if (current == end) {
                    reconstructPath(start, end);
                    return;
                }

                closedSet.add(current);

                // When exploring neighbors, check connection type
                for (NodeConnection connection : current.connections)
                {
                    Mood_Node neighbor = connection.node;

                    // If bidirectional or moving in a valid direction
                    if (connection.connectionType == ConnectionType.BIDIRECTIONAL || (connection.connectionType == ConnectionType.UNIDIRECTIONAL && neighbor.getY() < current.getY()))
                    {

                        // Skip already explored nodes
                        if (closedSet.contains(neighbor)) continue;

                        // Calculate tentative g score
                        double tentativeGScore = current.gScore + calculateEdgeCost(current, neighbor);

                        // If this is a new node or we've found a better path
                        if (!openSet.contains(neighbor) || tentativeGScore < neighbor.gScore) {
                            // Update node information
                            neighbor.backPointer = current;
                            neighbor.gScore = tentativeGScore;
                            neighbor.hScore = calculateHeuristic(neighbor, end);
                            neighbor.fScore = neighbor.gScore + neighbor.hScore;

                            // Add to open set if not already present
                            if (!openSet.contains(neighbor)) {
                                openSet.add(neighbor);
                            }
                        }
                    }
                }}

            // No path found
            System.out.println("No path exists between start and end nodes.");
        }

        // Helper method to reset node states before search
        private void resetNodes(Mood_Node start, Mood_Node end) {
            // Reset all nodes' search-related properties
            // This method should be called on all nodes in the graph
            start.gScore = Double.POSITIVE_INFINITY;
            start.hScore = 0;
            start.fScore = Double.POSITIVE_INFINITY;
            start.backPointer = null;
        }

        // Calculate heuristic using Manhattan distance (for grid-based movements)
        private double calculateHeuristic(Mood_Node current, Mood_Node goal) {
            return Math.abs(current.getX() - goal.getX()) + Math.abs(current.getY() - goal.getY());
        }

        // Calculate the cost between two connected nodes
        // Calculate the cost between two connected nodes based on Euclidean distance
        private double calculateEdgeCost(Mood_Node from, Mood_Node to) {
            // Calculate Euclidean distance between nodes
            double distance = Math.sqrt(
                    Math.pow(from.getX() - to.getX(), 2) +
                            Math.pow(from.getY() - to.getY(), 2)
            );

            // Optional: Add a base cost or multiplier for non-linear movement
            double baseCost = 1.0;
            return baseCost * distance;
        }

        // Reconstruct and mark the path from start to end
        private void reconstructPath(Mood_Node start, Mood_Node end) {
            Mood_Node current = end;

            while (current != start && current != null) {
                current.clicked(2); // Mark path nodes blue
                current = current.getBackPointer();
            }

            if (current != null) {
                current.clicked(2); // Mark start node
            } else {
                System.out.println("Failed to reconstruct path");
            }
        }


    }

    // Add an enum to represent connection types
    public enum ConnectionType {
        BIDIRECTIONAL,  // Can move in both directions (within jump/walk range)
        UNIDIRECTIONAL  // Can only move in one direction (typically fall-only)
    }

    public class NodeConnection {
        Mood_Node node;
        ConnectionType connectionType;

        NodeConnection(Mood_Node node, ConnectionType type) {
            this.node = node;
            this.connectionType = type;
        }
    }
    public class Mood_Node
    {
        // Store connections with their types

        //connections between nodes
        ArrayList<NodeConnection> connections = new ArrayList<>();
        //ArrayList<Mood_Node> connections = new ArrayList<Mood_Node>();
        // A* specific properties
        double gScore = Double.POSITIVE_INFINITY; // Cost from start node
        double hScore = 0; // Heuristic estimated cost to goal
        double fScore = Double.POSITIVE_INFINITY; // Total estimated cost

        // Getter for f-score to use in priority queue
        public double getFScore() {
            return fScore;
        }

        int x,y;

        //for IDs...
        int id;
        static int idgen=0;

        double currentBreakAmount=0; //in my program -10 on a tile means not broken or not breakable (use tw.getIsBreak() to deteremine the diff). -9 in my implementaion means not breakable. and a positive number is how much time is left
        double maxBreakAmount=0;

        public Mood_Node(int _x, int _y)
        {
            x = _x;
            y = _y;

            id = idgen++;
        }

        public int getX()
        {
            return x;
        }

        public int getY()
        {
            return y;
        }

        //name of a tile is x_y
        public String getName()
        {
            return x+"_"+y;
        }

        public void addConnection(Mood_Node toAdd, ConnectionType type) {
            connections.add(new NodeConnection(toAdd, type));
        }

        //for keeping track of break tiles.
        public void setBreakAmount(double d)
        {
            currentBreakAmount = d;
        }

        public void setBreakMax(double d)
        {
            maxBreakAmount = d;
        }

        //these methods were for dijsktra
        public int getSize()
        {
            return connections.size();
        }

        public boolean isInQueue()
        {
            return inQueue;
        }

        public void setIsInQueue(boolean val)
        {
            inQueue = val;
        }
        boolean inQueue = false;

        public NodeConnection get(int i)
        {
            return connections.get(i);
        }

        Color fillColor = Color.YELLOW;


        //clicked method to change colors. this is really for debugging
        public void clicked(int option)
        {

            fillColor = new Color(0,1,0,1);

            if(option == 0)
            {
                fillColor = Color.PINK;
            }
            if(option == 1)
            {
                fillColor = Color.PURPLE;
            }
            if(option == 2)
            {
                fillColor = Color.BLUE;
            }
        }

        public void draw(GraphicsContext gc)
        {

            //draw all this nodes's connections. NOTE: my implementation doesn't remove connections from each node when a node breaks.
            for(int i=0;i<connections.size();i++)
            {
                if(connections.get(i).connectionType == AI1.ConnectionType.BIDIRECTIONAL){
                    gc.setStroke(Color.RED);
                }
                else{
                    gc.setStroke(Color.ORANGE);
                }
                gc.setLineWidth(3);
                gc.strokeLine(x+8+7,y+8+7,connections.get(i).node.getX() +8+7,connections.get(i).node.getY()+8+7);
            }

            if(currentBreakAmount == -10)
            { //-10 means not stepped on or not breakable. You can do getIsBreak() from the tw if you want.
                gc.setFill(fillColor);
            }
            else if(currentBreakAmount == -9)
            {
                gc.setFill(fillColor);
            }
            else
            {
                gc.setFill(Color.BLACK.interpolate(Color.WHITE,currentBreakAmount/maxBreakAmount)); //color based on break amount %
            }

            gc.fillOval(x+8,y+8,14,14);
        }

        Mood_Node backPointer=null;

        public void setBackPointer(Mood_Node theThing)
        {
            backPointer = theThing;
        }

        public Mood_Node getBackPointer()
        {
            return backPointer;
        }

    }
}




