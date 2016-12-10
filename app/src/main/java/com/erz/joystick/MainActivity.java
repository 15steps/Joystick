package com.erz.joystick;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.erz.joysticklibrary.JoyStick;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements JoyStick.JoyStickListener {
    public Button btnShoot;
    public String serverIP;
    public int userID;
    public DatagramSocket outputSocket;
    public ExecutorService executor;
    public static final int serverPort = 12345;
    public InetAddress serverAddress;
    public double rotation = -1;
    public int localPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        executor = Executors.newFixedThreadPool(2);

        JoyStick joy1 = (JoyStick) findViewById(R.id.joy1);
        joy1.setListener(this);
        joy1.setPadColor(Color.parseColor("#55ffffff"));
        joy1.setButtonColor(Color.parseColor("#55ff0000"));

        TextView txtVID = (TextView) findViewById(R.id.textViewID);

        Intent intent = getIntent();
        userID = (int) intent.getExtras().get("id");
        serverIP = (String) intent.getExtras().get("ip");
        localPort = (int) intent.getExtras().get("port");
        txtVID.setText("ID: " + userID);

        try {
            serverAddress = InetAddress.getByName(serverIP);
            outputSocket = new DatagramSocket(localPort);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        btnShoot = (Button) findViewById(R.id.buttonShoot);
        btnShoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = "s " + userID;
                executor.execute(new Sender(message));
            }
        });
    }

    @Override
    public void onMove(JoyStick joyStick, double angle, double power) {
        switch (joyStick.getId()) {
            case R.id.joy1:
                //System.out.println("Current Angle: " + joyStick.getAngle());
                rotation = joyStick.getJSRotation();
                this.executor.execute(new Sender("r " + rotation + " " + userID));
                break;
        }
    }

    class Sender implements Runnable{
        private byte[] data;

        public Sender(String message) {
            this.data = message.getBytes();
        }

        @Override
        public void run() {
            DatagramPacket pkt = new DatagramPacket(data, data.length, serverAddress, serverPort);
            try {
                outputSocket.send(pkt);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}