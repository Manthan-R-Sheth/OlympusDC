package org.self.example;

/**
 * Created by manthan on 30/3/16.
 */
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created on 26-May-08<br>
 * The purpose of this class is to implement methods that are needed for IO easily send and receive and parse commands.
 *
 * @author AppleGrew
 * @since 0.7
 * @version 0.1.2
 *
 */
class DCIO {
    private String ioexception_msg;

    void set_IOExceptionMsg(String msg) {
        ioexception_msg = msg;
    }

    /**
     * Reading raw command from <i>in</i>.
     *
     * @param input stream from which to read the command.
     * @return Command from hub
     * @throws IOException
     */
    final String ReadCommand3(InputStream in) throws IOException{
        byte[] resultBuff = new byte[0];
        byte[] buff = new byte[1024];
        int MaxIter=5;
        int k = -1;
        while((k = in.read(buff, 0, buff.length)) != -1 && MaxIter>0) {
            byte[] tbuff = new byte[resultBuff.length + k]; // temp buffer size = bytes already read + bytes last read
            System.arraycopy(resultBuff, 0, tbuff, 0, resultBuff.length); // copy previous bytes
            System.arraycopy(buff, 0, tbuff, resultBuff.length, k);  // copy current lot
            resultBuff = tbuff; // call the temp buffer as your result buff
            MaxIter--;
            Log.d("MaxIter: ",MaxIter+"");
        }
        System.out.println(resultBuff.length + " bytes read.");
        Log.d("Readcommand3:",new String(resultBuff,"UTF-8")+"");
        return new String(resultBuff, "UTF-8");
    }

    final String ReadCommand(InputStream in) throws IOException {
        int c;
        //Changing to StringBuffer from String. Artifact#2934462.
        StringBuffer buffer = new StringBuffer();
        do {
            c = in.read();
            if (c == -1) {
                if (ioexception_msg == null)
                    ioexception_msg = "Premature End of Socket stream or no data in it";
                throw new IOException(ioexception_msg);
            }
            buffer.append((char) c);
        } while (c != '|');

        Log.d("remote ","From remote: " + buffer);
        return buffer.toString();
    }

    final String ReadCommand(Socket socket) throws IOException {
        //Removed BufferedInputStream from here,
        //which was added as part of artifact#2934462.
        //The local BufferedInputStream caused loss in data.
        return ReadCommand(socket.getInputStream());
    }

    /**
     * Sends raw command to <i>out</i>.
     *
     * @param buffer
     *                Line which needs to be send. This method won't append "|" on the end on the string if it doesn't exist, so it is up to make
     *                sure buffer ends with "|" if you calling this method.
     * @param out The socket stream into which to write the raw command.
     * @throws IOException
     */
    final void SendCommand(String buffer, OutputStream out) throws IOException {
        byte[] bytes = new byte[buffer.length()];
        for (int i = 0; i < buffer.length(); i++)
            bytes[i] = (byte) buffer.charAt(i);

        Log.d("bot: ","From bot: " + buffer);
        out.write(bytes);
    }

    final void SendCommand(final String buffer, final Socket socket) throws IOException {
        final BufferedOutputStream bufOut = new BufferedOutputStream(socket.getOutputStream());
        SendCommand(buffer, bufOut);
        bufOut.flush();
    }

    /**
     * Parses the given raw command and returns the command name in position 0 and the rest arguments in later slots.<br>
     * <b>Note:</b> This is a simple generalized parser. It simply splits at point of white space, hence it is not useful to
     * parse private/public messages etc.
     * @param cmd The raw command to parse.
     * @return
     */
    final String[] parseRawCmd(String cmd) {
        String tbuffer[] = null;
        String buffer[] = cmd.split(" ");
        if (buffer[0].startsWith("$"))
            buffer[0] = buffer[0].substring(1);
        int last = buffer.length - 1;
        if (buffer[last].endsWith("|")) {
            if (buffer[last].length() == 1) {
                tbuffer = new String[buffer.length - 1];
                System.arraycopy(buffer, 0, tbuffer, 0, tbuffer.length);
            } else {
                buffer[last] = buffer[last].substring(0, buffer[last].length() - 1);
                tbuffer = buffer;
            }
        }
        return tbuffer;
    }

    /**
     * Parses a raw command for the command name.
     * @param cmd The raw command to parse.
     * @return
     */
    final String parseCmdName(String cmd) {
        return cmd.substring(1, cmd.indexOf(' '));
    }

    /**
     * Parses a raw command for the command's arguments, i.e. Everything in the raw command except the command name and the trailing pipe (|).
     * @param cmd The raw command to parse.
     * @return
     */
    final String parseCmdArgs(String cmd) {
        return cmd.substring(cmd.indexOf(' '), cmd.lastIndexOf('|')).trim();
    }
}
