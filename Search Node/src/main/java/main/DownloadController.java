package main;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class DownloadController {

	@RequestMapping("/download")
	public void getFile( @RequestParam(value="filename",defaultValue="null") String filename, HttpServletResponse response) throws SQLException, ClassNotFoundException
	{
		Class.forName("com.mysql.jdbc.Driver");
		
		String user = "root";
        String password = "password";
        String url="jdbc:mysql://localhost:3306/DistributedSearch";
        InputStream inputStream = null;
        Connection conn = DriverManager.getConnection(url, user, password);
        System.out.println("S1");
        String sql = "SELECT FileName,Type,data FROM Data where filename='"+filename+"'";
        PreparedStatement statement = conn.prepareStatement(sql);
        ResultSet result = statement.executeQuery();
		
		
        while (result.next()) {
        	
            Blob blob = result.getBlob("data");
            
            inputStream = blob.getBinaryStream();
            break;
            
        }
        conn.close();
   
    
		try{
		IOUtils.copy(inputStream, response.getOutputStream());
		response.flushBuffer();
		}
		catch(Exception e){
			e.printStackTrace();
			
		}
	}
	
	
	
}
