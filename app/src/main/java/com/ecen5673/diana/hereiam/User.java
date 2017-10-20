package com.ecen5673.diana.hereiam;

import java.util.Comparator;
import java.util.HashMap;

/**
 * Created by diana on 9/28/16.
 *
 *
 */
public class User implements Comparator<User>{

    // TODO: Implement user password

    private String userName;
    private String userID;
    private String alertMessage;
    private String location;
    private String address;
    private String updateTime;
    private String activity;
    private HashMap<String, String> friends;

    private static int defaultUserID = 0;


    public User(){
        this("Darth Vader");
    }

    public User(String username){
        this(username, String.valueOf(defaultUserID));
        defaultUserID++;
    }

    public User(String userName, String userID){
        // Default constructor class, used to just create test user detail records
        this.userName = userName;
        this.userID = userID;
        this.location = "0,0";
        this.address = "Unavailable";
        this.updateTime = "2000-01-01 00:00:00.00";
        this.activity = "Unavailable";
        this.alertMessage = "I am in need of assistance!";
        this.friends = new HashMap<>();
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public String getLocation() {
        return location;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public HashMap<String, String> getFriends() {
        return friends;
    }

    public void setFriends(HashMap<String, String> friends) {
        this.friends = friends;
    }

    public void addFriend(String groupID){
        this.friends.put(groupID, groupID);
    }

    public void removeFriend(String groupID){
        if (this.getUserName().contains(groupID))
            this.friends.remove(groupID);
    }

    public void updateUser(User newUser){
        this.userName = newUser.getUserName();
        this.updateTime = newUser.getUpdateTime();
        this.activity = newUser.getActivity();
        this.address = newUser.getAddress();
        this.location = newUser.getLocation();
    }

    @Override
    public String toString(){
        return "User: " + this.userName + ", \t" +
                "ID: " + this.userID + ", \n" +
                "Current location: " + this.location + ", \t" +
                "Address: " + this.address + ", \n" +
                "Last updated: " + this.updateTime + ", \t" +
                "Current activity: " + this.activity + "\n" +
                "Number of friends: " + this.friends.size() + "\n";
    }

    // Compares users by most recently updated
    @Override
    public int compare(User o1, User o2) {
        // Return by who had been most recently updated
//        Date user1Date = o1.getUpdateTime();
        return 0;
    }
}
