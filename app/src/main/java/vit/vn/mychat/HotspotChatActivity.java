package vit.vn.mychat;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.apache.commons.lang3.SerializationUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import vit.vn.mychat.adapter.HotspotMessageAdapter;
import vit.vn.mychat.model.ChatBotMessage;

@SuppressWarnings("ALL")
public class HotspotChatActivity extends AppCompatActivity {

    static final int SocketServerPORT = 8080;
    private static final int SHARE_PICTURE = 2;
    private static final int REQUEST_PATH = 1;
    private static final int TIMEOUT = 3000;
    private static final int MAXFILELEN = 65000;

    final int portNum = 3238;
    InetAddress ip = null;
    NetworkInterface networkInterface = null;
    ServerSocket serverSocket;
//    FileReciveThread fileReciveThread;
    private MulticastSocket socket;
    private InetAddress group;
    private MulticastSocket fileSocket;
    private InetAddress fileGroup;
    public static String username;
    private String chatname;
    private String curFileName;


    private Toolbar mToolbar;
    RecyclerView recyclerView;
    EditText editText;
    RelativeLayout addBtn;
    private ArrayList<ChatBotMessage> lst;
    private LinearLayoutManager mLinearLayout;
    private HotspotMessageAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotspot_chat);
        setToolbar();
        username = (String) getIntent().getExtras().get("name");
        addControls();

        addEvents();

        setWifi();

    }

    private void setWifi() {
        WifiManager wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);//Call Wi-Fi service
        if (wifi != null) {
            WifiManager.MulticastLock lock =
                    wifi.createMulticastLock("My Chat");//create a lock to transfer data between peers
            lock.setReferenceCounted(true);
            lock.acquire();
        } else {
            Toast.makeText(getApplicationContext(), "Unable to acquire multicast lock", Toast.LENGTH_SHORT).show();

            finish();
        }

        try {
            if (socket == null) {
                Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
                while (enumNetworkInterfaces.hasMoreElements()) {

                    networkInterface = enumNetworkInterfaces.nextElement();
                    Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();

                    while (enumInetAddress.hasMoreElements()) {
                        InetAddress inetAddress = enumInetAddress.nextElement();

                        if (inetAddress.isSiteLocalAddress()) {
                            ip = inetAddress;
                            break;
                        }
                    }
                    if (ip != null) {
                        break;
                    }
                }
                socket = new MulticastSocket(portNum);
                socket.setInterface(ip);
                socket.setBroadcast(true);

                group = InetAddress.getByName("224.0.0.1");//224.0.0.1
                socket.joinGroup(new InetSocketAddress(group, portNum), networkInterface);

                fileSocket = new MulticastSocket(portNum + 1);
                fileSocket.setInterface(ip);
                fileSocket.setBroadcast(true);

                fileGroup = InetAddress.getByName("224.0.0.2");
                fileSocket.joinGroup(new InetSocketAddress(fileGroup, (portNum + 1)), networkInterface);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ReceiverMessage recvMsgThread = new ReceiverMessage();//Data Messages receiver client
        recvMsgThread.execute((Void) null);

    }

    private void addEvents() {
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = editText.getText().toString().trim();
                editText.setText("");
                if (!message.equals("")) {
                    SendMessage sendMessage = new SendMessage(message);
                    sendMessage.execute((Void) null);
                }
            }
        });
    }

    private void addControls() {
        editText = findViewById(R.id.editText);
        addBtn = findViewById(R.id.addBtn);
        lst = new ArrayList<>();
        mAdapter = new HotspotMessageAdapter(lst);
        mLinearLayout = new LinearLayoutManager(this);
        recyclerView = findViewById(R.id.hotspot_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLinearLayout);
        recyclerView.setAdapter(mAdapter);

    }

    private void setToolbar() {
        mToolbar = findViewById(R.id.hotspot_chat_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Room Chat");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private class SendMessage extends AsyncTask<Void, Void, Boolean> {//class for send messages as byte arrays

        ChatBotMessage m;
        String textMsg;

        SendMessage(String message) {
            m = new ChatBotMessage(message, username);
            textMsg = message;
            mAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(lst.size() - 1);

        }

        @Override
        protected Boolean doInBackground(Void... params) {
            byte[] data = SerializationUtils.serialize(m);
            DatagramPacket packet = new DatagramPacket(data, data.length, group, portNum);

            try {
                socket.send(packet);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }

    private class ReceiverMessage extends AsyncTask<Void, Void, Boolean> {//Asynchronous type class to receive messages
        ReceiverMessage(){}

        @Override
        protected Boolean doInBackground(Void... voids) {

            Thread newThread = new Thread() {

                public void run() {
                    while (true) {
                        byte[] recvPkt = new byte[1024];
                        DatagramPacket recv = new DatagramPacket(recvPkt, recvPkt.length);//class to get data packet
                        try {
                            socket.receive(recv);//get data from Multicast Socket
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ChatBotMessage m = SerializationUtils.deserialize(recvPkt);
                        lst.add(m);

                        try {
                            mAdapter.notifyDataSetChanged();
                            recyclerView.scrollToPosition(lst.size() - 1);
                        } catch (Exception e) {
                        }
                    }
                }
            };
            newThread.start();
            return null;
        }
    }


    @Override
    protected void onDestroy() {// final moment to close the server socket
        super.onDestroy();

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

//        fileReciveThread = new FileReciveThread();
//        fileReciveThread.start();

        try {
            socket = new MulticastSocket(portNum);
            socket.setInterface(ip);
            socket.setBroadcast(true);

            group = InetAddress.getByName("224.0.0.1");
            socket.joinGroup(new InetSocketAddress(group, portNum), networkInterface);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //method get IP address from network interface
    private String getIpAddress() {
        String ip = "";
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {

                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();

                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();

                    if (inetAddress.isSiteLocalAddress()) {
                        ip += inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            ip += "Something Wrong! " + e.toString() + "\n";
        }
        return ip;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // See which child activity is calling us back.
        if (requestCode == REQUEST_PATH) {
            if (resultCode == RESULT_OK) {
                curFileName = data.getStringExtra("GetPath");
                curFileName += data.getStringExtra("GetFileName");
//                edittext.setText(curFileName);
            }
        }
    }
}


