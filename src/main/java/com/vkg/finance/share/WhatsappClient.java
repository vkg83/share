package com.vkg.finance.share;

import it.auties.whatsapp.api.*;
import it.auties.whatsapp.model.chat.Chat;
import it.auties.whatsapp.model.mobile.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class WhatsappClient implements Listener {
    private static final Logger LOGGER = LoggerFactory.getLogger(WhatsappClient.class);
    Whatsapp whatsapp;

    public void init() throws ExecutionException, InterruptedException {
        System.out.println("Initiating Connection");
        var builder = Whatsapp.webBuilder().newConnection(PhoneNumber.ofNullable(919766045435L));
        builder.historySetting(WebHistorySetting.standard(true));
        builder.errorHandler(((whatsapp, location, th) -> {
            LOGGER.error("Error in whatsapp: {}", th.getMessage());
            return ErrorHandler.Result.RECONNECT;
        }));

        var future = builder.unregistered(QrHandler.toTerminal())
                .addListener(this).connect();
        whatsapp = future.get();
    }

    public void send(String to, String message) throws ExecutionException, InterruptedException {
        var store = whatsapp.store();
        var opChat = store.findChatByName(to);
        if(opChat.isEmpty()) {
            LOGGER.warn("Chat '{}' not found", to);
            return;
        }
        var f = whatsapp.sendChatMessage(opChat.get(), message);
        var v = f.get();
        LOGGER.info("Connected? {}, Sent {}", whatsapp.isConnected(), v.message().unbox().toString());
    }

    @Override
    public void onLoggedIn() {
        LOGGER.info("Logged in");
    }

    @Override
    public void onChats(Collection<Chat> chats) {
        LOGGER.info("Total {} chats loaded", chats.size());
    }

    @Override
    public void onDisconnected(DisconnectReason reason) {
        LOGGER.warn("Disconnected due to {}", reason);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        var c = new WhatsappClient();
        c.init();
        var s = new Scanner(System.in);
        String msg = "";
        while(!msg.equals("q")) {
            var to = s.nextLine();
            msg = s.nextLine();
            c.send(to, msg);
        }
    }
}
