package chat;

import java.io.*;
import java.net.*;

/**
 * Created by weijiangan on 19/09/2016.
 */
public class User implements Serializable {
    public enum Type {
        CLIENT, AGENT
    }

    private String id;
    private String pw;
    private Type type;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public User() {};

    public User(String id, String pw) {
        this.id = id;
        this.pw = pw;
        this.type = null;
        this.socket = null;
        this.in = null;
        this.out = null;
    }

    public User(String id, String pw, Type type) {
        this.id = id;
        this.pw = pw;
        this.type = type;
        this.socket = null;
        this.in = null;
        this.out = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPw() {
        return pw;
    }

    public void setPw(String pw) {
        this.pw = pw;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public ObjectInputStream getIn() {
        return in;
    }

    public void setIn(ObjectInputStream in) {
        this.in = in;
    }

    public ObjectOutputStream getOut() {
        return out;
    }

    public void setOut(ObjectOutputStream out) {
        this.out = out;
    }
}
