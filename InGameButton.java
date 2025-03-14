//class to represent button data

public class InGameButton
{
   int x;
   int y;
   int w;
   int h;
   int id;
   
   public InGameButton(int x, int y, int w, int h, int id)
   {
      this.x = x;
      this.y = y;
      this.w = w;
      this.h = h;
      this.id = id;
   }
   
   public boolean pointInsideOf(int tx, int ty) {
        return (tx >= x && tx <= (x + w)) && (ty >= y && ty <= (y + h));
    }
    
   public static boolean pointInsideOfStatic(int tx, int ty, int x, int y, int w, int h) {
        return (tx >= x && tx <= (x + w)) && (ty >= y && ty <= (y + h));
    }
}