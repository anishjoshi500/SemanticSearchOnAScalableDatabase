
package index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Scanner;

import main.SearchController;

import org.apache.tomcat.util.http.fileupload.FileUtils;

import com.google.gson.Gson;

import base.Config;

public class IndexThread  extends Thread {
	public String indexdir;
	public String modeldir;
	public String datadir;
	public String tempdir;
	public String link;
	public IndexThread()
	{
		Gson gson = new Gson();
		FileReader in = null;
		try {
			in = new FileReader("config.json");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		BufferedReader br = new BufferedReader(in);
		Config config = gson.fromJson(br, Config.class);
		if(config==null)
		{
			System.out.println("Shit");
		}
		//Modelloc and indexloc initialized
		modeldir = config.configmap.get("model");
		indexdir = config.configmap.get("indexdir");
		datadir= config.configmap.get("datadir");
		tempdir = config.configmap.get("tempdir");
		link = config.configmap.get("link");
		link=link.split(":")[0];
	}
	
	private static final int BUFFER_SIZE = 4096;
	@Override
	public void run() {
		
		Scanner sc = new Scanner(System.in);
		Integer start=206;
		while(true)
		{ //Test.main(new String[]{ start.toString() });
		getUnindexed();
		try {
			Indexer.index(new File(indexdir), new File(tempdir), modeldir);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			FileUtils.cleanDirectory(new File(tempdir));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		updatedb();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		start++;
		}
	}
	
private  void updatedb() {
		
		
		System.out.println("Printing the blob file");
		String url = "jdbc:mysql://"+link+":3306/DistributedSearch";
        String user = "root";
        String password = "password";
        String tempdirpath = tempdir+"/";
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("S1");
            String sql = "Update DistributedSearch.Data set indexed =1";
            PreparedStatement statement = conn.prepareStatement(sql);
            int result = statement.executeUpdate();
            System.out.println("S2");
          
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
	}

	public  void getUnindexed()
	{
		System.out.println("Printing the blob file");
		String url = "jdbc:mysql://"+link+":3306/DistributedSearch";
        String user = "root";
        String password = "password";
 
        String tempdirpath = tempdir+"/";
 
        System.out.println(link);
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            String sql = "SELECT FileName,Type,data FROM Data where Indexed=0";
            PreparedStatement statement = conn.prepareStatement(sql);
            ResultSet result = statement.executeQuery();
          
            while (result.next()) {
                Blob blob = result.getBlob("data");
                String filename=result.getString("FileName");
                System.out.println("FileName:"+filename);
                String truePath=tempdirpath+filename;
                InputStream inputStream = blob.getBinaryStream();
                OutputStream outputStream = new FileOutputStream(truePath);
                int bytesRead = -1;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                inputStream.close();
                outputStream.close();
                System.out.println("File saved");
            }
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
	}


	
}