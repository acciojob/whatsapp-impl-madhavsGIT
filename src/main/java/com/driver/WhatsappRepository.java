package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;



@Repository
public class WhatsappRepository {

    //Assume that each user belongs to at most one group
    //You can use the below mentioned hashmaps or delete these and create your own.

    private HashMap<Group, List<User>> groupUserMap;
    private HashMap<Group, List<Message>> groupMessageMap;
    private HashMap<Message,Integer> messageDb;
    private HashMap<Message, User> senderMap;
    private HashMap<Group, User> adminMap;
    private HashSet<String> userMobile;
    private int customGroupCount;
    private int messageId;

    public WhatsappRepository(){

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
        if(userMobile.contains(mobile)){
            throw new RuntimeException("User already exists");
        }else{
            User user = new User();
            user.setName(name);
            user.setMobile(mobile);

            userMobile.add(mobile);
        }
        return "SUCCESS";
    }

    public Group createGroup(List<User> users){

        // The list contains at least 2 users where the first user is the admin.
        // If there are only 2 users, the group is a personal chat and the group name should be kept as the name of the second user(other than admin)
        // If there are 2+ users, the name of group should be "Group #count". For example, the name of first group would be "Group 1", second would be "Group 2" and so on.
        // Note that a personal chat is not considered a group and the count is not updated for personal chats.

//       int size = users.size();
//       if(size == 2){
//           Group newgroup = new Group();
//           newgroup.setName(users.get(1).getName());
//           groupUserMap.put(newgroup,users);
//           groupMessageMap.put(newgroup,new ArrayList<Message>());
//          // adminMap.put(newgroup, users.get(1));
//           return newgroup;
//       }
//       else{
//           customGroupCount++;
//
//           Group newgroup = new Group();
//           newgroup.setName("Group " + (customGroupCount));
//           groupMessageMap.put(newgroup,new ArrayList<Message>());
//           groupUserMap.put(newgroup,users);
//           adminMap.put(newgroup, users.get(0));
//           return newgroup;
//       }

        if(users.size() == 2){
            Group group = new Group(users.get(1).getName(),users.size());
            groupUserMap.put(group,users);
            groupMessageMap.put(group,new ArrayList<Message>());
            return group;
        }else {
            customGroupCount++;
            Group group = new Group("Group " + customGroupCount,users.size());
            adminMap.put(group,users.get(0));
            groupMessageMap.put(group,new ArrayList<Message>());
            groupUserMap.put(group,users);
            return group;
        }



    }

    public int createMessage(String content){

        messageId++;
        Message message = new Message();
        message.setContent(content);
        message.setId(messageId);
        messageDb.put(message, message.getId());

        return message.getId();
    }

//    public int sendMessage(Message message, User sender, Group group) throws Exception{
//
//        if(!groupMessageMap.containsKey(group)){
//            throw new Exception("Group does not exist");
//        }
//       List<User> userList =  groupUserMap.get(group);
//        if(!userList.contains(sender)){
//            throw new Exception("You are not allowed to send message");
//
//        }
//
//        List<Message> messageList = groupMessageMap.get(group);
//        messageList.add(message);
//        groupMessageMap.put(group, messageList);
//        senderMap.put(message, sender);
//
//        return messageList.size();
//
//    }


    public int sendMessage(Message message, User sender, Group group) throws RuntimeException {
        if(!groupUserMap.containsKey(group)){
            throw new RuntimeException("Group does not exist");
        }else{
            for(User user : groupUserMap.get(group)){
                if(user.equals(sender)){
                    List<Message> messageList= groupMessageMap.get(group);
                    messageList.add(message);
                    senderMap.put(message,sender);
                    return  messageList.size();
                }
            }
            throw new RuntimeException("You are not allowed to send message");
        }
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception{
        //Change the admin of the group to "user".
        //Throw "Group does not exist" if the mentioned group does not exist
        //Throw "Approver does not have rights" if the approver is not the current admin of the group
        //Throw "User is not a participant" if the user is not a part of the group

        if(groupUserMap.containsKey(group)){
            if(adminMap.get(group).equals(approver)){
                if(groupUserMap.get(group).contains(user)){
                    adminMap.put(group, user);
                }
                else{
                    throw new Exception("User is not a participant");
                }
            }
            else{
                throw new Exception("Approver does not have rights");
            }
        }
        else{
            throw new Exception("Group does not exist");
        }
        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception{
        //If user is not found in any group, throw "User not found" exception
        //If user is found in a group and it is the admin, throw "Cannot remove admin" exception
        //If user is not the admin, remove the user from the group, remove all its messages from all the databases, and update relevant attributes accordingly.

        boolean userFound = false;
        int result = 0;
        Group ansGroup = null;
        for (Group group : groupUserMap.keySet()) {
            List<User> userList = groupUserMap.get(group);
            if (userList.contains(user)) {

                if (adminMap.containsValue(user)) {
                    throw new Exception("Cannot remove admin");
                }
                userFound = true;
                ansGroup = group;
                break;
            }
        }
            if(userFound == false) {
                throw new Exception("User not found");
            }else
            {
                List<User> updatedUsers = new ArrayList<>();
                for(User u : groupUserMap.get(ansGroup)){
                    if(u.equals(user)) continue;
                    updatedUsers.add(u);
                }
                groupUserMap.put(ansGroup, updatedUsers);

                //groupmessageMap

                List<Message> updatedMessages = new ArrayList<>();
                for(Message m : groupMessageMap.get(ansGroup)){
                   if(senderMap.get(m).equals(user)){
                       continue;
                   }
                   updatedMessages.add(m);
                }
                groupMessageMap.put(ansGroup, updatedMessages);

                //sendermap

                HashMap<Message,User> updatedSenderMap = new HashMap<>();
                for(Message message : senderMap.keySet()){
                    if(senderMap.get(message).equals(user)){
                        continue;
                    }
                    updatedSenderMap.put(message, senderMap.get(message));
                }
                senderMap = updatedSenderMap;

                result = updatedUsers.size() + updatedMessages.size() + senderMap.size();


            }
            return result;

    }

//    public String findMessage(Date start, Date end, int K) throws Exception {
//        List<Message> ml = new ArrayList<>();
//        for (Group gl : groupUserMap.keySet())
//        {
//            ml = groupMessageMap.get(gl);
//        }
//        List<Message> filterMessage = new ArrayList<>();
//        for (Message message : ml){
//            if (message.getTimestamp().after(start) && message.getTimestamp().before(end)){
//                filterMessage.add(message);
//            }
//        }
//        if (filterMessage.size() < K) throw new Exception("K is greater than the number of messages");
//
//        Collections.sort(filterMessage,(o1,o2) -> o2.getContent().compareTo(o1.getContent()));
//
//        return filterMessage.get(K-1).getContent();
//    }
}
