package com.blogspot.regulargeek.telegrambot.message.parser.command;

import com.blogspot.regulargeek.telegrambot.annotations.SupportedCommand;
import com.blogspot.regulargeek.telegrambot.message.commands.OnCommand;
import com.blogspot.regulargeek.telegrambot.message.parser.SingleCommandParser;
import com.blogspot.regulargeek.telegrambot.exception.CommandParseException;
import com.blogspot.regulargeek.telegrambot.service.OpenHabItemsService;
import model.ItemDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.api.methods.send.SendMessage;

@Service
@SupportedCommand(command = "ON")
public class OnCommandParser implements SingleCommandParser {
    private static final int ITEM_NAME_POSITION = 1;

    @Value("${readOnlyItems}")
    private String readOnlyItems;

    @Autowired
    private OpenHabItemsService openHabItemsService;

    private boolean isReadOnlyItem(String itemName) {
        if (readOnlyItems.equals("")) { return false; }
        if (readOnlyItems.equals("*")) { return true; }
        String[] arrayReadOnlyItems = readOnlyItems.split(",");
        for (String s: arrayReadOnlyItems) {
            if (itemName.equals(s) || itemName.matches(s)) { return true; }
        }
        return false;
    }

    public OnCommand parse(SendMessage message) throws CommandParseException {

        String[] parts = message.getText().split(" ");
        validateCommand(parts);
        String itemName = parts[ITEM_NAME_POSITION];
        OnCommand command = new OnCommand(message, itemName);
        return command;
    }

    private void validateCommand(String[] parts) throws CommandParseException {
        if (parts.length != 2) {
            throw new CommandParseException("<b>Invalid message syntax</b>\nType <b>HELP ON</b> to get proper syntax");
        }
        String itemName = parts[ITEM_NAME_POSITION];

        if (isReadOnlyItem(itemName)) {
            throw new CommandParseException("Item is <b>READ ONLY</b>.");
        }

        ItemDTO dto = openHabItemsService.getItem(itemName);
        if (dto == null) {
            throw new CommandParseException("Item not found. Type <b>ITEMS</b> to get list of known items. If item is not visible and is present in OpenHab type <b>REFRESH</b> to reload items.");
        }

    }

    @Override
    public String getHelpMessage() {
        return "Turns ON specified ITEM.";
    }

    @Override
    public String getUsageMessage() {
        StringBuilder builder = new StringBuilder();
        builder.append("Turns <b>ON</b> specified <b>ITEM</b>.")
                .append("\n\n")
                .append("Usage: ")
                .append("\n")
                .append("<b>ON</b> <i>{item_name}</i>");
        return builder.toString();
    }
}
