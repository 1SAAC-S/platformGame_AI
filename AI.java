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

public class AI {
   // Image for AI representation
   ImagePattern ptn;

   public AI() {
      Image img = new Image("Claude.jpeg");
      ptn = new ImagePattern(img);
   }

   // AI Name
   public String getName() {
      return "Claude";
   }

   Mood_GraphB theGraph;

   // Start method - initializes pathfinding
   public void start(Level.LevelIterator currentLevel, double px, double py) {
      theGraph = new Mood_GraphB(currentLevel);

      Mood_Node endNode = null;
      currentLevel.resetIterator();

      while (currentLevel.hasNext()) {
         Level.TileWrapper tw = currentLevel.getNext();
         if (tw.getIsEnd()) {
            endNode = findCorrespondingNode(tw.getX() * 30, tw.getY() * 30);
            break;
         }
      }

      if (endNode != null) {
         Mood_Node startNode = findClosestNode(px, py);
         if (startNode != null) {
            path = theGraph.dijkstra(startNode, endNode);
            currentPathIndex = 0;
            currentNode = startNode;
         }
      }
   }
      // Find corresponding graph node for a tile
      private Mood_Node findCorrespondingNode ( int x, int y){
         for (Mood_Node node : theGraph.theNodes) {
            if (node.getX() == x && node.getY() == y) {
               return node;
            }
         }
         return null;
      }


   Mood_Node currentTargetNode = null;

   int currentPathIndex;
   double stationaryTimer = 0;
   double stationaryThreshold = 2.0; // Adjust this value as needed
   ArrayList<Mood_Node> path;
   double deadzoneX = 8.5; // Adjust this value as needed
   double deadzoneY = 10.0; // Adjust this value as needed

   Mood_Node currentNode = null;


   private double jumpDistanceThreshold = 60.0; // Adjustable, default 2 tiles (30px each)

   public void runEachTick(Level.LevelIterator currentLevel, double px, double py, double xv, double yv, double jump, boolean isGrounded) {
      //theGraph.updateBreak(currentLevel);
      //boolean blockBroke = theGraph.updateBreak(currentLevel);


      aDown = false;
      dDown = false;
      jumpDown = false;

      if (path != null && currentPathIndex < path.size()) {
         Mood_Node currentTargetNode = path.get(currentPathIndex);

         if ((currentTargetNode.currentBreakAmount > 0 &&
                 currentTargetNode.currentBreakAmount < currentTargetNode.maxBreakAmount * 0.3)) {
            start(currentLevel, px, py);
            //System.out.println("Path recalculated - Break block detected");
            return;
         }

         // Calculate actual distance to target node
         double distanceToTarget = Math.sqrt(
                 Math.pow(px - currentTargetNode.getX(), 2) +
                         Math.pow(py - currentTargetNode.getY(), 2)
         );

         // Check if we've reached the current target node
         if (distanceToTarget <= Math.max(deadzoneX, deadzoneY)) {
            currentPathIndex++;
            stationaryTimer = 0;
         } else {
            stationaryTimer += 0.016;
         }

         // Recalculate path if we haven't reached target in time

         if (stationaryTimer >= stationaryThreshold) {
            start(currentLevel, px, py);
            stationaryTimer = 0;
         }

         // Continue with movement if we still have nodes to visit
         if (currentPathIndex < path.size()) {
            currentTargetNode = path.get(currentPathIndex);


            int dx = currentTargetNode.getX() - (int)px;
            int dy = currentTargetNode.getY() - (int)py;

            // Horizontal movement
            if ((dx < 0 && py <= currentTargetNode.getY() +5) || dx < 0 && isGrounded) {
               aDown = true;
              // System.out.println("Left detected");
            } else if ((dx > 0 && py <= currentTargetNode.getY() +5) || dx > 0 && isGrounded) {
               dDown = true;
              // System.out.println("Right detected");
            }

            //System.out.println("py <= currentTargetNode.getY(): " + (py >= currentTargetNode.getY()));
            //System.out.println("isGrounded " + isGrounded);
            //System.out.println("dx > 90 " + (dx < 90));

           // System.out.println("dx " + dx);
            // Vertical movement (jumping)
            if ((py >= currentTargetNode.getY() +5  && isGrounded && dx <90) || isAtEdge(currentLevel, px, py, dx)) {
               System.out.println("ShouldJUmp");
               jumpDown = true;
            }

            if (isAtEdge(currentLevel, px, py, dx)) {
               System.out.println(isAtEdge(currentLevel, px, py, dx));
            }

            // Long jump check
            if ((Math.abs(dx) > jumpDistanceThreshold) && isAtEdge(currentLevel, px, py, dx) && isGrounded && dy == 0) {
               //System.out.println("Jump detected");
               jumpDown = true;
            }
         }
      }
   }

   private Mood_Node findClosestNode(double px, double py) {
      Mood_Node closest = null;
      double minDistance = Double.MAX_VALUE;

      for (Mood_Node node : theGraph.theNodes) {
         double distance = Math.sqrt(
                 Math.pow(node.getX() - px, 2) +
                         Math.pow(node.getY() - py, 2)
         );
         if (distance < minDistance) {
            minDistance = distance;
            closest = node;
         }
      }
      return closest;
   }

   private boolean isAtEdge(Level.LevelIterator currentLevel, double px, double py, int dx) {
      int currentTileX = (int)(px / 30);
      int currentTileY = (int)(py / 30);

      // Check if there's ground below current position
      Level.TileWrapper currentGroundTile = currentLevel.getSpecificTile(currentTileX, currentTileY + 1);
      if (currentGroundTile == null || !currentGroundTile.getIsCollisionable()) {
         return false; // Not on solid ground
      }

      // Check next position based on movement direction
      if (dx > 0) { // Moving right
         Level.TileWrapper nextGroundTile = currentLevel.getSpecificTile(currentTileX + 1, currentTileY + 1);
         return nextGroundTile == null || !nextGroundTile.getIsCollisionable();
      } else if (dx < 0) { // Moving left
         Level.TileWrapper nextGroundTile = currentLevel.getSpecificTile(currentTileX - 1, currentTileY + 1);
         return nextGroundTile == null || !nextGroundTile.getIsCollisionable();
      }
      return false;
   }




   // Draw AI debugging information
   /*
   public void drawAIInfo(GraphicsContext gc) {
      theGraph.draw(gc);
   }

    */
   public void drawAIInfo(GraphicsContext gc) {
      for (Mood_Node node : theGraph.theNodes) {
         node.draw(gc);
      }

      gc.setStroke(Color.GREEN);  // Set path connection color to green
      gc.setLineWidth(3);
      if (path != null) {
         for (int i = 0; i < path.size() - 1; i++) {
            Mood_Node current = path.get(i);
            Mood_Node next = path.get(i + 1);
            gc.strokeLine(current.getX() + 8, current.getY() + 8, next.getX() + 8, next.getY() + 8);
         }
      }

      gc.setStroke(Color.BLUE);
      gc.setLineWidth(3);
      if (path != null && !path.isEmpty()) {
         Mood_Node current = path.get(path.size() - 1);
         while (current != null && current.backPointer != null) {
            gc.strokeLine(current.getX() + 8, current.getY() + 8, current.backPointer.getX() + 8, current.backPointer.getY() + 8);
            current = current.backPointer;
         }
      }
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
            // Create nodes above all tiles and breakable tiles, and at end tiles regardless of what's above
            if (tw.getIsEnd() || isThereATileThere.get(tw.getX() + "_" + (tw.getY() - 1)) == null || tw.getIsBreak()) {
               if (!tw.getIsStart() && !tw.getIsEnd()) {
                  theNodes.add(new Mood_Node(tw.getX() * 30, tw.getY() * 30 - 30));
               }
               else {
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


         // Create connections
         createConnections(graphToCreate);
      }


      private boolean isConnectionPossible(Mood_Node currentNode, Mood_Node targetNode) {
         int dx = targetNode.getX() - currentNode.getX();
         int dy = targetNode.getY() - currentNode.getY();

         // Absolute values for easier calculation
         int absDx = Math.abs(dx);
         int absDy = Math.abs(dy);

         // Check if target node is an end tile
         Level.TileWrapper targetTile = currentLevel.getSpecificTile(targetNode.getX() / 30, targetNode.getY() / 30);
         boolean targetIsEnd = targetTile != null && targetTile.getIsEnd();

         // Special handling for end tiles
         if (targetIsEnd) {
            // For end tiles, allow much more generous vertical movement, especially upward
            if (dy < 0) {  // Moving up to end tile
               return absDx <= 12 * 30 && absDy <= 180;  // Allow up to 6 tiles up
            } else {  // Moving down or horizontally to end tile
               return absDx <= 12 * 30 && absDy <= 11 * 30;
            }
         }

         // Special case: Check for vertical breaking connections first
         if (dx == 0 && dy > 0) {  // Moving straight down
            int startX = currentNode.getX() / 30;
            int startY = currentNode.getY() / 30;
            int endY = targetNode.getY() / 30;

            // Check if the tile below start is breakable
            Level.TileWrapper tileBelow = currentLevel.getSpecificTile(startX, startY + 1);
            if (tileBelow != null && tileBelow.getIsBreak()) {
               // Then check rest of path
               for (int y = startY + 2; y <= endY; y++) {
                  Level.TileWrapper tile = currentLevel.getSpecificTile(startX, y);
                  if (tile != null && tile.getIsCollisionable() && !tile.getIsBreak()) {
                     return false;  // Found non-breakable obstacle
                  }
               }
               return true;  // Path is clear or only has breakable blocks
            }
            return false;  // First block isn't breakable
         }

         // Regular movement checks
         if (dy < 0) {  // Moving up
            if (dx == 0 && absDy <= 150) return true;
            if ((absDx + 1) <= 11 * 30 && absDy <= 5 * 30) return true;
         }

         if (dy > 0) {  // Moving down normally
            double horizontalToVerticalRatio = absDx / (double) absDy;
            double hmax = ((dy / 30) / 3) + 11;
            if ((dx / 30) <= hmax) return true;
            return horizontalToVerticalRatio <= (1.0 / 3.0);
         }

         if (dy == 0) return absDx <= 11 * 30;  // Horizontal movement

         return false;
      }
      private double calculateWeight(Mood_Node currentNode, Mood_Node targetNode, ConnectionType connectionType) {
         double baseWeight = 1.0;

         if (connectionType == ConnectionType.UNIDIRECTIONAL) {
            // Higher base weight for breaking connections
            if (isBreakingConnection(currentNode, targetNode)) {
               baseWeight = 3.0;  // Breaking blocks is more costly
            } else {
               baseWeight = 2.0;  // Normal unidirectional (falling)
            }
         }

         // Vertical distance penalty
         double verticalDistance = Math.abs(currentNode.getY() - targetNode.getY());
         baseWeight += (verticalDistance / 30) * (verticalDistance / 30);

         return baseWeight;
      }

      private boolean isBreakingConnection(Mood_Node current, Mood_Node target) {
         if (current.getX() != target.getX()) return false;
         if (current.getY() >= target.getY()) return false;

         int x = current.getX() / 30;
         int startY = current.getY() / 30 + 1;
         int endY = target.getY() / 30;

         for (int y = startY; y <= endY; y++) {
            Level.TileWrapper tile = currentLevel.getSpecificTile(x, y);
            if (tile != null && tile.getIsBreak()) return true;
         }
         return false;
      }

      private void createConnections(Level.LevelIterator graphToCreate) {
         Map<String, Mood_Node> nodeMap = new HashMap<>();

         // Populate nodeMap
         for (Mood_Node node : theNodes) {
            String gridKey = (node.getX() / 30) + "_" + (node.getY() / 30);
            nodeMap.put(gridKey, node);
         }

         // Connection creation
         for (Mood_Node currentNode : theNodes) {
            int currentTileX = currentNode.getX() / 30;
            int currentTileY = currentNode.getY() / 30;

            // Search range covering all possible player movements
            //kinda working at dx -14 12
            for (int dx = -14; dx <= 13; dx++) {
               for (int dy = -30; dy <= 30; dy++) {
                  if (dx == 0 && dy == 0) continue;

                  String targetKey = (currentTileX + dx) + "_" + (currentTileY + dy);
                  Mood_Node targetNode = nodeMap.get(targetKey);

                  if (targetNode == null || targetNode == currentNode) continue;

                  // Check if connection is possible based on movement mechanics
                  if (isConnectionPossible(currentNode, targetNode)) {
                     ConnectionType connectionType = determineConnectionType(
                             graphToCreate, currentNode, targetNode, dx, dy
                     );

                     if (connectionType != null) {
                        boolean isUp = targetNode.getY() > currentNode.getY();
                        double weight = calculateWeight(currentNode, targetNode, connectionType);
                        currentNode.addConnection(targetNode, connectionType, isUp, weight);
                     }
                  }
               }
            }
         }
      }





      private boolean hasCollisionBetweenNodes(Level.LevelIterator level, Mood_Node start, Mood_Node end) {
         // Convert to tile coordinates
         int x1 = start.getX() / 30;
         int y1 = start.getY() / 30;
         int x2 = end.getX() / 30;
         int y2 = end.getY() / 30;


         //System.out.println("\n=== Connection Check Debug ===");
         //System.out.println("Checking path from (" + x1 + "," + y1 + ") to (" + x2 + "," + y2 + ")");


         // Get the actual platforms (one tile below the node positions)
         int platformY1 = y1 + 1;
         int platformY2 = y2 + 1;


         //System.out.println("Platform positions: (" + x1 + "," + platformY1 + ") to (" + x2 + "," + platformY2 + ")");


         // First, verify there are actually platforms at both ends
         Level.TileWrapper startPlatform = level.getSpecificTile(x1, platformY1);
         Level.TileWrapper endPlatform = level.getSpecificTile(x2, platformY2);

         boolean validPlatforms = startPlatform != null && startPlatform.getIsCollisionable() &&
                 endPlatform != null && endPlatform.getIsCollisionable();


         //if(!currentLevel.getSpecificTile(start.getX(), start.getY()).getIsEnd() || !  currentLevel.getSpecificTile(end.getX(), end.getY()).getIsEnd() ){

         Level.TileWrapper targetTile = currentLevel.getSpecificTile(x1, y1);
         Level.TileWrapper targetTile2 = currentLevel.getSpecificTile(x2, y2);


         // Check if it's an end tile
         if ((targetTile != null && !targetTile.getIsEnd()) || targetTile2 != null && !targetTile2.getIsEnd()) {
            if (!validPlatforms) {
               // System.out.println("Missing platform at start or end");
               return true;
            }

         }

         // Check if there are any solid blocks in the path
         int minX = Math.min(x1, x2);
         int maxX = Math.max(x1, x2);
         int minY = Math.min(y1, y2);
         int maxY = Math.max(y1, y2);

         // For horizontal movement, check if any blocks are in the way

         // System.out.println("test-2");
         if (y1 == y2) {
            //System.out.println("test0");

            for (int x = minX; x <= maxX; x++) {
               //System.out.println("test1");
               // Check the level of platforms and one above
               for (int y = y1; y <= platformY1; y++) {

                  //System.out.println("test3");
                  if (x != x1 && x != x2) {  // Don't check start and end positions
                     Level.TileWrapper tile = level.getSpecificTile(x, y);
                     if (tile != null && tile.getIsCollisionable()) {
                        //System.out.println("Horizontal collision found at: (" + x + "," + y + ")");

                        return true;
                     }
                  }
               }
            }
         } else {
            // For diagonal or vertical movement
            int dx = Math.abs(x2 - x1);
            int dy = Math.abs(y2 - y1);
            int stepX = x1 < x2 ? 1 : -1;
            int stepY = y1 < y2 ? 1 : -1;

            int error = dx - dy;
            int x = x1;
            int y = y1;

            while (true) {
               Level.TileWrapper currentTile = level.getSpecificTile(x, y);

               //System.out.println("test4");


               // Only consider collision if the tile is NOT part of either platform
               if (currentTile != null && currentTile.getIsCollisionable()) {
                  //System.out.println("test5");
                  if (!((x == x1 && y == platformY1) || (x == x2 && y == platformY2))) {

                     //System.out.println("Diagonal/vertical collision found at: (" + x + "," + y + ")");


                     if (Math.abs(x2 - x1) < 2 && platformY2 >= platformY1 + 4 && x == x1 && platformY2 <= platformY1 + 10) {
                        //do nothing i guess
                        int dfx = Math.abs(x1 - x2);
                        int dfy = Math.abs(platformY1 - platformY2);

                        for (int jx = Math.min(x1, x2); jx <= Math.max(x1, x2); jx++) {
                           for (int jy = Math.min(platformY1, platformY2); jy <= Math.max(platformY1, platformY2); jy++) {
                              // Skip the start and end points
                              if (!(jx == x1 && jy == platformY1) && !(jx == x2 && jy == platformY2)) {
                                 Level.TileWrapper tile = level.getSpecificTile(jx, jy);
                                 if (tile != null && tile.getIsCollisionable() && !tile.getIsEnd()) {
                                    // If we find a solid block in our path, block the movement
                                    // Unless this is a purely vertical movement and we're checking the destination column
                                    if (!(x1 == x2 && jx == x2)) {
                                       return true; // Collision found
                                    }
                                 }
                              }
                           }
                        }
                     } else {
                        return true;
                     }
                  }
               }

               if (x == x2 && y == y2) break;

               int e2 = 2 * error;
               if (e2 > -dy) {
                  error -= dy;
                  x += stepX;
               }
               if (e2 < dx) {
                  error += dx;
                  y += stepY;
               }
            }
         }

         //if goal or start is boxed in dont

         //level.getSpecificTile(x1, platformY1)
         //level.getSpecificTile(x2, platformY2)

         //start boxed
         //tile above

         boolean ta = level.getSpecificTile(x1, platformY1 - 2) != null;
         //tile left
         boolean tl = level.getSpecificTile(x1 - 1, platformY1 - 1) != null;
         //tile right
         boolean tr = level.getSpecificTile(x1 + 1, platformY1 - 1) != null;

         if (ta && tl && tr) {
            return true;
         }
         if (ta && tl && x2 < x1 && Math.abs(y1 - y2) < 12) {
            return true;
         }
         if (ta && tr && x2 > x1 && Math.abs(y1 - y2) < 12) {
            return true;
         }

         //if theres blocks to the left or right on same y only go to those unless its a jump up
         
         //boolean taa = level.getSpecificTile(x1, platformY1-2) !=null;
         //tile left
         boolean al = level.getSpecificTile(x1 - 1, platformY1) != null;
         //tile right
         boolean ar = level.getSpecificTile(x1 + 1, platformY1) != null;



         if (platformY2 > platformY1) { //goal is below
            if (al && ar) {

               //make sure goal isnt these adjacents
               if (x2 != x1 + 1 || x2 != x1 - 1) {
                  return true;
               }
            }
            //if goal is below and right and theres blocks adjacent right to start
            if (ar && x2 > x1) {
               return true;
            }
            if (al && x2 < x1) {
               return true;
            }
         }
         
 boolean isEnd = false;
if(level.getSpecificTile(x2, y2)!=null){
   isEnd = level.getSpecificTile(x2, y2).getIsEnd();
}


if(!isEnd){


         //if goal has two on either side dont
         //tile left
         boolean dl = level.getSpecificTile(x2 - 1, platformY2) != null;
         //tile right
         boolean dr = level.getSpecificTile(x2 + 1, platformY2) != null;


         if (platformY2 < platformY1) { //goal is above
            if (dl && dr) {
               //make sure goal isnt these adjacents
               if (x2 != x1 + 1 || x2 != x1 - 1) {
                 return true;
               }
            }
            if (dr && x1 > x2) {
               return true;
            }
            if (dl && x1 < x2) {
               return true;
            }
         }
         }
         


         //corner

         boolean cb = level.getSpecificTile(x1, platformY1 + 1) != null;
         boolean cl = level.getSpecificTile(x1 - 1, platformY1) != null;
         boolean cr = level.getSpecificTile(x1 + 1, platformY1) != null;
         boolean ca = level.getSpecificTile(x1, platformY1-1) != null;

         
         //bottom corner
         //above e to the right
         if(ca && cr){
          //if goal is below+right.
            if (x1 + 2 != x2) {
               if (x2 == x1 + 1 && platformY2 == platformY1 - 2) {
                  return true;
               }
               if (x2 > x1) {
                  if (platformY2 != platformY1) {
                     return true;
                  }
               }
            }
         
         }
         
         //above e to the left
         if (ca && cl) {

            // if cl isnt goal
            if (x1 - 1 != x2) {
               //if goal is below+left.
               if (x2 == x1 - 1 && platformY2 == platformY1 - 2) {
                  return true;
               }
               if (x2 < x1) {
                 return true;
               }
            }
         }
         
         

         // if theres a block .below and to the .left of start
         if (cb && cl) {

            // if cl isnt goal
            if (x1 - 1 != x2) {
               //if goal is below+left.
               if (x2 == x1 - 1 && platformY2 == platformY1 + 2) {
                  return true;
               }
               if (x2 < x1) {
                 return true;
               }
            }
         }
         if (cb && cr) {
            //if goal is below+right.
            if (x1 + 2 != x2) {
               if (x2 == x1 + 1 && platformY2 == platformY1 + 2) {
                  return true;
               }
               if (x2 > x1) {
                  if (platformY2 != platformY1) {
                     return true;
                  }
               }
            }

         }


         //goal boxed
         //tile above
         boolean ga = level.getSpecificTile(x2, platformY2 - 2) != null;
         //tile left
         boolean gl = level.getSpecificTile(x2 - 1, platformY2 - 1) != null;
         //tile right
         boolean gr = level.getSpecificTile(x2 + 1, platformY2 - 1) != null;

         if (ga && gl && gr) {
            return true;
         }
         /*
         if(ga && gl && x1 < x2 && Math.abs(y2-y1)<12){
            return true;
         }
         if(ta && tr && x2 > x1 && Math.abs(y1-y2)<12){
            return true;
         }

          */


         // if(level.getSpecificTile(x1, platformY1))


         //if theres blocks left and right of start dont go to >= x


         //if theres blocks left and right of goal dont got to it?


         //if block is below dont go

         if(x1==x2){
            if(!isEnd ){

               return true;
            }
         }


         // System.out.println("No collisions found, connection possible");

         return false;
      }

      private boolean isPotentialJumpPath(int dx, int dy) {
         // Maximum horizontal distance for a jump
         final int MAX_JUMP_HORIZONTAL = 3;
         // Maximum vertical distance for a jump (negative is upward)
         final int MAX_JUMP_UP = -4;
         final int MAX_FALL_DOWN = 6;

         // Check if movement is within jump/fall parameters
         return dx <= MAX_JUMP_HORIZONTAL &&
                 dy >= MAX_JUMP_UP &&
                 dy <= MAX_FALL_DOWN;
      }









      private ConnectionType determineConnectionType(
              Level.LevelIterator level,
              Mood_Node start,
              Mood_Node end,
              int dx,
              int dy
      ) {
         // Only allow breaking from above - check if start node is directly above a breakable block
         if (dx == 0 && dy > 0) {  // Moving straight down
            int startX = start.getX() / 30;
            int startY = start.getY() / 30;
            int endY = end.getY() / 30;

            // Get the tile directly below the start node
            Level.TileWrapper tileBelow = level.getSpecificTile(startX, startY + 1);

            // Only proceed if the tile below start is breakable
            if (tileBelow != null && tileBelow.getIsBreak()) {
               boolean validBreakPath = true;

               // Check the rest of the path
               for (int y = startY + 1; y <= endY; y++) {
                  Level.TileWrapper tile = level.getSpecificTile(startX, y);
                  if (tile != null && tile.getIsCollisionable() && !tile.getIsBreak()) {
                     validBreakPath = false;  // Found a non-breakable block in path
                     break;
                  }
               }

               if (validBreakPath) {
                  return ConnectionType.UNIDIRECTIONAL;
               }
            }
         }

         // Rest of  existing connection logic for normal movement
         if (hasCollisionBetweenNodes(level, start, end)) {
            return null;
         }

         double horizontalDistance = Math.abs(end.getX() - start.getX());
         double verticalDistance = start.getY() - end.getY();
         double maxHorizontalReach = calculateHorizontalReach(verticalDistance);

         // Check if target is an end tile
         Level.TileWrapper targetTile = level.getSpecificTile(end.getX() / 30, end.getY() / 30);
         boolean isEndTile = targetTile != null && targetTile.getIsEnd();

         double verticalLimit = isEndTile ? 180 : 6 * 30;

         if (Math.abs(verticalDistance) <= verticalLimit) {
            if (canWalkBetweenTiles(level, start.getX() / 30, start.getY() / 30, end.getX() / 30, end.getY() / 30) ||
                    canJumpHorizontally(level, start.getX() / 30, start.getY() / 30, end.getX() / 30, end.getY() / 30, dx, dy)) {
               return ConnectionType.BIDIRECTIONAL;
            }
         }

         if (horizontalDistance <= maxHorizontalReach) {
            if (canFallBetweenTiles(level, start.getX() / 30, start.getY() / 30, end.getX() / 30, end.getY() / 30, dx, dy)) {
               return ConnectionType.UNIDIRECTIONAL;
            }
         }

         return null;
      }

      // Dynamic horizontal reach calculation based on vertical drop
      private double calculateHorizontalReach(double verticalDrop) {
         // Base horizontal reach
         double baseReach = 11 * 30; // 11 blocks horizontally

         // Adjust reach based on drop
         if (verticalDrop == 0) {
            return baseReach; // Full horizontal reach on same level
         } else if (verticalDrop > 10 && verticalDrop <= 55) {
            return baseReach - (30); // Full horizontal reach on same level
         }
         if (verticalDrop > 55 && verticalDrop < 85) {
            return baseReach - (65); // Full horizontal reach on same level
         }
         if (verticalDrop > 85 && verticalDrop < 115) {
            return baseReach - (90); // Full horizontal reach on same level
         }
         if (verticalDrop > 115 && verticalDrop < 145) {
            return baseReach - (90); // Full horizontal reach on same level
         }

         if (verticalDrop < 0) {

            // As you drop, you can go further horizontally
            // Implement a curve that allows more horizontal movement with drop
            double reachFactor = 0.79 + (Math.abs(verticalDrop) / 30 * 0.2); // Increase reach by 20% per block dropped

            //System.out.println("reachFactor: "+reachFactor);
            return Math.min(baseReach * reachFactor, 30 * 20); // Cap at 20 blocks
         }
         return baseReach;
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
         //System.out.println("finding jump: ");
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
         //return maxHeight * (4 * t * (1 - t));

         return 5;

      }


      //drawing the graph
      public void draw(GraphicsContext gc) {
         for (int i = 0; i < theNodes.size(); i++) {
            theNodes.get(i).draw(gc);
         }
      }

      public boolean updateBreak(Level.LevelIterator currentLevel) {
         boolean anyBlockBroke = false;

         for (int i = 0; i < breakNodes.size(); i++) {
            Level.TileWrapper tw = currentLevel.getSpecificTile(
                    breakNodes.get(i).getX() / 30,
                    (breakNodes.get(i).getY() + 30) / 30
            );

            if (tw == null) {  // Block broke
               breakNodes.remove(i);
               i--;
               anyBlockBroke = true;
            } else {  // Update timer
               breakNodes.get(i).setBreakAmount(tw.getBreakTimer());
            }
         }

         return anyBlockBroke;
      }


      //takes in tilespace points
      public void clickPoint(int x, int y) {

         //this is because the the nodes are the upper left coords and the x and y are the center.
         x -= 15;
         y -= 15;

         for (int i = 0; i < theNodes.size(); i++) {
            Mood_Node n1 = theNodes.get(i);

            double d = Math.sqrt((n1.getX() - x) * (n1.getX() - x) + (n1.getY() - y) * (n1.getY() - y));


            //if distance is within 20 px of the clicked tile.
            if (d < 20) {


               if (start == null) {
                  start = n1;
                  n1.clicked(1);
               } else if (end == null) {
                  end = n1;
                  n1.clicked(0);

                  //aStar(start,end);
                  dijkstra(start, end);
               } else {
                  n1.clicked(-1);
               }
            }
         }
      }


      Mood_Node start = null;
      Mood_Node end = null;

      // Calculate the cost between two connected nodes based on Euclidean distance
      // Calculate the cost between two connected nodes
      private double calculateEdgeCost(Mood_Node current, Mood_Node neighbor, NodeConnection connection) {
         // Calculate vertical and horizontal distances
         double verticalDist = Math.abs(neighbor.getY() - current.getY());
         double horizontalDist = Math.abs(neighbor.getX() - current.getX());

         // Base cost is the Euclidean distance
         double baseCost = Math.sqrt(verticalDist * verticalDist + horizontalDist * horizontalDist);

         // Add penalties based on movement type
         if (connection.connectionType == ConnectionType.UNIDIRECTIONAL) {
            // If it's a fall, make it slightly cheaper to encourage using gravity
            baseCost *= 0.8;
         } else {
            // Add penalties for both vertical and horizontal distances
            baseCost += (verticalDist / 30) * 10; // Vertical penalty: 10 units per tile
            baseCost += ((horizontalDist / 30) * 5) * ((horizontalDist / 30) * 5);  // Horizontal penalty: 5 units per tile
         }

         // Add direction change penalty if applicable
         if (current.getBackPointer() != null) {
            // Calculate vectors for current and previous movements
            double prevDX = current.getX() - current.getBackPointer().getX();
            double prevDY = current.getY() - current.getBackPointer().getY();
            double currentDX = neighbor.getX() - current.getX();
            double currentDY = neighbor.getY() - current.getY();

            // Calculate angle between movements (in radians)
            double angleChange = Math.atan2(currentDY, currentDX) - Math.atan2(prevDY, prevDX);
            double absoluteAngleChange = Math.abs(angleChange);

            // Add a smaller direction change penalty
            baseCost += absoluteAngleChange * 15;
         }

         return baseCost;
      }

      private boolean hasObstacleProximity(Mood_Node current, Mood_Node neighbor) {
         // Example: Check for obstacles near the target node
         Level.TileWrapper above = currentLevel.getSpecificTile(neighbor.getX() / 30, (neighbor.getY() / 30) - 1);
         return above != null && above.getIsCollisionable();
      }


      private boolean areAdjacent(Mood_Node a, Mood_Node b) {
         return (Math.abs(a.getX() - b.getX()) == 30 && a.getY() == b.getY()) || // Horizontal adjacency
                 (Math.abs(a.getY() - b.getY()) == 30 && a.getX() == b.getX());   // Vertical adjacency
      }


      // Reconstruct and mark the path from start to end
      private void reconstructPath(Mood_Node start, Mood_Node end) {
         Mood_Node current = end;

         while (current != start && current != null) {
            current.clicked(2); // Mark path nodes blue
            current = current.backPointer;
         }

         if (current != null) {
            current.clicked(2); // Mark start node
         } else {
            //System.out.println("Failed to reconstruct path");
         }
      }

      public ArrayList<Mood_Node> dijkstra(Mood_Node start, Mood_Node end) {
         // Priority queue to store nodes to explore, sorted by distance
         PriorityQueue<Mood_Node> priorityQueue = new PriorityQueue<>((a, b) -> Double.compare(a.distance, b.distance));

         // Reset node states
         for (Mood_Node node : theNodes) {
            node.distance = Double.POSITIVE_INFINITY;
            node.setBackPointer(null);
         }

         // Initialize start node
         start.distance = 0;
         priorityQueue.add(start);

         while (!priorityQueue.isEmpty() && !priorityQueue.peek().equals(end)) {
            Mood_Node current = priorityQueue.poll();

            // Explore neighbors
            for (NodeConnection connection : current.connections) {
               Mood_Node neighbor = connection.node;
               boolean isValidConnection = connection.connectionType == ConnectionType.BIDIRECTIONAL
                       || (connection.connectionType == ConnectionType.UNIDIRECTIONAL && current.getY() < neighbor.getY());
               // || currentLevel.getSpecificTile(neighbor.getX() / 30, neighbor.getY() / 30).getIsEnd();

               if (isValidConnection) {
                  double tentativeDistance = current.distance + calculateEdgeCost(current, neighbor, connection);
                  if (tentativeDistance < neighbor.distance) {
                     neighbor.distance = tentativeDistance;
                     neighbor.setBackPointer(current);

                     // Update the priority queue if the node is already present
                     if (priorityQueue.contains(neighbor)) {
                        priorityQueue.remove(neighbor);
                     }
                     priorityQueue.add(neighbor);
                  }
               }
            }
         }

         // Reconstruct the path
         ArrayList<Mood_Node> path = new ArrayList<>();
         Mood_Node current = end;
         while (current != null) {
            path.add(0, current);
            current.clicked(2);
            current = current.getBackPointer();
         }

         return path;
      }
   }

   // enum to represent connection types
   public enum ConnectionType {
      BIDIRECTIONAL,  // Can move in both directions (within jump/walk range)
      UNIDIRECTIONAL  // Can only move in one direction (fall-only)
   }

   public class NodeConnection {
      Mood_Node node;
      ConnectionType connectionType;
      boolean isUp;
      double weight; // Add this line

      NodeConnection(Mood_Node node, ConnectionType type, boolean isUp, double weight) {
         this.node = node;
         this.connectionType = type;
         this.isUp = isUp;
         this.weight = weight;
      }

      public boolean getIsUp() {
         return this.isUp;
      }
   }
   public class Mood_Node {


      double distance = Double.POSITIVE_INFINITY;

      // Store connections with their types

      //connections between nodes
      ArrayList<NodeConnection> connections = new ArrayList<>();


      public void print() {
         //  System.out.println("x: "+x+" y: "+ y+" Distance: "+this.distance );
      }


      int x, y;

      //for IDs...
      int id;
      static int idgen = 0;

      double currentBreakAmount = 0; //in my program -10 on a tile means not broken or not breakable (use tw.getIsBreak() to deteremine the diff). -9 in my implementaion means not breakable. and a positive number is how much time is left
      double maxBreakAmount = 0;

      public Mood_Node(int _x, int _y) {
         x = _x;
         y = _y;

         id = idgen++;
      }

      public int getX() {
         return x;
      }

      public int getY() {
         return y;
      }

      //name of a tile is x_y
      public String getName() {
         return x + "_" + y;
      }

      public void addConnection(Mood_Node toAdd, ConnectionType type, boolean isUp, double weight) {
         connections.add(new NodeConnection(toAdd, type, isUp, weight));
      }

      //for keeping track of break tiles.
      public void setBreakAmount(double d) {
         currentBreakAmount = d;
      }

      public void setBreakMax(double d) {
         maxBreakAmount = d;
      }

      //these methods were for dijsktra
      public int getSize() {
         return connections.size();
      }

      public boolean isInQueue() {
         return inQueue;
      }

      public void setIsInQueue(boolean val) {
         inQueue = val;
      }

      boolean inQueue = false;

      public NodeConnection get(int i) {
         return connections.get(i);
      }

      Color fillColor = Color.YELLOW;


      //clicked method to change colors. this is really for debugging
      public void clicked(int option) {

         fillColor = new Color(0, 1, 0, 1);

         if (option == 0) {
            fillColor = Color.PINK;
         }
         if (option == 1) {
            fillColor = Color.PURPLE;
         }
         if (option == 2) {
            fillColor = Color.BLUE;
         }
      }

      public void draw(GraphicsContext gc) {

         //draw all this nodes's connections. NOTE: my implementation doesn't remove connections from each node when a node breaks.
         for (int i = 0; i < connections.size(); i++) {
            if (connections.get(i).connectionType == ConnectionType.BIDIRECTIONAL) {
               gc.setStroke(Color.RED);
               //gc.setStroke(Color.TRANSPARENT);

               double weightX = (x + connections.get(i).node.getX()) / 2;
               double weightY = (y + connections.get(i).node.getY()) / 2;
               if (weightY > 2) {
                  String weightText = String.format("%.2f", connections.get(i).weight);
                  gc.setFill(Color.WHITE);
                  gc.fillText(weightText, weightX, weightY);
               }


            } else {
               gc.setStroke(Color.ORANGE);
               //gc.setStroke(Color.TRANSPARENT);
            }


            gc.setLineWidth(3);
            gc.strokeLine(x + 8 + 7, y + 8 + 7, connections.get(i).node.getX() + 8 + 7, connections.get(i).node.getY() + 8 + 7);
         }

         if (currentBreakAmount == -10) { //-10 means not stepped on or not breakable. You can do getIsBreak() from the tw if you want.
            gc.setFill(fillColor);
         } else if (currentBreakAmount == -9) {
            gc.setFill(fillColor);
         } else {
            gc.setFill(Color.BLACK.interpolate(Color.WHITE, currentBreakAmount / maxBreakAmount)); //color based on break amount %
         }

         gc.fillOval(x + 8, y + 8, 14, 14);
         gc.fillText((x + " " + y), x, y); // Filled text
      }

      Mood_Node backPointer = null;

      public void setBackPointer(Mood_Node theThing) {
         backPointer = theThing;
      }

      public Mood_Node getBackPointer() {
         return backPointer;
      }


   }
}




