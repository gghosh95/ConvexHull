import java.util.Comparator;

/*==================================================================================//
 AUTHOR- GAUTAM D GHOSH
 Point_2D class for representing cartesian 2D points
//==================================================================================*/


public class Point_2D implements Comparable<Point_2D> 
{
    private double x;
    private double y;
    
    public Point_2D(double x,double y)
    {
        this.x = x;
        this.y = y;
    }
    
    /*
      Getters for X and Y co-ordinate;
    */
    public double getX(){return x;}
    public double getY(){return y;}
    
    /*
      toString() method to print 2D point, useful for debugging
      @return string, X and Y co-ordiante
    */
    @Override
    public String toString(){ return "x="+x+" "+"y="+y; }

    /*
      Overriden compareTo method to compare two points
      @param  Point p1, the point to which this point needs to be compared
      @return int
    */
    @Override
    public int compareTo(Point_2D p1) 
    {
        if (this.y < p1.y) return -1;
        if (this.y > p1.y) return +1;
        if (this.x < p1.x) return -1;
        if (this.x > p1.x) return +1;
        return 0;
    }

    /*
      Overriden method to remove duplicates while storing points in HashSet
      @param  Object, abstract input but is typecasted to Point_2D
      @return boolean, true if equal else false
    */
    @Override
    public boolean equals(Object obj)
    {
        if      (obj==null) return false;
        else if (obj instanceof Point_2D)
        {
           Point_2D temp = (Point_2D) obj;
           if( this.getX()==temp.getX() && this.getY()==temp.getY() )
           {
              return true;
           }
        }
        return false;
    }

    @Override
    public int hashCode()
    {
      Double X = this.x;
      Double Y = this.y;
      return ( X.hashCode()+Y.hashCode() );
    }
 
    /*
       Routine for findng the orientation of three points
      @param  Point a, Point b, Point c
      @return int
      +1 -> counter-clock wise
      -1 -> clock wise
       0 -> collinear
    */   
    public static int orientation(Point_2D a, Point_2D b, Point_2D c) 
    {
        double area = (b.x-a.x)*(c.y-a.y) - (b.y-a.y)*(c.x-a.x);
        if      (area < 0) return -1;
        else if (area > 0) return +1;
        else               return  0; 
    }

    // overloaded utility function
    public int orientation(Point_2D b, Point_2D c) 
    {
        double area = (b.x-this.x)*(c.y-this.y) - (b.y-this.y)*(c.x-this.x);
        if      (area < 0) return -1;
        else if (area > 0) return +1;
        else               return  0;
    }
   
    /*
       Routine to compute distance between two points in 2D
       @param   point a, point till which distance needs to be computed
       @reuturn double, distance between points
    */
    public double distanceTo(Point_2D a)
    {
        double delX = a.x - this.x;
        double delY = a.y - this.y;
        return Math.pow( Math.pow(delX, 2)+Math.pow(delY, 2), 0.5);
    }
    
    /*
      Comparator for polar sorting of points
    */
    public Comparator<Point_2D> polarorder()
    {
        return new PolarOrder();
    }
    
    private class PolarOrder implements Comparator<Point_2D> {
        @Override
        public int compare(Point_2D q1, Point_2D q2) {
            double dx1 = q1.x - x;
            double dy1 = q1.y - y;
            double dx2 = q2.x - x;
            double dy2 = q2.y - y;

            if      (dy1 >= 0 && dy2 < 0) return -1;    // q1 above; q2 below
            else if (dy2 >= 0 && dy1 < 0) return +1;    // q1 below; q2 above
            else if (dy1 == 0 && dy2 == 0) {            // 3-collinear and horizontal
                if      (dx1 >= 0 && dx2 < 0) return -1;
                else if (dx2 >= 0 && dx1 < 0) return +1;
                else                          return  0;
            }
            else return -orientation(Point_2D.this, q1, q2);     // both above or below
        }
    } 
}
