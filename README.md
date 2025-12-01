# Ninetales Discord Bot
A Spring application for the Discord bot managing **Ninetales** community member verification, role synchronization, and application processing.

**Hypixel Guild Thread:** [✧ Ninetales ✧](https://hypixel.net/threads/5966032/)

---

## Overview
### Automatic Role Synchronization
- Syncs Hypixel guild ranks to Discord roles periodically and on link.
- Manages guild member roles (Egg, Vulpix, Tail, Guild Member) and Visitor role
- Does not interfere with other roles

### Account Linking
1. User runs `/link <minecraft username>` in the designated link channel
2. Bot verifies the Discord username is linked on the user's Hypixel profile
3. If verified, creates a database record linking Discord ID ↔ Minecraft UUID
4. Automatically syncs guild roles if the user is a guild member

### Application Workflow
1. User clicks "Apply" button on welcome message
2. Bot creates a private application channels and walks user through questions
3. Tails review in their own private channel and use `/accept-app` or send a denial message and use `/close-app`
4. For accepted apps: Tail invites to guild, then when the player has joined, run `/close-app`
5. Channels are automatically deleted after closure

### Question Workflow
1. User clicks "Ask a Question" button on welcome message
2. Bot creates a private question channel for user to ask
3. Tails answer question, user acknowledges that the answer was satisfactory
4. Tails use `/close-question` to close the question and delete the channel

### Misc. Member Management
- Automatic record deletion when members leave/are kicked from Discord

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
- `/paste-welcome-message` - Create the welcome message with apply/question buttons
- `/delete-user-data <discord user>` - Delete a user's database record
- `/db-record <discord user>` - View a user's database record

### Tail Commands
- `/nt-say <message>` - Send a message as the bot
- `/nt-react <message> <reaction>` - Send a reaction as the bot
- `/accept-app <message>` - Accept an application with an optional message
- `/close-app` - Close an application once the user has joined the guild, or been denied
- `/close-question` - Close a question channel
- `/make-visitor <discord user>` - Manually make a linked user a visitor without an application
- `/status <discord user>` - View link and guild status of a user

### Public Commands
- `/link <minecraft username>` - Link your Discord account to your Minecraft account 

---

## Environment Variables

| Variable                                | Required | Default | Description                                              |
|-----------------------------------------|----------|---------|----------------------------------------------------------|
| `MONGO_URI`                             | ✅        | -       | MongoDB connection URI                                   |
| `MONGO_USERS_COLLECTION_NAME`           | ❌        | `users` | MongoDB collection name for user data                    |
| `HYPIXEL_API_KEY`                       | ✅        | -       | Hypixel API key for guild/player data                    |
| `HYPIXEL_GUILD_ID`                      | ✅        | -       | Hypixel guild ID to monitor                              |
| `DISCORD_BOT_TOKEN`                     | ✅        | -       | Discord bot token                                        |
| `DISCORD_GUILD_ID`                      | ✅        | -       | Discord server/guild ID                                  |
| `GUILD_APPLICATIONS_CATEGORY_ID`        | ✅        | -       | Category ID for Guild application channels               |
| `GUILD_APPLICATIONS_ARCHIVE_FORUM_ID`   | ❌        | -       | Forum channel ID for archiving Guild app channels        |
| `DISCORD_APPLICATIONS_CATEGORY_ID`      | ✅        | -       | Category ID for Discord application channels             |
| `DISCORD_APPLICATIONS_ARCHIVE_FORUM_ID` | ❌        | -       | Forum channel ID for archiving Discord app channels      |
| `QUESTIONS_CATEGORY_ID`                 | ✅        | -       | Category ID for question channels                        |
| `GUILD_MEMBER_ROLE_ID`                  | ✅        | -       | Role ID for Guild Members                                |
| `TAIL_ROLE_ID`                          | ✅        | -       | Role ID for Tail rank                                    |
| `VULPIX_ROLE_ID`                        | ✅        | -       | Role ID for Vulpix rank                                  |
| `EGG_ROLE_ID`                           | ✅        | -       | Role ID for Egg rank                                     |
| `VISITOR_ROLE_ID`                       | ✅        | -       | Role ID for visitors (non-guild members)                 |
| `LINK_CHANNEL_ID`                       | ✅        | -       | Channel ID where `/link` command can be used             |
| `DISCORD_JOIN_MESSAGE_CHANNEL_ID`       | ✅        | -       | Channel ID for Discord join messages                     |
| `GUILD_JOIN_MESSAGE_CHANNEL_ID`         | ❌        | -       | Channel ID for guild join messages (optional)            |
| `LOG_CHANNEL_ID`                        | ❌        | -       | Channel ID for logs of level `INFO` and above (optional) |
| `DEBUG_LOG_CHANNEL_ID`                  | ❌        | -       | Channel ID for logs of level `DEBUG` (optional)          |

---

## Database Schema (MongoDB)
### Users Collection
```javascript
{
  discordId: Long,
  minecraftUuid: String,
  discordApplicationChannelId: Long,
  guildApplicationChannelId: Long,
  tailDiscussionChannelId: Long,
  questionChannelId: Long,
  awaitingHypixelInvite: Boolean,
  discordMember: Boolean,
  guildJoinMessage: Boolean
}
```