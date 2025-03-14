import java.util.*;
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

public class Chunk
{
   int x,y;
   
   //chunks are 5 by 5, x and y is top left position;
  
   Tile [][] tiles = new Tile[5][5]; //keep in a 5x5 2d array
   
   //the position of this particular chunk (basically the tile position / 5)
   public Chunk(int x, int y)
   {
      this.x = x;
      this.y = y;
   }
   
   //add a tile to this chunk
   public void addTile(Tile t)
   {
      //calculate the index.
      int xindex = t.getX() - x*5;
      int yindex = t.getY()-y*5;
      
      //handle negative values
      if(t.getX() < 0 )
      {
         xindex += -1;
      }
      
      if(t.getY() < 0 )
      {
         yindex += -1;
      }
           
      //assign the tile
      tiles[xindex][yindex] = t;
   
   }
   
   //remove a particular tile from the chunk
   public void removeTile(int tx, int ty)
   {
      //calc the index 
      int xindex = tx - x*5;
      int yindex = ty-y*5;
      
      //handle negative values
      if(tx < 0 )
      {
         xindex += -1;
      }
      
      if(ty < 0 )
      {
         yindex += -1;
      }
       
      //remove it by setting it to null    
      tiles[xindex][yindex] = null;

   }

   //get a specific tile
   public Tile getTileMod(int tx, int ty)
   {
      //calc index
      int xindex = tx - x*5;
      int yindex = ty-y*5;


      //handle negative
      if(tx < 0 )
      {
         xindex += -1;
      }
      
      if(ty < 0 )
      {
         yindex += -1;
      }
         
        
      return tiles[xindex][yindex];
   }

   
   //get tile from chunk if the indexes of i and j are 0 to 4.
   public Tile getIndex(int i, int j)
   {
      return tiles[i][j];
   }


   //draw the chunk
   public void drawme(GraphicsContext gc)
   {
      for(int i=0;i<5;i++)
      {
         for(int j=0;j<5;j++)
         {
            if(tiles[i][j] != null)
            {
               tiles[i][j].drawme(gc);
            }
         }
      }
   }
   
   //run the chunk (i.e. for this project, it is only the break tiles.
   public void run(ArrayList<Player> p, double elapsedTime)
   {
      for(int i=0;i<5;i++)
      {
         for(int j=0;j<5;j++)
         {
            if(tiles[i][j] != null)
            {
               //true being to be removed
               if(tiles[i][j].run(p, elapsedTime))
               {
                  tiles[i][j] = null;
               }  
            }
         }
      }
   }
}