package chat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * Created by weijiangan on 12/10/2016.
 */
public class Message implements Serializable {
    private String fr;
    private String msg;
    private boolean broadcast;
    private LocalDateTime ldt;

    public Message(String fr, String msg) {
        this.fr = fr;
        this.msg = msg;
        this.broadcast = false;
        this.ldt = LocalDateTime.now();
    }

    public Message(String fr, String msg, LocalDateTime ldt) {
        this.fr = fr;
        this.msg = msg;
        this.ldt = ldt;
    }

    public String getFr() {
        return fr;
    }

    public void setFr(String fr) {
        this.fr = fr;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public boolean isBroadcast() {
        return broadcast;
    }

    public void setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }

    public LocalDateTime getLdt() {
        return ldt;
    }

    public void setLdt(LocalDateTime ldt) {
        this.ldt = ldt;
    }
}
