package searchhead;
 
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

import base.Shard;

import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
class DirectoryWatcher implements Runnable {
	private String link;
    private Path path;
    private Connection connection;
    private int ShardPointer;
    private Connection shardConnections[];
    List<Shard> ShardList;
    private int shardNo;
    private ShardComparator comparator;
    private PriorityQueue<Shard> queue;
    private int previousindex;
    private int iteratevalue;
    private long lasttime;
    public DirectoryWatcher(Path path) {
    	shardConnections = new Connection[100];
    	
    	ShardList=UpdateMain.getShardList();
    	
    	for(int i =0 ;i<ShardList.size();i++)
    	{
    		Shard temp=ShardList.get(i);
    		try {
    			System.out.println("Link"+temp.getLink().split(":")[0]);
    			System.out.println("Password"+temp.getPassword());
    			System.out.println("Link"+temp.getUser());
    			
				shardConnections[i]= DriverManager.getConnection("jdbc:mysql://"+temp.getLink().split(":")[0]+":3306/DistributedSearch", temp.getUser(), temp.getPassword());
    		} catch (SQLException e) {
				e.printStackTrace();
			}
    		temp.setIndex(i);
    	}
    	comparator = new ShardComparator();
    	shardNo=ShardList.size();
    	queue=new PriorityQueue<Shard>(100,comparator);
    	queue.addAll(ShardList);
        this.path = path;
        connection=shardConnections[ShardPointer];
    }
    // print the events and the affected file
    private void printEvent(WatchEvent<?> event) {
        Kind<?> kind = event.kind();
        if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
            Path pathCreated = (Path) event.context();
            System.out.println("Entry created:" + pathCreated);   
        } else if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
            Path pathDeleted = (Path) event.context();
            System.out.println("Entry deleted:" + pathDeleted);
        } else if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
            Path pathModified = (Path) event.context();
            System.out.println("Entry modified:" + pathModified);
            
            displayFile(pathModified);

            	Shard temp=queue.poll();
            	ShardPointer= temp.getIndex();
            	System.out.println("Pointer inside:"+temp.getIndex());
            	if(previousindex==ShardPointer)
            	{
            		temp.history++;
            	}
            	previousindex=ShardPointer;
            	queue.add(temp);
            	printqueue();
            
            	long diff=new Date().getTime()-lasttime;
            	diff=diff/1000;
            	if(iteratevalue==shardNo || diff>10)
            	{
            		ShardList=UpdateMain.getShardList();
                    shardNo=ShardList.size();
            	}
            	
            	connection=shardConnections[ShardPointer];
                System.out.println("Shard Pointer index:"+ShardPointer);
           
        }
        
    }
	private void printqueue() {
		
		System.out.println("\tPrinting contents of the queue");
		Iterator<Shard> it = queue.iterator();
		while(it.hasNext())
		{
			Shard temp = it.next();
			System.out.println("\tShard index:"+temp.getIndex()+" Link:"+temp.getLink()+",History:"+temp.history);
		}
	}
	private void displayFile(Path pathCreated) {
		
				String truepath = path+"/"+pathCreated.toString();
				System.out.println(truepath);
		        File f = new File(truepath);
		        Scanner input=null;
		try {
				input = new Scanner(f);
		} catch (FileNotFoundException e) {
				e.printStackTrace();
		}
		InputStream inputStream;
				try {
					inputStream = new FileInputStream(f);
					 String FileName=pathCreated.toString();
				        String FileURL= link+truepath;
				        String stmt="Insert into Data(FileName,FileURL,Data,Indexed) values('"+FileName+"','"+FileURL+"',?,0)";
				        PreparedStatement statement = connection.prepareStatement(stmt);
				        statement.setBlob(1,inputStream);
				        statement.executeUpdate();
				        
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (SQLException e) {
				
					e.printStackTrace();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
		         
	}
		    public void run() {
		        try {
		            WatchService watchService = path.getFileSystem().newWatchService();
		            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
		                    StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
		          
		            while (true) {
		                WatchKey watchKey;
		                watchKey = watchService.take(); // this call is blocking until events are present
		                // poll for file system events on the WatchKey
		                for (final WatchEvent<?> event : watchKey.pollEvents()) {
		                    printEvent(event);
		                }
		                // if the watched directed gets deleted, get out of run method
		                if (!watchKey.reset()) {
		                    System.out.println("No longer valid");
		                    watchKey.cancel();
		                    watchService.close();
		                    break;
		                }
		            }
		        } catch (InterruptedException ex) {
		            System.out.println("interrupted. Goodbye");
		            return;
		        } catch (IOException ex) {
		            ex.printStackTrace();  
		            return;
		        }
		    }   
}
public class UpdateMain {
	public static List<Shard> getShardList() {
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
		      sql = "SELECT * FROM ShardList";
		      ResultSet rs = stmt.executeQuery(sql);
		      while(rs.next()){
		         int id  = rs.getInt("id");
		         int storage = rs.getInt("storage");
		         int rating = rs.getInt("rating");
		         String link = rs.getString("JDBCLink");
		         String user = rs.getString("user");
		         String password = rs.getString("password");
		         Shard temp = new Shard();
		         temp.setId(id);
		         temp.setLink(link);
		         temp.setRating(rating);
		         temp.setStorage(storage);
		         temp.setUser(user);
		         temp.setPassword(password);
		         shardList.add(temp);
		         System.out.print("ID: " + id);
		         System.out.print(", Storage: " + storage);
		         System.out.print(", Rating: " + rating);
		         System.out.println(", JDBCLink: " + link);
		      }
		      rs.close();
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
		return shardList;
	}
    public static void main(String[] args) throws InterruptedException {
        Scanner sc = new Scanner(System.in);
    	Path pathToWatch = FileSystems.getDefault().getPath("/home/sanket/Experiments/NotificationS", args);
        DirectoryWatcher dirWatcher = new DirectoryWatcher(pathToWatch);
        Thread dirWatcherThread = new Thread(dirWatcher);
        dirWatcherThread.start();
        // interrupt the program after 10 seconds to stop it.
        Thread.sleep(10000000);
        dirWatcherThread.interrupt();
    }
}
class ShardComparator implements Comparator<Shard>
{
	public int compare(Shard s1, Shard s2) {
		
		System.out.println("Shard 1 Storage and Rating are as follows:"+s1.getRating()+" :"+s1.getStorage());
		System.out.println("Shard 1 Storage and Rating are as follows:"+s2.getRating()+" :"+s2.getStorage());
		double s1score =(double)s1.getStorage()/1000+(double)s1.getRating()/5-s1.history*0.15464878;
		double s2score=(double)s2.getStorage()/1000+(double)s2.getRating()/5-s2.history*0.15464879;
		System.out.println("Calculating Scores");
		System.out.println("Shard "+s1.getLink()+" "+s1score+", Index:"+s1.getIndex());
		System.out.println("Shard "+s2.getLink()+" "+s2score+", Index:"+s2.getIndex());
		if(s1score>s2score)
		{
			return -1;
		}
		else if (s1score==s2score)
		{
			
			return 0;
		}
		else
		{
			return 1;
		}
	}
}