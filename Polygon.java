
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Scanner;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.*;
import java.io.*;
import java.util.Map;
import java.util.Iterator;
import java.util.HashSet;
import java.lang.Number;
import java.math.*;

/*==================================================================================//
 AUTHOR- GAUTAM D GHOSH
 AIM - TO FIND THE AREA OF THE SMALLEST CONVEX POLYGON 
 INPUT - Points in JSON format
 OUTPUT - area of convex polygon
 ALGORITHM USED - GRAHAM SCAN
//==================================================================================*/

public class Polygon
{
    private static int size;
    private static Point_2D p0;

    private static LinkedList<Point_2D> hull;
    private static HashSet<Point_2D> points;
    private static Point_2D[] distinctpoints;

    Polygon(StringBuilder sb) throws FileNotFoundException, IOException, ParseException
    {
        // instantiate a collection to store points from JSON file, since Set is used no duplicates are present
        points = new HashSet<>();
        // Read JSON, create objects of Point_2D and store into HashSet
        readJSON(sb);
        // Maybe some bad data can cause null objects, safety step to avoid NullPointerException
        for(Point_2D e : points)
        {
          if(e == null) points.remove(e);
        }
        // Copy all points into array, saves access time
        distinctpoints = new Point_2D[points.size()];
        points.toArray(distinctpoints);
        // make sure to remove all duplicates before computing the size
        size = distinctpoints.length;
        // create a collection to store points belonging to convex hull, 
        // LinkedList is used because the size is unknown during run time and is more space efficient than ArrayList 
        hull = new LinkedList<>();
    }

    /*
       Read JSON file from Standard Input and create Point_2D objects
       @param StringBuilder, input from standard input is taken as a string 
    */    
    private static void readJSON(StringBuilder json) throws FileNotFoundException, IOException, ParseException
    {
       String tempstring = json.toString();
       Object obj = new JSONParser().parse(tempstring);
       JSONArray ja = (JSONArray) obj;
       Iterator<Map.Entry> itr1;
       Iterator itr2 = ja.iterator();
       LinkedList<Double> temp = new LinkedList<>();
       int ii=0;
       while(itr2.hasNext())
       {
        itr1 = ((Map) itr2.next()).entrySet().iterator();
        while(itr1.hasNext())
        { 
          Map.Entry pair = itr1.next();
          if( ii%2 == 0 )
          {
            double x = ((Number) pair.getValue()).doubleValue();
            BigDecimal bx = new BigDecimal(x).setScale(6,RoundingMode.HALF_EVEN);
            temp.addLast(bx.doubleValue());
            ii++;   
          }
          else if( ii%2 != 0 )
          {
            double y = ((Number) pair.getValue()).doubleValue();
            BigDecimal by = new BigDecimal(y).setScale(6,RoundingMode.HALF_EVEN);
            temp.addLast(by.doubleValue());
            points.add(new Point_2D( temp.get(0),temp.get(1) ));
            temp.clear();
            ii++;  
          }
        } 
      } 
    }
   
    /*
       Find the lowest point, incase of a clash choose the left-most point
       Time Complexity: O(N), Space Complexity: O(1)
  
       @return int, index of point with lowest Y
    */
    private static int lowestY() // 
    {
        double ymin = distinctpoints[0].getY();
        int min=0;
        for(int ii=1; ii<distinctpoints.length; ii++)
        {
            double y = distinctpoints[ii].getY();
            if( ( y < ymin ) || ( ymin == y && Math.round(distinctpoints[ii].getX()) < Math.round(distinctpoints[min].getX() ) ) )
            {
                ymin = distinctpoints[ii].getY();
                min = ii;
            }
        }
        return min;
    }

    /*
       Routine for finding the convex hull 
       Time Complexity: O(NlogN), Space Complexity: O(N)
    */
    private static void GrahamScan()
    {
        int ymin = lowestY();
        
        // swap points, Time Complexity = O(1)
        p0 = distinctpoints[ymin];
        Point_2D temp = distinctpoints[0];
        distinctpoints[0] = p0;
        distinctpoints[ymin] = temp;
        
        // Sort remaining points by polar angle, Time Complexity = O(NlogN)
        Arrays.sort(distinctpoints,1,size,distinctpoints[0].polarorder());
        
        // incase two points are collinear remove the closest point to p0 , Time Complexity = O(N)
        // Very crucial for the computing area
        for(int ii=1; ii<distinctpoints.length-1;ii++)
        {
            if(p0.orientation(distinctpoints[ii], distinctpoints[ii+1]) == 0 )
            {
                if      (p0.distanceTo(distinctpoints[ii]) < p0.distanceTo(distinctpoints[ii+1]))  distinctpoints=removeElement(distinctpoints,ii);
                else if (p0.distanceTo(distinctpoints[ii]) > p0.distanceTo(distinctpoints[ii+1]))  distinctpoints=removeElement(distinctpoints,ii+1);
            }
        }
        
        // push first two points in hull, these are guranteed to be present in the convex hull , Time Complexity = O(1)
        hull.addFirst(distinctpoints[0]);
        hull.addFirst(distinctpoints[1]);
        
        // find remanining points for convex hull and push to hull
        for (int i = 2; i < distinctpoints.length; i++) 
        {
            Point_2D top = hull.pop();
            while (Point_2D.orientation(hull.peek(), top, distinctpoints[i]) <= 0) 
            {
                top = hull.pop();
            }
            hull.addFirst(top);
            hull.addFirst(distinctpoints[i]);
        }
    }

    /*
       Routine for computing area of convex polygon using convex hull
       Time Complexity: O(N), Space Complexity: O(N)

       @return area of convex polygon 
    */
    public static float minimumArea()
    {
        if(distinctpoints.length < 3) return 0.00000000f;
        GrahamScan();
        int n = hull.size();
        double area = 0;
       
        // copy hull to an array, saves access time while computing area, Space Complexity: O(N)
        Point_2D[] hullcopy = new Point_2D[n];
        for(int i=0; i<n; i++)
        {
           hullcopy[i] = hull.get(i); 
        }
        
        // ccw traversal of set of points in hull and compute area, Time Complexity: O(N)
        for(int ii=n-1; ii>=0; ii--)
        {
            if(ii==0)
            {
                area += (hullcopy[0].getX()*hullcopy[n-1].getY())-(hullcopy[0].getY()*hullcopy[n-1].getX());
                break;
            }
            area += (hullcopy[ii].getX()*hullcopy[ii-1].getY())-(hullcopy[ii].getY()*hullcopy[ii-1].getX());
        }

        clearData();
        return (float)(area/2.0);
    }
    
    // Utiliy functions
    /*
       Remove element in an array at specified index
       @param array, element in this array is to be deleted
       @param int, index at which element needs to be removed
    
       @return modified array 
    */
    private static Point_2D[] removeElement(Point_2D[] arr, int index) 
    { 
        if (arr == null
            || index < 0
            || index >= arr.length) { 
  
            return arr; 
        } 
        Point_2D[] anotherArray = new Point_2D[arr.length - 1]; 

        System.arraycopy(arr, 0, anotherArray, 0, index); 

        System.arraycopy(arr, index + 1, 
                         anotherArray, index, 
                         arr.length - index - 1); 
        return anotherArray; 
    }

    /*
      Clear data from all containers to void mixing between test cases
    */
    private static void clearData()
    {
       points.clear();
       hull.clear();
    }
    
    public static void main(String[] args) throws Exception 
    {  
        Scanner sc = new Scanner(System.in);
        StringBuilder sb = new StringBuilder();
        while(sc.hasNextLine())
        {
           sb.append(sc.nextLine());
           System.out.println(new Polygon(sb).minimumArea());
           sb=new StringBuilder();
        }     
    }
}
