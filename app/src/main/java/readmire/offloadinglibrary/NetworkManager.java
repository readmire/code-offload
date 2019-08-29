package readmire.offloadinglibrary;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

/**
 *
 *    @yasemin
 **/

public class NetworkManager {
    public static NetworkManager networkManager=null;
    // DEFAULT IP
    public static String SERVERIP = "";

    // DESIGNATE A PORT
    public static final int SERVERPORT = 8000;

    public Handler handler;

    private ServerSocket serverSocket;

    private boolean connected = false;
    private long serverElapsed;
    private long clientElapsed;
    private Socket socket;
    TextView txtinfo;
    private byte [] imgbyte;
    String filepath;

    public static final String DATA_PATH = Environment
            .getExternalStorageDirectory().toString() + "/EyeDentify/";

    private NetworkManager(){
              Looper.prepare();
              handler = new Handler();
    }
    public void setMyView(TextView tv){
        txtinfo = tv;
    }
    public static NetworkManager getInstance(){
        if(networkManager == null)
            networkManager = new NetworkManager();

        return networkManager;
    }

    public void startListening(){
        Thread t = new Thread(new ServerThread());
        t.start();
    }

    public String getLocalIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces
                        .nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface
                        .getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress
                            .nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += " Server running at : "
                                + inetAddress.getHostAddress();
                    }
                }
            }

        } catch (SocketException e) {
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }


    public void sendData(byte[] imgbyte){
        try {
            Log.d("ClientActivity", "C: Connecting...");
            socket = new Socket(SERVERIP, SERVERPORT);
            socket.setKeepAlive(true);
            //socket.setTcpNoDelay(true);
            connected = true;
            Log.d("ClientActivity", "C: Sending command.");
            //while (connected) {
            try {

                Log.d("ClientActivity", "C: Sending command.");

                OutputStream output = socket.getOutputStream();
                InputStream is = socket.getInputStream();

                long start = System.currentTimeMillis();

                Log.d("ClientActivity", "C: image writing.");
                BufferedOutputStream bos = new BufferedOutputStream(output);
                bos.write(imgbyte);
                bos.flush();
                socket.shutdownOutput();

                ObjectInputStream ois = new ObjectInputStream(is);
                //mFeatureVector = (FeatureVector) ois.readObject();

                serverElapsed = System.currentTimeMillis() - start;

                //Log.d("ClientActivity", "FeatureVector : "+ mFeatureVector.toString() );
                Log.d("ClientActivity", "C: read from server "+serverElapsed );
            } catch (Exception e) {
                Log.d("ClientActivity", "C: Error", e);
            }
            socket.close();
            Log.d("ClientActivity", "C: Closed.");
        } catch (Exception e) {
            Log.d("ClientActivity", "C: Error", e);
            connected = false;
        }
    }
    private class ClientThread implements Runnable {

        public void run() {
            try {
                Looper.prepare();
                Log.d("ClientActivity", "C: Connecting...");
                socket = new Socket(SERVERIP, SERVERPORT);
                socket.setKeepAlive(true);
                //socket.setTcpNoDelay(true);
                connected = true;
                Log.d("ClientActivity", "C: Sending command.");
                //while (connected) {
                try {

                    Log.d("ClientActivity", "C: Sending command.");

                    OutputStream output = socket.getOutputStream();
                    InputStream is = socket.getInputStream();

                    long start = System.currentTimeMillis();
                    Log.d("ClientActivity", "C: image writing.");
                    BufferedOutputStream bos = new BufferedOutputStream(output);
                    bos.write(imgbyte);
                    bos.flush();
                    socket.shutdownOutput();

                    ObjectInputStream ois = new ObjectInputStream(is);
                    //mFeatureVector = (FeatureVector) ois.readObject();

                    serverElapsed = System.currentTimeMillis() - start;

                    //Log.d("ClientActivity", "FeatureVector : "+ mFeatureVector.toString() );

                    Log.d("ClientActivity", "C: read from server"+serverElapsed );
                } catch (Exception e) {
                    Log.d("ClientActivity", "C: Error", e);
                }
                //}
                socket.close();
                Log.d("ClientActivity", "C: Closed.");
            } catch (Exception e) {
                Log.d("ClientActivity", "C: Error", e);
                connected = false;
            }
        }
    }
    private class ServerThread implements Runnable{
        String serverTime =" ";
        public void run() {
            try {
                Looper.prepare();
                if (SERVERIP != null) {

                    serverSocket = new ServerSocket(SERVERPORT);
                    while (true) {
                        // LISTEN FOR INCOMING CLIENTS
                        final Socket client = serverSocket.accept();
                        try {
                            int bytesRead =0;
                            byte[] buffer = new byte[16*1024];

                            long start1 = System.currentTimeMillis();

                            InputStream is = client.getInputStream();
                            OutputStream os = client.getOutputStream();
                            BufferedInputStream bis = new BufferedInputStream(is);
                            File myFile = new File(DATA_PATH+"test.dat");
                            FileOutputStream fos = new FileOutputStream(myFile); // destination path and name of file
                            //FileOutputStream fos = new FileOutputStream("/storage/sdcard0/Pictures/Screenshots/");
                            BufferedOutputStream bos = new BufferedOutputStream(fos);
                            //bytesRead = is.read(mybytearray2,0,mybytearray2.length);
                            //current = bytesRead;

                            double okunan = 0;

                            while((bytesRead = bis.read(buffer)) != -1){
                                bos.write(buffer,0,bytesRead);
                                okunan +=bytesRead;
                            }
                            bos.flush();
                            long end1 = System.currentTimeMillis();

                            serverTime += " Network: "+(end1-start1) +" DataSize: "+ okunan + " ";

                            long m1 = System.currentTimeMillis();
                            byte[] bFile = new byte[(int) myFile.length()];

                            //read file into bytes[]
                            FileInputStream fileInputStream = new FileInputStream(myFile);
                            fileInputStream.read(bFile);
                            long start2 = System.currentTimeMillis();
                            serverTime += " Read File: " +(start2 - m1);
                           // ImageCompression ic = new ImageCompression();
                            //RGB24Image rgb24Image = ic.getResizedRGB24from(bFile);

                            long stime1 = System.currentTimeMillis();

                            serverTime += " Compress: "+(stime1-start2);

                            //FeatureVectorServiceImpl fi = new FeatureVectorServiceImpl();
                           // FeatureVector mFeatureVector = fi.getFeatureVector(rgb24Image,null);

                            long end2 = System.currentTimeMillis();

                            serverTime += " ServerProcess: "+ (end2-start2)+" ";
                            //os = client.getOutputStream();
                            ObjectOutputStream objectos = new ObjectOutputStream(client.getOutputStream());
                            objectos.writeObject(new Byte[1024]);
                            objectos.flush();
                            objectos.close();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    txtinfo.setText(serverTime);
                                }
                            });
                            String fvector = "Feature Vector: "+ (end2-stime1);
                            File f = new File(Environment.getExternalStorageDirectory().toString() + "/EyeDentify/","measurement.txt");
                            FileOutputStream mfos = new FileOutputStream(f,true);
                            mfos.write(serverTime.getBytes());
                            mfos.write(fvector.getBytes());
                            mfos.write("\r\n \r\n ---------------------------------------- \r\n".getBytes());
                            mfos.flush();
                            mfos.close();

                        } catch (Exception e) {

                            e.printStackTrace();
                        }
                    }
                } else {

                }
            } catch (Exception e) {
                final String er = e.getMessage();
                e.printStackTrace();
            }
        }
    }

}
