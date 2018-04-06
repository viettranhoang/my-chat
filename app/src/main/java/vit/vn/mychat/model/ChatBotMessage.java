package vit.vn.mychat.model;


import java.io.Serializable;

/**
 * Created by beast on 14/4/17.
 */

public class ChatBotMessage implements Serializable {

    private String msgText;
    private String msgUser;



    public ChatBotMessage(String msgText, String msgUser){
        this.msgText = msgText;
        this.msgUser = msgUser;

    }


    public ChatBotMessage(){

    }

    public String getMsgText() {
        return msgText;
    }

    public void setMsgText(String msgText) {
        this.msgText = msgText;
    }

    public String getMsgUser() {
        return msgUser;
    }

    public void setMsgUser(String msgUser) {
        this.msgUser = msgUser;
    }
}
