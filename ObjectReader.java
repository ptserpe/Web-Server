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

public class ObjectReader {

  String userAgent;
  String req;
  String accessFile;
  String errorFile;
  String Doc_root;
  OutputStream os;
  PrintWriter error_out;
  PrintWriter access_out;
  PrintWriter out;
  String remoteIP;

  public ObjectReader(String userAgent, String req, String accessFile, String errorFile, String Doc_root, OutputStream os, PrintWriter error_out,
    PrintWriter access_out, PrintWriter out, String remoteIP) {
      this.userAgent = userAgent;
      this.req=req;
      this.accessFile = accessFile;
      this.errorFile = errorFile;
      this.Doc_root = Doc_root;
      this.os = os;
      this.error_out = error_out;
      this.access_out = access_out;
      this.out = out;
      this.remoteIP = remoteIP;

    }

}
