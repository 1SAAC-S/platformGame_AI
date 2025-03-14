import java.util.HashMap;
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

//my implementation of a class to hold chunks.

public class Level
{
   //chunk data hash-map
   HashMap<String, Chunk> map = new HashMap<>();
   
   
   public Level()
   {
   
   }
   
   //remove a particular tile - x and y is tilespace
   public void removeTile(int x, int y)
   {
      if(Main.isLocked())
      {
         System.out.println("Method may not be called from AI");
         return;
      }
   
      //find what chunk and then call remove on the chunk.
   
      int xc = x/5;
      int yc = y/5;
      if(x<0)
      {
         xc--;
      }
      if(y<0)
      {
         yc--;
      }      
   
      String chunk = xc+"_"+yc;
      
      if(!map.containsKey(chunk))
      {
         map.put(chunk, new Chunk(xc,yc));
      }
      else
      {
      
      }
      
      Chunk theChunk = map.get(chunk);
      
      theChunk.removeTile(x,y);   
   }
   
   
   //add a tile. 
   public void addTile(Tile t)
   {
      if(Main.isLocked())
      {
         System.out.println("Method may not be called from AI");
         return;
      }
   
      //find the chunk. and create it if it doesn't exist.
      int xc = t.getX()/5;
      int yc = t.getY()/5;
      if(t.getX()<0)
      {
         xc--;
      }
      if(t.getY()<0)
      {
         yc--;
      }      
   
      String chunk = xc+"_"+yc;
      
      
      //System.out.println(chunk);
      
      if(!map.containsKey(chunk))
      {
         map.put(chunk, new Chunk(xc,yc));
      }
      else
      {
      
      }
      
      Chunk theChunk = map.get(chunk);
      
      //call add on the chunk.
      theChunk.addTile(t);
   }

   //draw the map if the camera is at x/y
   public void drawAtPosition(int x, int y, GraphicsContext gc)
   {
      //calc what chunks to display
      int myChunkx = x/5;
      int myChunky = y/5;
      
      for(int i=myChunkx-3;i<=myChunkx+6;i++)
      {
         for(int j=myChunky-3;j<=myChunky+3;j++)
         {
            Chunk c = map.get(i+"_"+j);
            if(c != null)
            {
               c.drawme(gc);
            }
         }      
      }
   }
   
   //run all the tiles. For this project, they have to all be ran because of a player may be far from the camera.
   public void runTiles(ArrayList<Player> p, double elapsedTime)
   {
      if(Main.isLocked())
      {
         System.out.println("Method may not be called from AI");
         return;
      }   
   
      for (Chunk value : map.values()) 
      {
         value.run(p, elapsedTime);
      }
        
   }
   
   //return the internal map structure. Probably a bad plan.
   public HashMap<String,Chunk> getMap()
   {
      if(Main.isLocked())
      {
         System.out.println("Method may not be called from AI");
         return null;
      }
   
      return map;
   }
   
   //create a level from another level. This is used to create a player's version of the level from the level in the editor.
   public Level(Level otherLevel)
   {
      HashMap<String,Chunk> othermap = otherLevel.getMap();
      
      //copy the map
      for (Chunk value : othermap.values()) 
      {
         for(int i=0;i<5;i++)
         {
            for(int j=0;j<5;j++)
            {
               Tile t = value.getIndex(i,j);
               if(t != null)
               {
                  Tile newT = new Tile(t);
                  
                  addTile(newT);
               }
            }
         }

      }
   }
   
   LevelIterator lit = null;///new LevelIterator();
   
   public LevelIterator getLevelIterator()
   {
      if(lit == null)
      {
         lit = new LevelIterator();
      }   
   
      return lit;
   }
   
   //level iterator!!!! SO MUCH FUN to write this. But it makes it a lot easier for you.
   public class LevelIterator
   {
      //all the chunks from the level to a list.
      ArrayList<Chunk> theChunksAL = new ArrayList<Chunk>();
      
      //the next tile to query
      Tile nextTile;
      
      //add all the chunks to the list.
      public LevelIterator()
      {
         for (Chunk value : map.values()) 
         {
            theChunksAL.add(value);
         }
      }
      
      //var to determine if at the end
      boolean atEnd=false;
      
      //advance until the next tile is found.
      private Tile advance()
      {
         //sets next to null then tries to get nextTile to not be at null
         nextTile = null;
         if(atEnd)
         {
            return null;
         }
         
         //keep trying to find then next thing.
         while(nextTile == null)
         {
            tilei++;
            if(tilei == 5)
            {
               tilei=0;
               tilej++;
               
               if(tilej == 5)
               {
                  tilej=0;
                  chunki++;
                  if(chunki == theChunksAL.size())
                  {
                     nextTile = null;
                     atEnd = true;
                     return null;
                  }
               }
            }
            //System.out.println(tilei+" "+tilej+" "+chunki);
            nextTile = theChunksAL.get(chunki).getIndex(tilei,tilej);
         }
         return nextTile;
      }
      
      
      //reset the iterator to the beginning.
      public void resetIterator()
      {
         tilei = -1;
         tilej = 0;
         chunki = 0;
         atEnd = false;
         
         nextTile = advance();
      }
      
      int tilei, tilej, chunki;
      
      //method for if there is any more
      public boolean hasNext()
      {
         if(nextTile != null)
         {
            return true;
         }
         return false;
      }
      
      //method to get the next tile.
      public TileWrapper getNext()
      {
         Tile t = nextTile;
         
         advance();
         
      
         return wrap(t);
      }
      
      //method to wrap this tile in a wrapper.
      private TileWrapper wrap(Tile t)
      {
         return t.getWrapper();
      }
      
      //method to get a specific tile.
      public TileWrapper getSpecificTile(int x, int y)
      {
         int xc = x/5;
         int yc = y/5;
         if(x<0)
         {
            xc--;
         }
         if(y<0)
         {
            yc--;
         }      
      
         String chunk = xc+"_"+yc;    
      
      
         Chunk c = map.get(chunk);
         
         if(c == null)
         {
            return null;
         }
         
         
         Tile t = c.getTileMod(x,y);     
         
         if(t == null)
         {
            return null;
         }
         
         return t.getWrapper();
         
      }
   }
   
   //the wrapper class you get to use. Sorry, you don't get to play with the full tile / map data. This class just holds the data, doesn't do anything with it. reset whenever it is called
   public static class TileWrapper
   {
      int x, y;
      boolean isStart;
      boolean isEnd;
      boolean isBreak;
      double breakTimer;
      double maxBreakTimer;
      String name;
      boolean collides;
      
      public double getMaxBreakTimer(){return maxBreakTimer;}
      public double getBreakTimer(){return breakTimer;}
      public boolean getIsStart(){return isStart;}
      public boolean getIsEnd(){return isEnd;}
      public boolean getIsBreak(){return isBreak;}
      public int getX(){return x;}
      public int getY(){return y;}
      public String getName(){return name;}
      public boolean getIsCollisionable(){return collides;}
   
      public TileWrapper()
      {
      
      }
      
      public void setX(int _x)
      {
         x = _x;
      }
      
      public void setY(int _y)
      {
         y = _y;
      }
      
      public void setIsStart(boolean t)
      {
         isStart = t;
      }
      public void setIsEnd(boolean t)
      {
         isEnd = t;
      }
      public void setIsBreak(boolean t)
      {
         isBreak = t;
      }   
      
      public void setBreakTimer(double t)
      {
         breakTimer = t;
      }
      
      public void setMaxBreakTimer(double t)
      {
         maxBreakTimer = t;
      }
      
      public void setName(String s)
      {
         name = s;
      }
      
      public void setIsCollisionable(boolean b)
      {
         collides=b;
      }
      
   
   }
}