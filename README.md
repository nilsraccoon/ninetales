# Ninetales Discord Bot
A spring application for the discord bot managing **Ninetales** Hypixel guild member verification, role synchronization, and application processing.

**Guild Thread:** [✧ Ninetales ✧](https://hypixel.net/threads/%E2%9C%84-ninetales-%E2%9C%84-tale-%E2%9C%84-a-little-corner-of-the-internet-%E2%9C%84-level-56-%E2%9C%84.5966032/)

---

## Environment Variables

| Variable                           | Required | Default | Description                                   |
|------------------------------------|----------|---------|-----------------------------------------------|
| `MONGO_URI`                        | ✅        | -       | MongoDB connection URI                        |
| `MONGO_USERS_COLLECTION_NAME`      | ❌        | `users` | MongoDB collection name for user data         |
| `HYPIXEL_API_KEY`                  | ✅        | -       | Hypixel API key for guild/player data         |
| `HYPIXEL_GUILD_ID`                 | ✅        | -       | Hypixel guild ID to monitor                   |
| `DISCORD_BOT_TOKEN`                | ✅        | -       | Discord bot token                             |
| `DISCORD_GUILD_ID`                 | ✅        | -       | Discord server/guild ID                       |
| `GUILD_APPLICATIONS_CATEGORY_ID`   | ✅        | -       | Category ID for Guild application channels    |
| `DISCORD_APPLICATIONS_CATEGORY_ID` | ✅        | -       | Category ID for Discord application channels  |
| `QUESTIONS_CATEGORY_ID`            | ✅        | -       | Category ID for question channels             |
| `GUILD_MEMBER_ROLE_ID`             | ✅        | -       | Role ID for Guild Members                     |
| `TAIL_ROLE_ID`                     | ✅        | -       | Role ID for Tail rank                         |
| `VULPIX_ROLE_ID`                   | ✅        | -       | Role ID for Vulpix rank                       |
| `EGG_ROLE_ID`                      | ✅        | -       | Role ID for Egg rank                          |
| `VISITOR_ROLE_ID`                  | ✅        | -       | Role ID for visitors (non-guild members)      |
| `LINK_CHANNEL_ID`                  | ✅        | -       | Channel ID where `/link` command can be used  |
| `DISCORD_JOIN_MESSAGE_CHANNEL_ID`  | ✅        | -       | Channel ID for Discord join messages          |
| `GUILD_JOIN_MESSAGE_CHANNEL_ID`    | ❌        | -       | Channel ID for guild join messages (optional) |

---

## Overview
### Automatic Role Synchronization
- Syncs Hypixel guild ranks to Discord roles.
- Automatically assigns roles when members link their accounts
- Manages guild member roles (Egg, Vulpix, Tail, Guild Member) and Visitor role
- Does not interfere with other roles

### Member Management
- Discord ↔ Minecraft account linking with Hypixel verification
- Automatic data cleanup when members leave/are kicked from Discord
- Prevents duplicate account linking

### Account Linking
1. User runs `/link <minecraft username>` in the designated link channel
2. Bot verifies the Discord username is linked on the user's Hypixel profile
3. If verified, creates a database record linking Discord ID ↔ Minecraft UUID
4. Automatically syncs guild roles if the user is a guild member

### Application Workflow
1. User clicks "Apply" button on welcome message
2. Bot creates a private application channel
3. Staff review and use `/accept-app` or `/deny-app`
4. For accepted apps: Tail invites to guild, then when the player has joined, run `/close-accepted-app`
5. Channel is automatically deleted after closure

### Caching
- **Hypixel API**: 5-minute cache for Discord usernames and guild ranks
- **Mojang API**: 30-minute cache for username ↔ UUID lookups
- Cache automatically invalidates on username changes
- Failed API calls are not cached

---

## Commands
### Admin Commands
- `/force-link <discord user> <minecraft username>` - Manually link a Discord user to a Minecraft account
- `/force-role-sync` - Force an immediate role sync from Hypixel API
- `/nt-say <message>` - Send a message as the bot
- `/paste-welcome-message` - Create the welcome message with apply/question buttons
- `/delete-user-data <discord user>` - Delete a user's database record

### Tail Commands
- `/accept-app <message>` - Accept an application with an optional message
- `/close-accepted-app` - Close an accepted application channel after member has joined guild
- `/deny-app <reason>` - Deny an application with an optional reason and close the channel
- `/close-question` - Close a question channel

### Public Commands
- `/link <minecraft username>` - Link your Discord account to your Minecraft account 

---

## Database Schema (MongoDB)
### Users Collection
```javascript
{
  discordId: Long,
  minecraftUuid: String,
  discordApplicationChannelId: Long,
  guildApplicationChannelId: Long,
  questionChannelId: Long,
  awaitingHypixelInvite: Boolean,
  discordMember: Boolean
}
```