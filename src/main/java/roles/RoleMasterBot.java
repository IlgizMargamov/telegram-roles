package roles;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoleMasterBot extends TelegramLongPollingBot {
    private final ArrayList<NamedArrayList<String>> roles = new ArrayList<>();
    private final Map<Long, List<String>> chatAll = new HashMap<>();
    private static Boolean awaitingReplyWithRoleName= false;
    private static final String all ="@all";

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
            if (update.getMessage().hasText()){
                if (update.getMessage().getText().contains(all)){
                    var allUsers = chatAll.get(update.getMessage().getChatId());
                    var all = new StringBuilder();
                    for (var i : allUsers){
                        all.append(i+" ");
                    }

                    SendMessage sendMessage = SendMessage.builder().chatId(chatId).text(all.toString()).build();
                    try {
                        execute(sendMessage);
                    }
                    catch (TelegramApiException e){
                        e.printStackTrace();
                    }
                }
            }
            if (update.getMessage().isReply() && awaitingReplyWithRoleName){
                awaitingReplyWithRoleName = false;
                String roleName = update.getMessage().getText().split(" ")[0];
                if (checkIfRoleAlreadyExists(roleName)) {
                    SendMessage sendMessage= SendMessage
                            .builder()
                            .chatId(chatId)
                            .text("Role "+roleName+" already exists")
                            .build();
                    try {
                        execute(sendMessage);
                    }
                    catch (TelegramApiException e){
                        e.printStackTrace();
                    }

                }
                roles.add(new NamedArrayList<>(roleName));
                SendMessage sendMessage= SendMessage
                        .builder()
                        .chatId(chatId)
                        .text("Role "+roleName+" has been created")
                        .build();
                try {
                    execute(sendMessage);
                }
                catch (TelegramApiException e){
                    e.printStackTrace();
                }
            }
            String botUsernameWithOffsetForFollowingCommand = "@RolemASSter_bot ";
            if (messageText.startsWith(botUsernameWithOffsetForFollowingCommand)){
                String[] splitMessage = messageText.split(" ");
                String command = splitMessage[1];

                if (command.equals("hi")){
                    if (chatAll.get(chatId) == null){
                        chatAll.putIfAbsent(chatId, new ArrayList<>());
                    }

                    String userName = update.getMessage().getFrom().getUserName();
                    if(!chatAll.get(chatId).contains("@"+userName)){
                        chatAll.get(chatId).add("@"+userName);
                    }

                    SendMessage sendMessage = SendMessage.builder().chatId(chatId).text("@"+userName+" hi!").build();
                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }

                if (command.equals("help")){
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(chatId);
                    sendMessage.setText("""
                            This bot helps you tag people of certain role
                            To add a role, send:
                            @RolemASSter_bot new_role <ROLE_NAME>
                            No restrictions implied

                            To show existing roles, send:
                            @RolemASSter_bot show_roles

                            To add users to a role, send:
                            @RolemASSter_bot add_users <ROLE_NAME> [USERNAME1 USERNAME2..]
                            You might add any number of users to role.
                            Username format: @username
                            Also you might send:
                            @RolemASSter_bot add_users +<ROLE_NAME> [USERNAME1 USERNAME2..]
                            + will be omitted in rolename

                            To tag group of users by role, send:
                            @RolemASSter_bot tag_users <ROLE_NAME>
                            """);
                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
                if (command.equals("show_roles")){
                    SendMessage sendMessage = new SendMessage();
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Currently exist such roles:\n");
                    stringBuilder.append("{\n");
                    for(NamedArrayList<String> namedArrayList : roles){
                        stringBuilder.append("\t");
                        stringBuilder.append(namedArrayList.getName());
                        stringBuilder.append("\n");
                    }
                    stringBuilder.append("}");
                    sendMessage.setText(stringBuilder.toString());
                    sendMessage.setChatId(chatId);
                    try {
                        execute(sendMessage);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }

                if (splitMessage.length>2){
                    String roleName = splitMessage[2];
                    if (command.equals("new_role")){
                        if (checkIfRoleAlreadyExists(roleName)) {
                            SendMessage sendMessage= SendMessage
                                    .builder()
                                    .chatId(chatId)
                                    .text("Role "+roleName+" already exists")
                                    .build();
                            try {
                                execute(sendMessage);
                            }
                            catch (TelegramApiException e){
                                e.printStackTrace();
                            }

                        }
                        roles.add(new NamedArrayList<>(roleName));
                        SendMessage sendMessage= SendMessage
                                .builder()
                                .chatId(chatId)
                                .text("Role "+roleName+" has been created")
                                .build();
                        try {
                            execute(sendMessage);
                        }
                        catch (TelegramApiException e){
                            e.printStackTrace();
                        }
                        return;
                    }
                    if (roleName.startsWith("+")){
                        roleName= roleName.replace('+',' ').trim();
                        roles.add(new NamedArrayList<>(roleName));
                    }
                        if (checkIfRoleAlreadyExists(roleName)) {
                            String copyRoleName =roleName;
                            NamedArrayList<String> currentRole = roles.stream().filter(x -> x.getName().equals(copyRoleName)).findFirst().get();
                            if (command.equals("tag_users")) {
                                SendMessage sendMessage = new SendMessage();
                                sendMessage.setChatId(chatId);
                                StringBuilder stringBuilder = new StringBuilder();
                                for (String user : currentRole) {
                                    stringBuilder.append(user);
                                    stringBuilder.append(" ");
                                }
                                sendMessage.setText(stringBuilder.toString());
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                            }

                            if (command.equals("add_users")) {
                                int keyWords = 3;
                                for (String user : splitMessage) {
                                    if (keyWords > 0) {
                                        keyWords--;
                                        continue;
                                    }
                                    currentRole.add(user);
                                }
                                SendMessage sendMessage = SendMessage.builder().chatId(chatId).text("Users have been added to role " + roleName).build();
                                try {
                                    execute(sendMessage);
                                } catch (TelegramApiException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else {
                            SendMessage sendMessage = new SendMessage();
                            sendMessage.setChatId(chatId);
                            sendMessage.setText("Role " + roleName + " doesn't exist");
                            try {
                                execute(sendMessage);
                            } catch (TelegramApiException e) {
                                e.printStackTrace();
                            }
                        }
                }
                else if (command.equals("new_role")){
                    SendMessage sendMessage = SendMessage
                            .builder()
                            .replyToMessageId(update.getMessage().getMessageId()).chatId(chatId)
                            .text("What is role name you wish to create?")
                            .build();
                    try {
                        execute(sendMessage);
                        awaitingReplyWithRoleName = true;
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private boolean checkIfRoleAlreadyExists(String roleName) {
        for(NamedArrayList<String> namedArrayList : roles){
            if (namedArrayList.getName().equals(roleName)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void clearWebhook() throws TelegramApiRequestException {

    }

    @Override
    public String getBotUsername() {
        return getClass().getName();
    }

    @Override
    public String getBotToken() {
        return System.getenv("BOT_TOKEN");
    }
}
