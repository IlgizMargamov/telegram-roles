import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class RoleMasterBot extends TelegramLongPollingBot {
    private final ArrayList<NamedArrayList<String>> roles = new ArrayList<>();
    private final String botUsernameWithOffsetForFollowingCommand = "@RolemASSter_bot ";
    private static Boolean awaitingReplyWithRoleName= false;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();
            Long chatId = update.getMessage().getChatId();
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

                };
                roles.add(new NamedArrayList<String>(roleName));
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
            if (messageText.startsWith(botUsernameWithOffsetForFollowingCommand)){
                String[] splitMessage = messageText.split(" ");
                String command = splitMessage[1];

                if (command.equals("help")){
                    SendMessage sendMessage = new SendMessage();
                    sendMessage.setChatId(chatId);
                    sendMessage.setText("This bot helps you tag people of certain role\n" +
                            "To add a role, send:\n" +
                            "@RolemASSter_bot new_role <ROLE_NAME>\n" +
                            "No restrictions implied\n\n" +
                            "To show existing roles, send:\n" +
                            "@RolemASSter_bot show_roles\n\n" +
                            "To add users to a role, send:\n" +
                            "@RolemASSter_bot add_users <ROLE_NAME> [USERNAME1 USERNAME2..]\n" +
                            "You might add any number of users to role.\n" +
                            "Username format: @username\n" +
                            "Also you might send:\n" +
                            "@RolemASSter_bot add_users +<ROLE_NAME> [USERNAME1 USERNAME2..]\n" +
                            "+ will be omitted in rolename\n\n" +
                            "To tag group of users by role, send:\n" +
                            "@RolemASSter_bot tag_users <ROLE_NAME>\n" +
                            "");
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

                        };
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
                                int keyWords = 3;
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
        try {
            return Files.readString(Path.of(System.getProperty("user.dir") + "\\token.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
