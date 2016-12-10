package com.erz.joystick;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Initial extends AppCompatActivity {
    public static final int serverPort = 12345;
    public int localPort;
    int userID;
    String userName;
    String serverIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial);

        Button btnConnect = (Button) findViewById(R.id.buttonConnect);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editUser = (EditText) findViewById(R.id.editTextUser);
                EditText editIP = (EditText) findViewById(R.id.editTextIP);
                userName = editUser.getText().toString();
                serverIP = editIP.getText().toString();
                userID = -1;

                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.execute(new Connection());
                executor.shutdown();

                while(!executor.isTerminated()) {}

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("id", userID);
                intent.putExtra("ip", serverIP);
                intent.putExtra("port", localPort);
                startActivity(intent);
            }
        });
    }

    class Connection implements Runnable{

        @Override
        public void run() {
            try {
                // Sending connection message to server
                InetAddress srvAddress = InetAddress.getByName(serverIP);
                DatagramSocket sendSocket = new DatagramSocket();
                localPort = sendSocket.getLocalPort();
                System.out.println("LOCAL_PORT: " + localPort);
                //sendSocket.bind(new InetSocketAddress(localPort));
                byte[] dataOut = ("C " + userName).getBytes();
                DatagramPacket sendPkt = new DatagramPacket(dataOut, dataOut.length, srvAddress, serverPort);
                sendSocket.send(sendPkt);
                sendSocket.close();

                //Retrieving user ID
                DatagramSocket inputSocket = new DatagramSocket(localPort);
                byte[] receiveData = new byte[1024];
                DatagramPacket inputPkt = new DatagramPacket(receiveData, receiveData.length);
                inputSocket.receive(inputPkt);
                String[] message = new String(inputPkt.getData()).trim().split(" ");
                //System.out.println("MENSAGEM: " + message[0] + " " + message[1]);
                userID = Integer.parseInt(message[1]);
                inputSocket.close();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
