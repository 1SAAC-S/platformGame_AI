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

//this class represents a player that is controlled by AI.

public class AIPlayer extends Player
{
   AI myAI;
   
   //passing in the AI to the constructor
   public AIPlayer(double x, double y, AI theAI)
   {
      super(x,y);
      
      myAI = theAI;
   }
   
   //draws the player using the AI's info
   public void drawMe(GraphicsContext gc)
   {
      gc.setFill(myAI.getFill());
      gc.fillRect(x,y,30,30);
      
      gc.setStroke(Color.WHITE);
      gc.strokeText(myAI.getName(),x,y);
      
      myAI.drawAIInfo(gc); //this call should draw whatever debug info you want.
   }
   
   //these methods are how the AI will control the AIPlayer
   public boolean isADown()
   {
      return myAI.isADown();
   }
   
   public boolean isDDown()
   {
      return myAI.isDDown();
   }
   
   public boolean isJumpDown()
   {
      return myAI.isJumpDown();
   }
   
   public void jumped()
   {
      myAI.jumped();
   }
   
   //these methods just call the AI's methods with specific parameters.
   public void runEachTick(Level currentLevel, Player p)
   {
      myAI.runEachTick(currentLevel.getLevelIterator(),p.getX(),p.getY(),p.getXVel(), p.getYVel(), p.getJump(),p.isGrounded());
   }

   
   public void start(Level currentLevel, Player p)
   {
      myAI.start(currentLevel.getLevelIterator(),p.getX(),p.getY());
   }
   
   public String getName()
   {
      return myAI.getName();
   }
   
   public void clicked(int x, int y)
   {
      myAI.clicked(x,y);
   }
}