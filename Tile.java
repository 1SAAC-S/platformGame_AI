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


public class Tile
{
   Color c;
   Color outline;
   String name;
   String fname;
   int x; //tilespace, not pixil space
   int y;
   
   //image for the background.
   Image img;
   ImagePattern ptn;
   
   //head of the decoration list.
   TileDecoration decs;
   
   boolean isStartTile=false;
   boolean isEndTile = false;
   
   boolean isColisionAble;
   
   //this tile's tilewrapper.
   Level.TileWrapper tw = new Level.TileWrapper();

   //create the initial tiletypes templates
   public Tile(Color theColor,Color outlineColor, int x, int y, String name, String fname, boolean isColisionAble,TileDecoration decs)
   {
      this.x = x;
      this.y = y;
      c = theColor;
      outline = outlineColor;
      
      this.name = name;
      this.fname = fname;
      
      //System.out.println("Filename: "+fname);
      img = new Image(fname); // Make sure the path is correct

        // Create an ImagePattern with the image
      ptn = new ImagePattern(img);
      
      this.decs = decs;
      this.isColisionAble = isColisionAble;
      
      TileDecoration td = decs;
      
      while(decs!= null)
      {
         if(decs instanceof StartDecorator)
         {
            isStartTile = true;
         }
         if(decs instanceof GoalDecorator)
         {
            isEndTile = true;
         }
         decs = decs.getNext();
      }
   }
   
   
   boolean ontop = false;
   
   //called if a thing is ontop of this tile. Called from GameObject.
   public void trigger()
   {
      if(Main.isLocked())
      {
         System.out.println("Method may not be called from AI");
         return;
      }
   
      ontop = true;
      //outline = Color.PINK;
   }
   
   //run the tile (i.e. the break decorations)
   public boolean run(ArrayList<Player> players, double timeElapsed)
   {
      if(Main.isLocked())
      {
         System.out.println("Method may not be called from AI");
         return false;
      }
   
      if(decs != null && decs.run(players, ontop, timeElapsed)) ///if it is true then remove this tile. The remove happens elsewhere.
      {
         return true;
      }
      
      ontop=false;
      return false;
   }
   
   
   //copying a tile into a specific position
   public Tile(Tile other, int x, int y)
   {
      this.x = x;
      this.y = y;
      c = other.c;
      outline = other.outline;
      
       name = other.name;
       fname = other.fname;
       
       this.img = other.img;
       this.ptn = other.ptn;
       
       if(other.decs != null)
       {
         this.decs = other.decs.duplicate(this);
       }  
       
       this.isStartTile = other.isStartTile;
       this.isEndTile = other.isEndTile;
       this.isColisionAble = other.isColisionAble;
   }
   
   //copy constructor.
   public Tile(Tile other)
   {
      this.x = other.x;
      this.y = other.y;
      c = other.c;
      outline = other.outline;
      
       name = other.name;
       fname = other.fname;
       
       this.img = other.img;
       this.ptn = other.ptn;
       
       this.decs = other.decs;
       
       this.isStartTile = other.isStartTile;
       this.isEndTile = other.isEndTile;
       this.isColisionAble = other.isColisionAble;
       
       if(other.decs != null)
       {
         this.decs = other.decs.duplicate(this); 
       }
   }
   
   public String getName()
   {
      return name;
   }
   
      public String getFName()
   {
      return fname;
   }
   
   public Color getFill()
   {
      return c;
   }
   public Color getOutline()
   {
      return outline;
   }
   
   public TileDecoration getTileDecoration()
   {
      if(Main.isLocked())
      {
         System.out.println("Method may not be called from AI");
         return null;
      }
   
      return decs;
   }
   
   public boolean getCollision()
   {
      return isColisionAble;
   }
   
   public boolean getIsStart()
   {
      return isStartTile;
   }
   
   //write the tiletype to a file
   public void write(PrintWriter writer)
   {
         if(Main.isLocked())
         {
            System.out.println("Method may not be called from AI");
            return;
         }
   
         writer.print(getName()+" "+getFill().getRed() +" "+
         getFill().getGreen() +" "+getFill().getBlue() +" " +
         getFill().getOpacity() +" "+getFill().getRed() +" "+
         getOutline().getGreen() +" "+getFill().getBlue() +" " +
         getOutline().getOpacity() +" " +getFName()+" "+isColisionAble+" ");
      
         writer.print(TileDecoration.countNumber(getTileDecoration())+" ");
         
         TileDecoration head = getTileDecoration();
         
         while(head != null)
         {
            head.save(writer);
            head = head.getNext();
            
            //System.out.println("writing thing...");
         }
      
         writer.println();   
   }

   //different draw methods for different purposes.
   public void drawme(GraphicsContext gc)
   {
      //this is to not draw the start tile
      if(isStartTile && Main.state == Main.GameState.GAME)
      {
         return;
      }
   
      //background
      gc.setFill(c);
      gc.fillRect(x*30,y*30,29,29);
      
      //fill
       gc.setFill(ptn);
      gc.fillRect(x*30,y*30,29,29);
           
      //outline
      gc.setStroke(outline);
      gc.strokeRect(x*30,y*30,29,29);
   }
   
   //draw at specific x and y without the multiplication of the x and y
   public void drawmeAbsolute(GraphicsContext gc, int x, int y)
   {
      gc.setFill(c);
      gc.fillRect(x,y,30,30);
      
             gc.setFill(ptn);
      gc.fillRect(x,y,30,30);
      
      gc.setStroke(outline);
      gc.strokeRect(x,y,30,30);
   }
   
   //draw absolute but also draw a selected outline
   public void drawmeAbsoluteSelected(GraphicsContext gc, int x, int y)
   {
      gc.setFill(c);
      gc.fillRect(x,y,30,30);
      
      gc.setFill(ptn);
      gc.fillRect(x,y,30,30);
      
      gc.setStroke(outline);
      gc.strokeRect(x,y,30,30);  
       
      gc.setStroke(Color.WHITE);
      gc.strokeRect(x-1,y-1,32,32);   
   }
   
   //draw the gold hover over a particular spot.
   public void drawmeAbsoluteHover(GraphicsContext gc, int x, int y)
   {
       
      gc.setStroke(Color.GOLD);
      gc.strokeRect(x-2,y-2,34,34);   
   }
   
   
   //return x and y
   public int getX(){return x;}
   public int getY(){return y;}
   
   
   //return the wrapper of the tile. Note that I reset all to info when this method is called.
   public Level.TileWrapper getWrapper()
   {
      tw.setX(x);
      tw.setY(y);
      tw.setIsStart(isStartTile);
      tw.setIsEnd(isEndTile);
      tw.setIsBreak(false);
      tw.setBreakTimer(-10); //-10 means not breakable
      tw.setMaxBreakTimer(-10); //-10 means not breakable
      tw.setName(name);
      tw.setIsCollisionable(isColisionAble);
     
      
      TileDecoration td = decs;
      
      //iterate over the decorators to grab the break tile time.
      while(td != null)
      {
         if(td instanceof BreakTileDecoration)
         {
            tw.setIsBreak(true);
            tw.setBreakTimer(((BreakTileDecoration)td).getTimeLeft());  
             tw.setMaxBreakTimer(((BreakTileDecoration)td).getMaxTimeLeft());
         }
         td = td.getNext();
      }
      
      return tw;
   }
}