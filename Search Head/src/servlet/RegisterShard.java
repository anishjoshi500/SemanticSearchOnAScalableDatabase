package servlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import base.Shard;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class RegisterShard
 */
@WebServlet("/RegisterShard")
public class RegisterShard extends HttpServlet {
	private static final long serialVersionUID = 1L;
    /**
     * Default constructor. 
     */
    public RegisterShard() {
        // TODO Auto-generated constructor stub
    }
    public void init() throws ServletException
    {
          System.out.println("----------");
         
          System.out.println("----------");
    }
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		String link=request.getParameter("link");
		String storage=request.getParameter("storage");
		String rating = request.getParameter("rating");
		
		Shard temp = new Shard();
		temp.setLink(link);
		temp.setRating(Integer.parseInt(rating));
		temp.setStorage(Integer.parseInt(storage));
		List<Shard> shardlist = getShardList();
		boolean result=search(shardlist,temp);
		
		if(result==false)
		{
			temp.setId(shardlist.size()+1);
			insert(temp);
			PrintWriter writer = response.getWriter();
			writer.print("Success");
		}
	}

	private boolean search(List<Shard> shardlist,Shard tosearch) {
		
		for(int i=0;i<shardlist.size();i++)
		{
			Shard temp = shardlist.get(i);
			if(tosearch.getLink().equals(temp.getLink()))
			{
				return true;
			}
			
		}
		
		return false;
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}
	
	private static List<Shard> getShardList() {
		
		String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
		String DB_URL = "jdbc:mysql://localhost/DistributedSearch";

		String USER = "root";
		String PASS = "password";
		List<Shard> shardList = new ArrayList<Shard>();
		Connection conn = null;
		   Statement stmt = null;
		   try{
		      Class.forName("com.mysql.jdbc.Driver");

		      System.out.println("Connecting to database...");
		      conn = DriverManager.getConnection(DB_URL,USER,PASS);

		      System.out.println("Creating statement...");
		      stmt = conn.createStatement();
		      String sql;
		      sql = "SELECT * FROM ShardList";
		      ResultSet rs = stmt.executeQuery(sql);

		      while(rs.next()){
		         int id  = rs.getInt("id");
		         int storage = rs.getInt("storage");
		         int rating = rs.getInt("rating");
		         String link = rs.getString("JDBClink");

		         Shard temp = new Shard();
		         temp.setId(id);
		         temp.setLink(link);
		         temp.setRating(rating);
		         temp.setStorage(storage);
		         
		         shardList.add(temp);
		         //Display values
		         System.out.print("ID: " + id);
		         System.out.print(", Storage: " + storage);
		         System.out.print(", Rating: " + rating);
		         System.out.println(", JDBCLink: " + link);
		      }
		      rs.close();
		      stmt.close();
		      conn.close();
		   }catch(SQLException se){
		      //Handle errors for JDBC
		      se.printStackTrace();
		   }catch(Exception e){
		      //Handle errors for Class.forName
		      e.printStackTrace();
		   }finally{
		      //finally block used to close resources
		      try{
		         if(stmt!=null)
		            stmt.close();
		      }catch(SQLException se2){
		      }
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }
		   }
		   System.out.println("Goodbye!");	
		return shardList;
	}

	public static void insert(Shard insert)
	{
		
		String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
		String DB_URL = "jdbc:mysql://localhost/DistributedSearch";

		   //  Database credentials
		String USER = "root";
		String PASS = "password";
		List<Shard> shardList = new ArrayList<Shard>();
		Connection conn = null;
		   Statement stmt = null;
		   try{
		      Class.forName("com.mysql.jdbc.Driver");
		      System.out.println("Connecting to database...");
		      conn = DriverManager.getConnection(DB_URL,USER,PASS);
		      System.out.println("Creating statement...");
		      stmt = conn.createStatement();
		      String sql;
		      sql = "INSERT INTO ShardList (ID,JDBCLink,Storage,Rating,User,Password)"+"VALUES ("+insert.getId()+",'"+insert.getLink()+"',"+insert.getStorage()+","+insert.getRating()+",'"+USER+"','"+PASS+"')";
		      stmt.executeUpdate(sql);
		      System.out.println("Inserted records into the table...");
		      stmt.close();
		      conn.close();
		   }catch(SQLException se){
		      se.printStackTrace();
		   }catch(Exception e){
		      e.printStackTrace();
		   }finally{
		      try{
		         if(stmt!=null)
		            stmt.close();
		      }catch(SQLException se2){
		      }
		      try{
		         if(conn!=null)
		            conn.close();
		      }catch(SQLException se){
		         se.printStackTrace();
		      }
		   }
		   System.out.println("Goodbye!");	
		
	}
	

}