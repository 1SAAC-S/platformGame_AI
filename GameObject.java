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



public abstract class GameObject
{
   //vars for this game object
   protected double x, y;
   protected double xvelocity,yvelocity;
   
   //can hit things. Not used in this project.
   protected boolean willHit=true;
   
   //how much time elapsed
   protected float totalTime=0;
   
   //for jumping
   double jump=0;
   
   public GameObject(double x, double y)
   {
      this.x = x;
      this.y = y;
   }
   
   
   //varis if the AI is gounded   
   boolean grounded=true;
   
   public boolean isGrounded()
   {
      return grounded;
   }
   
   //starting a jump
   public void jump(double speed)
   {
      jump = speed;
   }
   
   //run each .01 seconds
   public void move(Level theLevel, Player thePlayer, Main theMain)
   {

      //count the times called.
      totalTime++;
   
      //lock out the AIs from calling certain methods. Not used anymore.
      theMain.lock();
      ((Player)thePlayer).runEachTick(theLevel, thePlayer);
      theMain.unlock();
      
      //check if the AI wants to jump
      if(((Player)this).isJumpDown())
      {
         if(this.isGrounded())
         {
            this.jump(10);
         }
         
         //inform the AI he jumped (or tried to jump).
         ((Player)this).jumped();
      }
      
      //get the AI's current control setup for left and right
      int leftright = ((((Player)this).isADown() ? -1 : 0) + (((Player)this).isDDown() ? 1 : 0 ));
   
      //handle jump
      jump*=.95f;
   
     //no negative jump
      if(jump < 0)
         jump = 0;
      
      //accelerate
      xvelocity += (leftright*.4f);
      
      //max speed for left and right (NOTE: the player can go above this on the push mech) this just limits the left/right from controls.
      if(xvelocity > 3)
      {
         xvelocity = 3;
      }
      else if(xvelocity < -3)
      {
         xvelocity = -3;
      }
      
      //reduce speed if neither left or right is pressed. Again, you will have to account for push, etc.
      if(leftright==0)
      {
         xvelocity *=.9f;
      }
      
      //get rid of gravity if jumping. gravity is pesky! NOTE: you cannot use this exact code once push / pull implemented
      if(jump > 1 && yvelocity < -0.1f)
      {
         yvelocity = 0;
      }
      
      if(jump > 1)
      {
         yvelocity = jump; //jump is alreday limited by the elapsed time above
      }

      //gravity
      yvelocity -= .15;
      
      
      //max velocity (neg). positive has no max velocity
      if(yvelocity < -12 )
      {
         yvelocity = -12;
      }
      
      //housekeeping for doubles. set to 0 if close to 0.
      if(Math.abs(yvelocity) < 0.01)
      {
         yvelocity = 0;
      }
      if(Math.abs(xvelocity) < 0.01)
      {
         xvelocity = 0;
      }

      //get where the game object should move to if there is nothing in the way.
      double nx = x+xvelocity, ny = y-yvelocity;
      
      //NOTE: my implementaion doesn't try to handle to problem of being able to move "halfway". Its all or nothing.
      
      //check if the player can move to the left/right. overlap returns what tiles it overlapped
      if(overlap((int)nx,(int)y, theLevel).size() != 0)
      {
         //if not set the xvel to 0
         xvelocity = 0;
      }
      else
      {
         //if yes then say the player moved to that new x
         x = nx;
      }
      
      
      //check new y position. getting what tiles it overlapped
      if(overlap((int)x,(int)ny, theLevel).size() != 0)
      {
         //if it hit something...
         yvelocity = 0;
         jump = 0;
         
        
         
         if(ny >= y-.001) //not falling
         {
            //if it hit a floor
         
            grounded = true;
         }  
         else
         {
            //this is if it a ceiling
            grounded = false;
         }
         
         //this code is only for break tiles in this implementation
         for(int j=0;j<onTopOf.size();j++)
         {
            if(y < onTopOf.get(j).getY()*30)
            {
               onTopOf.get(j).trigger();
            }
         }
      }
      else //so it doesn't overlap anything
      {
         if(Math.abs(y -ny) < 0.001)
         {
         }
         else
         {
            grounded = false;
         }
      
         //move the player through the y!
         y = ny;
      }
      
   }
   
   public abstract void drawMe(GraphicsContext gc);
   
   public double getX()
   {
      return x;
   }
   public double getY()
   {
      return y;
   }
   
   
   //this is for the overlap method. 
   private static ArrayList<Tile> onTopOf = new ArrayList<Tile>();
   
   public static ArrayList<Tile> overlap(int playerx, int playery, Level theLevel)
   {
      onTopOf.clear();
   
      //only check nearby tiles.
      for(int i=playerx/30-1;i<=playerx/30+1;i++)
      {
         for(int j=playery/30-1;j<=playery/30+1;j++)
         {
            //same as calc'ing the postiion in the chunk and tile (combined)
            int chunkx = i/5;
            int chunky = j/5;
            
            if(i<0)
            {
               chunkx--;
            }
            if(j<0)
            {
               chunky--;
            }
            
            String chunk = chunkx+"_"+chunky;
            
            
            Chunk c = theLevel.getMap().get(chunk);
            
            if(c == null)
            {
               continue;
            }
            
            //get particular tile.
            Tile t = c.getTileMod(i,j);
            
            //if the player overlaps with this tile then add to the list.
            if(t!= null && t.getCollision() && squaresOverlap(playerx, playery, t.getX()*30,t.getY()*30))
            {
               onTopOf.add(t);
            }     
         }
      }
      
      return onTopOf;
   }
   
   //from gpt
   public static boolean squaresOverlap(int x1, int y1, int x2, int y2) {
        // Calculate the boundaries of the squares
        int square1Right = x1 + 30;
        int square1Bottom = y1 + 30;
        int square2Right = x2 + 30;
        int square2Bottom = y2 + 30;

        // Check if the squares overlap
        return (x1 < square2Right && square1Right > x2 && 
                y1 < square2Bottom && square1Bottom > y2);
    }


   public String getName()
   {
      return "forgot to override getName() :(";
   
   }
   
   //get velocities
   public double getXVel()
   {
      return xvelocity;
   }
   
   public double getYVel()
   {
      return yvelocity;
   }
   
   //get current jump amount
   public double getJump()
   {
      return jump;
   }
}