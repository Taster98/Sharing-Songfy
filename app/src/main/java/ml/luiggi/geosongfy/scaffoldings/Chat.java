package ml.luiggi.geosongfy.scaffoldings;

import java.io.Serializable;
import java.util.ArrayList;

public class Chat implements Serializable {
    private String chatId, chatName;
    private ArrayList<Friend> friendArrayList = new ArrayList<>();

    public Chat(String ci){
        chatId=ci;
    }
    public Chat(String ci,String chName){
        chatId=ci;
        chatName=chName;
    }

    public String getChatId() {
        return chatId;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public void setFriendArrayList(ArrayList<Friend> friendArrayList) {
        this.friendArrayList = friendArrayList;
    }

    public ArrayList<Friend> getFriendArrayList() {
        return friendArrayList;
    }

    public void addFriendToArrayList(Friend mUser) {
        this.friendArrayList.add(mUser);
    }
}
