# MineManiaChat
A chat plugin for velocity.

---

## Commands

- /chatenable | Permission: "chat.disable" | Enables chat globally for all players
- /chatdisable | Permission: "chat.disable"| Disables chat globally for all players without "chat.joinmessage.disable"
- /mmchatbannedwords \<add|check|remove\> \<word\> | Permission: "chat.bannedwords" | Edit or check the banned words list
- /mmchatreload | Permission: "chat.reload" | Reloads all configs 
- /clearchat or /cc | Permission: "chat.clear" | Clears the chat history
- /joinmessagesend or /jmsend | Permission: "chat.joinmessage.fakesend" | Send a join message for yourself (For users with chat.joinmessage.disable)
- /message or /msg \<Player\> \<Message\> | Permission: "chat.private-message.allow" | Send private messages to other users    
- /enablepm or /unmutepm \<Player|global\> | Permission: "chat.private-message.mute" | Enable private messages for a user or globally 
- /disablepm or /mutepm \<Player|global\> | Permission: "chat.private-message.mute" | Disable private messages for a user or globally
- /togglespy or /spy | Permission: "chat.private-message.spy" | Toggles spy which allows the user to see other players private messages  
- /servermessage or /servermsg or /smsg \<Player\> \<Message\> | Permission: "chat.server-message.send" | Send a private server message to a player
- /broadcast \<message\> | Permission: "chat.broadcast" | Broadcast a message network wide
- /broadcastserver \<server\> \<message\> | Permission: "chat.broadcast" | Broadcast a message to a specific backend server 
- /togglechatalerts or /chatalerts |  Permission: "chat.notify" | Enable or disable receiving chat filter alerts
- /chat or /c or /talk \<Message\> | Permission: None | Triggers a chat event with a message this allows a workaround for Mojang's age verification
- /mmchatspamcooldown \<check|reset\> \<player\> | Permisson: "chat.manage.spamcooldown" | Allows staff to check or remove a players spam cooldown
- /mmchatdiscord \<enable|disable\> | Permission "chat.manage.discord" | Allows staff to enable and disable the discord bridge
- /mmchatsetdiscordpresence \<playing|streaming|listening|watching|custom|competing|clear\> \<presence text\> | "chat.manage.discordpresence" | Allows user to set the discord bots presence
- /discordlink or /link | Allows a user to link a discord account to their Minecraft account
- /discordunlink or /unlink | Allows a user to remove an existing link between a discord account and their Minecraft account
- /discordadminunlink or /aunlink \<player\> | Permission "chat.manage.discord" | Allows staff to remove a link between a discord and a Minecraft account

## Permissions

- chat.bypass.disable | Allows a user to bypass a global chat disablement
- chat.bypass.filter.url | Allows a user to bypass the URL filter to send URLs in chat
- chat.bypass.filter.banned-words | Allows a user to bypass the Banned words filter to send banned words in chat
- chat.bypass.filter.spam | Allows a user to bypass the spam filter
- chat.bypass.private-message.disablement | Allows a user to send private messages even when they have been disabled with /disablepm (Global or individual)
- chat.format | Allows a user to use the & chat formatting
- chat.server-message.alert | Any user with this permission will receive a message when another user is sent a server message. 
- chat.joinmessage.disable | Any user with this permission will have their global join message hidden.
- chat.joinmessage.alert | Any user with this permission will see hidden join messages from users with "chat.joinmessage.disable"
- chat.notify | Any user with this permission will be alerted when a chat filter is triggered (If they have them enabled see command /togglechatalerts)

# Post 3.0.0 Warnings

- Proxy and Backend Servers (Any server in velocity.toml) MUST have [SignedVelocity](https://modrinth.com/plugin/signedvelocity) plugin installed
- MineMania API is no longer used meaning Kerb no longer need to be configured
- Config and Banned words are now in separate YAMLs
