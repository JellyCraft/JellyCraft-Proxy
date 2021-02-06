package net.md_5.bungee.rcon;

import java.util.ArrayList;
import java.util.Collection;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.CommandSender;

public class RconCommandSender implements CommandSender
{
    private final StringBuffer buffer;
    private final ProxyServer server;
    
    public RconCommandSender(final ProxyServer server) {
        this.buffer = new StringBuffer();
        this.server = server;
    }
    
    public ProxyServer getServer() {
        return this.server;
    }
    
    public String flush() {
        final String result = this.buffer.toString();
        this.buffer.setLength(0);
        return result;
    }
    
    public String getName() {
        return "RCON";
    }
    
    public void sendMessage(final String message) {
        this.buffer.append(message).append("\n");
    }
    
    public void sendMessages(final String... messages) {
        for (final String line : messages) {
            this.sendMessage(line);
        }
    }
    
    public void sendMessage(final BaseComponent... message) {
        for (final BaseComponent line : message) {
            this.sendMessage(line);
        }
    }
    
    public void sendMessage(final BaseComponent message) {
        this.sendMessage(message.toLegacyText());
    }
    
    public Collection<String> getGroups() {
        return new ArrayList<String>();
    }
    
    public void addGroups(final String... groups) {
    }
    
    public void removeGroups(final String... groups) {
    }
    
    public boolean hasPermission(final String permission) {
        return true;
    }
    
    public void setPermission(final String permission, final boolean value) {
    }
    
    public Collection<String> getPermissions() {
        return new ArrayList<String>();
    }
}
