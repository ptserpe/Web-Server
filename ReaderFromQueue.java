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


public class ReaderFromQueue extends Thread {
  //pedia
  ArrayBlockingQueue<ObjectReader> queue;
  PrintWriter out;
  PrintWriter access_out;
  PrintWriter error_out;
  ReentrantLock readLock;
  String userAgent;

  //constructor
  public ReaderFromQueue(ArrayBlockingQueue<ObjectReader> rq, PrintWriter readerOut, PrintWriter readerAccessOut, PrintWriter readerErrorOut, ReentrantLock readerReadLock) {
    queue = rq;
    out = readerOut;
    access_out = readerAccessOut;
    error_out = readerErrorOut;
    readLock = readerReadLock;
  }

  //methodoi
  public void run() {
      ObjectReader o;

      try {

          while(true) {

            readLock.lock();
            o = queue.take();

            sendResponse(o);

            readLock.unlock();
          }

      } catch(InterruptedException e) {
        e.printStackTrace();
      }
      finally {
        if( readLock.isHeldByCurrentThread() ) {
          readLock.unlock();
        }
      }


  }

  public String findIcon(String suf) {
    String icon = "/icons/";
    String suffix = suf; //including dot (.)

    switch(suffix.toLowerCase()) {
      /* doc */
      case ".doc":
      case ".docx":
      case ".odt":
        icon += "doc.png";
        break;
      /* xls */
      case ".xls":
      case ".xlsx":
      case ".ods":
        icon += "xls.png";
        break;
      /* ppt */
      case ".ppt":
      case ".pptx":
      case ".odp":
        icon += "ppt.png";
        break;
      /* pdf */
      case ".pdf":
      case ".ps":
        icon += "pdf.png";
        break;
      /* images */
      case ".png":
      case ".jpg":
      case ".jpeg":
      case ".bmp":
      case ".tiff":
      case ".svg":
      case ".pgm":
      case ".ppm":
      case ".pbm":
        icon += "img.png";
        break;
      /* video */
      case ".mp4":
      case ".flv":
      case ".mkv":
      case ".ogv":
      case ".avi":
      case ".mov":
      case ".qt":
        icon += "video.png";
        break;
      /* audio */
      case ".wav":
      case ".mp3":
      case ".ogg":
      case ".cda":
      case ".flac":
      case ".snd":
      case ".aa":
      case ".mka":
      case ".wma":
      case ".m4p":
      case ".mp4a":
      case ".mpa":
        icon += "audio.png";
        break;
      /* html */
      case ".html":
      case ".htm":
        icon += "html.png";
        break;
      /* xml */
      case ".xml":
        icon += "xml.png";
        break;
      /* rss */
      case ".rss":
        icon += "rss.png";
        break;
      default:
        icon += "txt.png";


    }
    return icon;
  }

  public void sendResponse(ObjectReader o) {
      int j=0;

      StringBuffer log = new StringBuffer("");
      StringBuffer response=new StringBuffer("");
      String[] httpRequest = new String[3];
      String status_code="";

      String inputLine = o.req;
      String userAgent = o.userAgent;
      String Doc_root = o.Doc_root;
      OutputStream os = o.os;
      PrintWriter error_out = o.error_out;
      PrintWriter access_out = o.access_out;
      PrintWriter out = o.out;
      String remoteIP = o.remoteIP;



      try {
        Scanner sc = new Scanner(inputLine).useDelimiter(" ");
        while(sc.hasNext()!=false) {
            httpRequest[j] = sc.next();

            j++;
        }
        //HTTP/1.1  or HTTP/1.0
        response.append(httpRequest[2]);

        File filepath = new File(Doc_root + httpRequest[1]);
        //404 status code
        if(!(filepath.exists())) {
             response.append(" 404 File Not Found\n");
             status_code = "404 File not Found";
             out.println("404 File not Found");
             return;
        }
        //400 status code
        if(httpRequest[2] == null || ((!httpRequest[2].equals("HTTP/1.1")) && (!httpRequest[2].equals("HTTP/1.0")))) {
             out.println("400 Bad Request");
             status_code = "400 Bad Request";
             return;
        }
        else if(inputLine.startsWith("POST") || inputLine.startsWith("HEAD")){
            status_code = "405 Method Not Allowed";
            out.println("405 Method Not Allowed\n");
            return;
        }
        //202 OK status code
        else {
               response.append(" 200 OK\n");
               status_code = "200 OK";
        }

        //Date: Mon, 23 Mar 2015 16:55:25 GMT
        Date date = new Date();
        response.append("Date: " + new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").format(date)+"\n");

        //Server: CE325 (Java based server)
        response.append("Server: CE325 (Java based server)\n");

        //Last-Modified: Mon, 23 Mar 2015
        long ms = filepath.lastModified();
        Date lastmodified = new Date(ms);
        response.append("Last-Modified: " + new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").format(lastmodified) +"\n");

        //Content-Length
        int size = (int) filepath.length();
        response.append("Content-Length: " + size + "\n");

        //Connection
        response.append("Connection: close" + "\n");

        Properties prop = new Properties();
        File mimeTypes = new File("mime-types.txt");
        FileInputStream fisMime = new FileInputStream(mimeTypes);

        prop.load(fisMime);

        String suffix;
        String icon;

        //an to filepath kataligei se fakelo
        if(filepath.isDirectory()) {
          String parent;

          suffix = ".dir";
          String type = new String(prop.getProperty(suffix));

          out.println(response);
          out.flush();

          StringBuffer dir_format = new StringBuffer("");

          //title
          dir_format.append("<html>\n <head>\n  <title>Index of " + filepath +"</title>\n </head>\n");

          //body
          dir_format.append(" <body>\n<h1>Index of " + filepath + "</h1>\n");
          //Name Last Modified Size
          dir_format.append("  <table>\n");
          dir_format.append("   <tr><th valign=\"top\"></th><th><a>Name</a></th><th><a>Last modified</a></th><th><a>Size</a></th><th><a>Description</a></th></tr>\n");
          dir_format.append("   <tr><th colspan=\"5\"><hr></th></tr>\n");

          //find files in the specified directory
          File[] paths;

          paths = filepath.listFiles();
          String[] p = new String[paths.length];
          int k=0;
          for(File path:paths) {
            p[k] = paths[k].getPath();
            p[k] = p[k].substring(Doc_root.length()+1, p[k].length());

            k++;
          }

          String[] filenames;
          filenames = filepath.list();

          k=0;
          // gia parent

          String fileString = filepath.getPath();
          File inFile = new File(httpRequest[1]);

          //an dn eimai to doc root ypologizw parent, alliws oxi
          if( fileString.length() != Doc_root.length()) {

            parent = inFile.getParent();

            dir_format.append("<tr><td valign=\"top\"></td><td><a href=\"" +parent+"\">"+"Parent Directory"+"</a></td><td align=\"right\">");

          }

          // gia ta arxeia pou periexeei o fakelos
          for(File path:paths) {
            if(path.isDirectory()) {
              icon = "/icons/dir.png";
            }
            else {
              String[] suf = p[k].split("\\.");
              String suff = "." + suf[1];
              icon = findIcon(suff);
            }

            dir_format.append("<tr><td valign=\"top\"></td><td><img src=\""+ icon +"\"><a href=\"" + p[k] +"\">"+filenames[k]+"</a></td><td align=\"right\">");
            dir_format.append(new SimpleDateFormat("YYYY-MM-dd HH:mm  ").format(lastmodified));
            dir_format.append("</td><td align=\"right\">"+ (int)path.length());
            dir_format.append("</td><td>&nbsp;</td></tr>\n");
            k++;
          }

          dir_format.append("   <tr><th colspan=\"5\"><hr></th></tr>\n");
          dir_format.append("</table>\n");
          dir_format.append("</body></html>\n");

          out.println(dir_format);

          out.flush();

        }
        else {
          String[] fn = httpRequest[1].split("\\.");
          suffix = "." + fn[1];

          String type = new String(prop.getProperty(suffix));
          response.append("Content-Type: " + type + "\r\n");

          out.println(response);
          out.flush();

          //provoli arxeiou
          FileInputStream fis = new FileInputStream (filepath);
          byte[] data = new byte[size];

          fis.read(data, 0, size);

          fis.close();

          os.write(data, 0, size);

          os.flush();

          out.flush();
         }

         //katagrafi access log
         InetAddress addr = InetAddress.getLocalHost();
         String ip = addr.getHostAddress();

         log.append(ip + " - [" + new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").format(date)+"] ");
         log.append(inputLine + " ->" + status_code + " \"" + userAgent + "\"\n");

         access_out.println(log);
         access_out.flush();


      } catch(Exception e) {
        //katagrafi error log
        System.out.println("500 Internal Server Error");
        Date date = new Date();

        StringBuffer error = new StringBuffer("");
        String temp = e.getMessage();
        error.append(remoteIP + " - [" + new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").format(date)+"] ");
        error.append(temp);
        error_out.println(error);
        error_out.flush();
      }

    }
}
