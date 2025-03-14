import java.io.PrintWriter;
import java.util.Scanner;
import java.util.*;


//my implementation of the decorator pattern for the different decorations. You won't access this data.

public abstract class TileDecoration
{
   //next of the linked list
   protected TileDecoration next;
   
   //data of the decorator
   protected String name;
   protected double [] args;
   protected String sarg="";
   
   //tile of this decorator
   protected Tile theTile;
   
   public TileDecoration()
   {
      
   }

   //for saving the tiletype to the file.
   public void save(PrintWriter pw)
   {
      pw.print(name+" ");
      if(args != null)
      {
         for(int i=0;i<args.length;i++)
         {
            pw.print(args[i]+" ");
         }
      }
      if(!sarg.equals(""))
      {
         pw.print(sarg+" ");
      }
   }
   
   //a method stub for duplicating a decoration
   public abstract TileDecoration duplicate(Tile t);
   
   public TileDecoration getNext()
   {
      return next;
   }
   
   //running the decoration list
   public boolean run(ArrayList<Player> p, boolean ontop, double timeElapsed)
   {
      if(next == null)
         return false;
   
      return next.run(p,ontop, timeElapsed);
   }
   
   public abstract void load(Scanner scan);
   
   //count the number of decorations
   public static int countNumber(TileDecoration head)
   {
      int count=0;
      while(head != null)
      {
         count++;
         head = head.next;
      }
      return count;
   }
   
   public void setNext(TileDecoration t)
   {
      next = t;
   }
}

//start decortation
class StartDecorator extends TileDecoration
{
   //read it from the file
   public StartDecorator(Scanner scan)
   {
      load(scan);
   }
   
   public StartDecorator()
   {
   
   }
   
   //read whatever data is necessary for this decoration
   public void load(Scanner scan)
   {
      //this.args = new int[args.length];
      name = "start";
   }
   
   //for duplicateing a particular tile.
   public TileDecoration duplicate(Tile t)
   {
      TileDecoration temp = new StartDecorator();
      temp.name = "start";
      
      if(next != null)
      {
         temp.next = next.duplicate(t);
      }
      
      return temp;
   }

}

//goal decoration
class GoalDecorator extends TileDecoration
{
   public GoalDecorator(Scanner scan)
   {
      load(scan);
   }
   
   public GoalDecorator(Tile t)
   {
      theTile = t;
   }
   
   public void load(Scanner scan)
   {
      name = "goal";
   }
   
   //for duplicating the tile
   public TileDecoration duplicate(Tile t)
   {
      TileDecoration temp = new GoalDecorator(t);
      temp.name = "goal";
      
      if(next != null)
      {
         temp.next = next.duplicate(t);
      }
      
      return temp;
   }


   //for running the tile. Not that it checks if a player overlaps the goals. its probably wrong code (for the moment) but it will run correcting in our project :)
   public boolean run(ArrayList<Player> p, boolean ontop, double timeElapsed)
   {  
      for(int i=0;i<p.size();i++)
      {
         Player t = p.get(i);
      
         if(GameObject.squaresOverlap((int)t.getX(),(int)t.getY(),theTile.getX()*30,theTile.getY()*30))
         {
            t.atGoal();
         }
      }
      
      if(next == null)
         return false;
   
      return next.run(p,ontop, timeElapsed);
   }
}

//for break tiles
class BreakTileDecoration extends TileDecoration
{
   double timeLeft=-10;

   public BreakTileDecoration(Scanner scan)
   {
      load(scan);
   }
   
   public BreakTileDecoration()
   {
   
   }
   
   //info of the decorator
   public double getTimeLeft()
   {
      return timeLeft;
   }
   
   public double getMaxTimeLeft()
   {
      return args[0];
   }
   
   
   //loading it from a file
   public void load(Scanner scan)
   {
      this.args = new double[1];
      name = "breaktile";
      args[0] = scan.nextDouble();
   }
   
   //duplicating the tile
   public TileDecoration duplicate(Tile t)
   {
      TileDecoration temp = new BreakTileDecoration();
      temp.name = "breaktile";
      
      temp.args = new double[1];
      temp.args[0] = args[0];
      
      if(next != null)
      {
         temp.next = next.duplicate(t);
      }
      
      return temp;
   }
   
   //running it to reduce the life of the tile.
   public boolean run(ArrayList<Player> p, boolean ontop, double timeElapsed)
   {
      if(timeLeft == -10)
      {
         if(ontop)
         {
            timeLeft = args[0];
         }
      }
      else
      {
         timeLeft-=timeElapsed;

         if(timeLeft < 0)
         {
            timeLeft = 0;
         }
      }
   
      if(next == null)
         return false | timeLeft == 0;
   
      return next.run(p,ontop, timeElapsed) | timeLeft == 0 ; // return's true if this tile should be removed
   }
}