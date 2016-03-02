System Tested on/To be run on:
Ubuntu 14.04

Prequisites for Development Environment:
Java 7/8 
Maven
MySQL

System consists of two main components:
1. Search Nodes
2. Search Head 
The code for Search Node can be found in the Search Node folder similiarly for Search Head.
Data Folder consists of data that can be inserted into the system.
Deployment prerequisites of the Search Node:
1.  Java 7/8
2. MySQl

Database Schema:
TableName: Data
ID- INT(11) Primary Key Not Null AUTO INCREMENT
FileName  Unique
FileURL
Data
Type
Indexed -Binary 	

In order to run the search node, use the following command:
java -jar SearchNode/target/Node.jar

Database Schema:
TableName: ShardList
ID: INT 
JDBCLink: Unique
Storage:
Rating:
User:
Password:
In order to run the search head, prerequisite software required is as follows:
Tomcat7 
Java 7/8
Create the war file and place it in tomcat directory or run via eclipse:
Changes need to be made to UpdateMain.java:Line 250 pathToWatch variable to your respective directory where the data is to be dumped.
Data Generated can be from any sources

Finally, Place the GUI internal files on the local apache folder and modify the internal paths accordingly

