package org.self.example;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by manthan on 1/4/16.
 */
public class DataSendService extends IntentService{
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */

    DCIO dcio;
    Socket sock;
    String lockFromDC="",response,initial_command_for_connection="";
    BufferedReader sin = null;
    PrintWriter sout=null;
    private int _port;
    InputStream input;
    OutputStream output;
    private String _hubHostname;
    protected BufferedSocket socket = null;
    protected InetAddress _ip;

    //Hub proto supports lists all supports needed to the hub
    public static final String _hubproto_supports = "UserCommand NoHello UserIP2 TTHSearch UGetBlock";
    protected String _botname="1234", _password=" ", _description, _conn_type ="1A", _email =_botname+"@sdslabs.co.in", _sharesize ="5368709120", _hubname;
    private String _hubSupports = "";
    protected static final String _protoVersion = "1.0091";
    public static String PARAM_OUT_MSG;
    String key;

    public DataSendService(){
        super("DataSendService");
        dcio=new DCIO();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            connect("dc.sdslabs.co.in", 411);
            Intent broadcast=new Intent();
            broadcast.addCategory(Intent.CATEGORY_DEFAULT);
            broadcast.setAction(MainActivity.DataReceiver.ACTION_RESP);
            broadcast.putExtra(PARAM_OUT_MSG,dcio.UserMap);
            sendBroadcast(broadcast);
        } catch (IOException e) {
            Log.e("IOEceptiom", e.getMessage());
        }
    }

    private void connect(String hostname, int port)throws IOException,BotException{

        String buffer;

        _port = port;
        _hubHostname = hostname;

        // connect to server
        socket = new BufferedSocket(hostname, port);
        input = socket.getInputStream();
        output = socket.getOutputStream();
        //breader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        _ip = socket.getInetAddress();

        buffer = ReadCommand();
        String lock = dcio.parseRawCmd(buffer)[1];

        if (lock.startsWith("EXTENDEDPROTOCOL")) {
            buffer = "$Supports " + _hubproto_supports + "|";
            SendCommand(buffer);
        }

        key= lock2key(lock);
        buffer = "$Key " + key + "|";
        Log.e("Key ",key);
        SendCommand(buffer);

        buffer = "$ValidateNick " + _botname + "|";
        SendCommand(buffer);

        buffer = ReadCommand();

        int maxIters = 30;
        while (buffer.startsWith("$Hello") != true) {
            if (buffer.startsWith("$ValidateDenide"))
                Log.e("Valiate ","Denied");
            //throw new BotException(BotException.Error.VALIDATE_DENIED);
            if (buffer.startsWith("$BadPass"))
                throw new BotException(BotException.Error.BAD_PASSWORD);

            if (buffer.startsWith("$GetPass")) {
                buffer = "$MyPass " + _password + "|";
                Log.e("GetPass ","Password sent");
                SendCommand(buffer);
            } else
            if (buffer.startsWith("$HubName ")) {
                _hubname = unescapeSpecial(buffer.substring(9, buffer.length() - 1));
                Log.e("hubname ","Denied");
            } else
            if (buffer.startsWith("$Supports ")) {
                _hubSupports = dcio.parseCmdArgs(buffer);
                Log.e("supports ","Support Reached");
            } else
            if (buffer.startsWith("<")) {
                processPublicMsg(buffer);
                Log.e("xml ", "Denied");
            }

            maxIters--;
            if(maxIters < 0) {
                throw new BotException("Hub taking too long to handshake. Aborting!",
                        BotException.Error.TIMEOUT);
            }

            try {
                buffer = ReadCommand();
            } catch (IOException e) {
                //Sends the last read command (this will usually be a message from the hub).
                throw new BotException(e.getMessage() + ": " + buffer, BotException.Error.IO_ERROR);
            }
        }
        Log.e("AfterVersion", "Reached");

        buffer = "$Version " + _protoVersion + "|";
        SendCommand(buffer);

        buffer = "$GetNickList|";
        SendCommand(buffer);

        sendMyINFO();
        dcio.ReadCommand4(input);

        DisplayUser();

    }
    synchronized protected void sendMyINFO() throws IOException {
        String buffer = "$MyINFO $ALL " + _botname + " <++ V:0.2,M:A,H:0/1/0,S:5>" + "$ $" +
                _conn_type + "$" + _email + "$" + _sharesize + "$|";
        SendCommand(buffer);
    }

    public void DisplayUser(){
        Set keys=dcio.UserMap.keySet();
        Iterator it=dcio.UserMap.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry pair=(Map.Entry)it.next();
            Log.d("User Display:",(String)pair.getKey()+":"+(String)pair.getValue());
        }
    }

    private final String lock2key(String lock) {
        String key_return;
        int len = lock.length();
        char[] key = new char[len];
        for (int i = 1; i < len; i++)
            key[i] = (char) (lock.charAt(i) ^ lock.charAt(i - 1));
        key[0] = (char) (lock.charAt(0) ^ lock.charAt(len - 1) ^ lock.charAt(len - 2) ^ 5);
        for (int i = 0; i < len; i++)
            key[i] = (char) (((key[i] << 4) & 240) | ((key[i] >> 4) & 15));

        key_return = new String();
        for (int i = 0; i < len; i++) {
            if (key[i] == 0) {
                key_return += "/%DCN000%/";
            } else if (key[i] == 5) {
                key_return += "/%DCN005%/";
            } else if (key[i] == 36) {
                key_return += "/%DCN036%/";
            } else if (key[i] == 96) {
                key_return += "/%DCN096%/";
            } else if (key[i] == 124) {
                key_return += "/%DCN124%/";
            } else if (key[i] == 126) {
                key_return += "/%DCN126%/";
            } else {
                key_return += key[i];
            }
        }

        return key_return;
    }

    protected final String ReadCommand() throws IOException {
        return dcio.ReadCommand(input);
    }
    protected final void SendCommand(final String buffer) throws IOException {
        if (output != null) {
            dcio.SendCommand(buffer, output);
        }
    }
    final public static String unescapeSpecial(String msg) {
        return msg.replace('$', ' ').replace("&#36;", "$").replace("&#124;", "|").replace("&amp;", "&");
    }
    private void processPublicMsg(String rawCommand) {
        String user, message;
        user = rawCommand.substring(1, rawCommand.indexOf('>'));
        message = rawCommand.substring(rawCommand.indexOf('>'));
        message = message.substring(2, message.length() - 1);
    }

    private void connectToPeer(){

    }
}
