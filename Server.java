package ce325.hw2;

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.io.File;
import java.util.Scanner;
import java.lang.String;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.ByteArrayOutputStream;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.net.Socket;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class Server {

    public static void main(String[] args) throws IOException {
          String accessFile="";
          String errorFile="";
          String Doc_root="";
          if (args.length != 1) {
              System.err.println("Usage: java EchoServer <port number>");
              System.exit(1);
          }

    	    int portNumber=0;


  	      try{
        		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder() ;
        		Document xml_doc= db.parse(args[0]);

    		  xml_doc.getDocumentElement().normalize();

      		/* LISTEN PORT */
      		NodeList list= xml_doc.getElementsByTagName("listen");
      		Node node=list.item(0);
      		if (node.getNodeType() == Node.ELEMENT_NODE) {
      			Element el=(Element) node;
      			portNumber = Integer.parseInt(el.getAttribute("port"));

      		}

      		/* STATISTICS PORT */
      		NodeList statList= xml_doc.getElementsByTagName("statistics");
      		Node snode=statList.item(0);
      		if (snode.getNodeType() == Node.ELEMENT_NODE) {
      			Element els=(Element) snode;
      			int statPort = Integer.parseInt(els.getAttribute("port"));

      		}

    		  /* LOG FILEPATHS */
    	    /*access*/

    		  NodeList accesslog = xml_doc.getElementsByTagName("access");
    			Node alnode=accesslog.item(0);
    			if (alnode.getNodeType() == Node.ELEMENT_NODE) {
      				Element element=(Element) alnode;
      				accessFile=element.getAttribute("filepath");
    			}
    			/*errors*/

    			NodeList errorlog = xml_doc.getElementsByTagName("error");

    			Node erlnode=errorlog.item(0);
    			if (erlnode.getNodeType() == Node.ELEMENT_NODE) {
      				Element erelement=(Element) erlnode;
      				errorFile=erelement.getAttribute("filepath");
    			}

    			/*DOCUMENT ROOT*/
    			NodeList docroot = xml_doc.getElementsByTagName("documentroot");
    			Node docnode=docroot.item(0);
    			if (docnode.getNodeType() == Node.ELEMENT_NODE) {
      				Element docelement=(Element) docnode;
      				Doc_root = docelement.getAttribute("filepath");
    			}
        	}catch(Exception ex){
        		ex.printStackTrace();

        	}


          OutputStream os;
          ServerSocket serverSocket = new ServerSocket(portNumber);
          Socket clientSocket;
          PrintWriter error_out = new PrintWriter(errorFile);
          PrintWriter access_out = new PrintWriter(accessFile);

          ArrayBlockingQueue<ObjectReader> blockingQueue = new ArrayBlockingQueue<>(1000);
          
          ReentrantLock readLock = new ReentrantLock();

          String getLine="";
          String userAgent="";
          String inputLine;
          clientSocket = serverSocket.accept();
          PrintWriter out = new PrintWriter(os = clientSocket.getOutputStream(), true);
          BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
          String remoteIP = clientSocket.getRemoteSocketAddress().toString();

          //2 worker threads
          ReaderFromQueue rd1 = new ReaderFromQueue(blockingQueue, out, access_out, error_out, readLock);
          ReaderFromQueue rd2 = new ReaderFromQueue(blockingQueue, out, access_out, error_out, readLock);

          rd1.start();
          rd2.start();

          while(true) {
              Date start = new Date();
        		  try{

                  //main thread
                  while((inputLine = in.readLine()) != null) {
                      if(inputLine.startsWith("GET")) {

                            getLine = inputLine;

                      }
                      else if (inputLine.startsWith("User-Agent")){
                					userAgent= inputLine;
                          ObjectReader ro = new ObjectReader(userAgent, getLine, accessFile, errorFile, Doc_root, os, error_out, access_out, out, remoteIP);
                          blockingQueue.put(ro);

                          break;
              				}
                }

                } catch (Exception e) {
                    e.printStackTrace();
                    return;
               }
             clientSocket = serverSocket.accept();
             out = new PrintWriter(os = clientSocket.getOutputStream(), true);
             in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

          }
      }
}
