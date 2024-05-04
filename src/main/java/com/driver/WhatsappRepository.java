package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;



@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.
    private HashMap<String,String> usersDb;
    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message,Integer> messageDb;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){
        this.usersDb = new HashMap();
        this.groupMessageMap = new HashMap<Group, List<Message>>();
        this.groupUserMap = new HashMap<Group, List<User>>();
        this.senderMap = new HashMap<Message, User>();
        this.adminMap = new HashMap<Group, User>();
        this.userMobile = new HashSet<>();
        this.messageDb = new HashMap<>();
        this.customGroupCount = 0;
        this.messageId = 0;
    }

    public String createUser(String name, String mobile) throws Exception{
        if(usersDb.containsKey(mobile)){
            throw new Exception("User already exist");
        }else{
            usersDb.put(mobile, name);
        }
        return "SUCCESS";
    }

    public String createGroup(List<User> users){

        // The list contains at least 2 users where the first user is the admin.
        // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
        // If there are 2+ users, the name of group should be "Group #count". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
        // Note that a personal chat is not considered a group and the count is not updated for personal chats.

       int size = users.size();
       if(size == 2){
           Group newgroup = new Group();
           newgroup.setName(users.get(1).getName());
           groupUserMap.put(newgroup,users);
           adminMap.put(newgroup, users.get(1));
       }
       if(size > 2){
           int ct = 0;
           int mapsize = groupUserMap.size();
           for(Group group : groupUserMap.keySet()){
               if(!group.getName().contains("Group")){
                   ct++;
               }
           }
           Group newgroup = new Group();
           newgroup.setName("Group " + (mapsize - ct + 1));
           groupUserMap.put(newgroup,users);
           adminMap.put(newgroup, users.get(0));
       }

       return "SUCCESS";

    }

    public int createMessage(String content){

        int Dbsize = messageDb.size();
        Message message = new Message();
        message.setContent(content);
        message.setId(Dbsize+1);
        messageDb.put(message, message.getId());

        return message.getId();
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception{

        if(!groupMessageMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
       List<User> userList =  groupUserMap.get(group);
        if(!userList.contains(sender)){
            throw new Exception("You are not allowed to send message");

        }

        List<Message> messageList = groupMessageMap.get(group);
        messageList.add(message);
        groupMessageMap.put(group, messageList);
        senderMap.put(message, sender);

        return messageList.size();

    }

    public String changeAdmin(User approver, User user, Group group) throws Exception{
        //Change the admin of the group to "user".
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group

        if(!adminMap.containsKey(group)){
            throw new Exception("Group does not exist");
        }
        List<User> usersList = groupUserMap.get(group);
        if(!usersList.contains(user)){
            throw new Exception("User is not a participant");
        }
        if(adminMap.get(group).equals(approver)){
            adminMap.put(group, user);

        }else{
            throw new Exception("Approver does not have rights");
        }


        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception{
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.

        boolean userFound = false;
        for (Group group : groupUserMap.keySet()) {
            List<User> userList = groupUserMap.get(group);
            if (userList.contains(user)) {
                userFound = true;
                if (adminMap.containsValue(user)) {
                    throw new Exception("Cannot remove admin");
                } else {
                    usersDb.remove(user.getName());
                    userList.remove(user);
                    groupUserMap.put(group, userList);
                    // Remove messages associated with the user
                    List<String> messagesToRemove = new ArrayList<>();
                    Iterator<Map.Entry<Message, User>> iterator = senderMap.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<Message, User> entry = iterator.next();
                        if (entry.getValue().equals(user)) {
                            messagesToRemove.add(String.valueOf(entry.getKey())); // Collect messages to remove
                            iterator.remove();
                        }
                    }

                    // Now remove the collected messages from groupMessageMap
                    for (String message : messagesToRemove) {

                        for(List<Message> msgList : groupMessageMap.values()){
                            if(msgList.contains(message)){
                                msgList.remove(message);
                            }
                        }
                    }
                }
            }
        }
        if (!userFound) {
            throw new Exception("User not found");
        }

        int ctOfUsersAndMessages = 0;
        for(Group group : groupUserMap.keySet()){
            ctOfUsersAndMessages += groupUserMap.get(group).size();
        }

        for(Group group : groupMessageMap.keySet()){
            ctOfUsersAndMessages += groupMessageMap.get(group).size();
        }

        return ctOfUsersAndMessages;
    }

    public String findMessage(Date start, Date end, int K) throws Exception {
        return "";
    }
}
