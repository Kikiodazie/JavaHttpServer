package com.odazie.javahttpserver;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

// Each Client Connection will be managed in a dedicated Thread
public class Server implements Runnable {

    static final File WEB_ROOT = new File(("."));
    static final String DEFAULT_FILE = "index.html";
    static final String FILE_NOT_FOUND = "404.html";
    static final String METHOD_NOT_SUPPORTED = "root_supported.html";

    // port to listen to connection
    static final  int PORT = 8080;

    // Verbose mode
    static final boolean verbose = true;

    // Client Connection via Socket Class
    private Socket connect;

    public Server(Socket connect){
        setConnect(connect);
    }

    @Override
    public void run() {
        // TODO Auto generated method stub
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedOutputStream dataOut = null;
        String fileRequested = null;

        try {
            // read characters form the client via input stream socket
            in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            // We get character output stream to client (For headers)
            out = new PrintWriter(connect.getOutputStream());
            // get binary output stream to client (for requested daata)
            dataOut = new BufferedOutputStream(connect.getOutputStream());

            // get first line of the request from the client
            String input = in.readLine();

            // we parse the request with a string tokenizer
            StringTokenizer parse = new StringTokenizer(input);
            String method = parse.nextToken().toUpperCase(); // We get the HTTP method of the client

            // We get file requested
            fileRequested = parse.nextToken().toLowerCase();

            // We support only GET and Head methods, we check

            if (!method.equals("GET") && !method.equals("HEAD")) {
                if (isVerbose()) {
                    System.out.println("501 Not Implemented: " + method + " method.");
                }

                // Wee return the not supported file.
                File file = new File(getWebRoot(), getMethodNotSupported());
                int fileLength = (int) file.length();
                String contentMineType = "text/html";
                //read content to return to client

                byte[] fileData = readFileData(file, fileLength);

                // we send HTTP headers with data to client
                out.println("HTTP/1.1 501 Not Implemented");
                out.println("Server: Java HTTP Server from Divine Odazie : 1.0");
                out.println("Date: " + new Date());
                out.println("Content-type: " + contentMineType);
                out.println("Content-length: " + fileLength);
                out.println(); // A blank line between headers and content, Very Important !

                out.flush(); // flush character output stream buffer
                // file
                dataOut.write(fileData, 0, fileLength);
                dataOut.flush();

            } else {
                // GET or HEAD method
                if (fileRequested.endsWith("/")) {
                    fileRequested += getDefaultFile();
                }
                File file = new File(getWebRoot(), fileRequested);
                int fileLength = (int) file.length();
                String content = getContentType(fileRequested);

                if (method.equals("GET")) { // GET method so we return content
                    byte[] fileData = readFileData(file, fileLength);

                    // Send HTTP Headers
                    out.println("HTTP/1.1 200 OK!!");
                    out.println("Server: Java HTTP Server from Divine Odazie : 1.0");
                    out.println("Date: " + new Date());
                    out.println("Content-type: " + content);
                    out.println("Content-length: " + fileLength);
                    out.println(); // A blank line between headers and content, Very Important !
                    out.flush(); // flush character output stream buffer

                    dataOut.write(fileData, 0, fileLength);
                    dataOut.flush();

                }
                if (isVerbose()) {
                    System.out.println("File " + fileRequested + "of type " + content + " returned");

                }

            }

        }catch (FileNotFoundException fnfe){
            try {
                fileNotFound(out,dataOut,fileRequested);
            }catch (IOException ioe){
                System.err.println("Error with file not found exception: " + ioe.getMessage());
            }


        }catch (IOException e ){
            System.err.println("Server error: " + e);
        }finally {
            try {
                in.close(); // close character input stream
                out.close();
                dataOut.close();
                connect.close();
            } catch (Exception e) {
                System.err.println("Error closing stream: " + e.getMessage());
            }

            if (isVerbose()){
                System.out.println("Connection closed. \n");
            }
        }

    }

    private void fileNotFound(PrintWriter out, BufferedOutputStream dataOut, String fileRequested) throws IOException {
        File file = new File(getWebRoot(), getFileNotFound());
        int fileLength = (int) file.length();
        String content = "text/html";
        byte[] fileData = readFileData(file,fileLength);

        out.println("HTTP/1.1 404 OK!!");
        out.println("Server: Java HTTP Server from Divine Odazie : 1.0");
        out.println("Date: " + new Date());
        out.println("Content-type: " + content);
        out.println("Content-length: " + fileLength);
        out.println(); // A blank line between headers and content, Very Important !
        out.flush(); // flush character output stream buffer

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();

        if (isVerbose()){
            System.out.println("file " + fileRequested + " not found");

        }

    }

    private String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".htm") || fileRequested.endsWith(".html"))
            return "text/html";
        else
            return "text/plain";

    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        FileInputStream fileIn = null;
        byte [] fileData = new  byte[fileLength];
        try {
            fileIn = new FileInputStream(file);
            fileIn.read(fileData);
        } finally {
            if (fileIn != null)
                fileIn.close();
        }
        return fileData;
    }

    public void setConnect(Socket connect) {
        this.connect = connect;
    }

    public static File getWebRoot() {
        return WEB_ROOT;
    }

    public static String getDefaultFile() {
        return DEFAULT_FILE;
    }

    public static String getFileNotFound() {
        return FILE_NOT_FOUND;
    }

    public static String getMethodNotSupported() {
        return METHOD_NOT_SUPPORTED;
    }

    public static int getPORT() {
        return PORT;
    }

    public static boolean isVerbose() {
        return verbose;
    }
}
