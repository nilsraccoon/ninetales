package ws.mia.ninetales;

import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class EnvironmentService {

	public String getMongoUri() {
		return Objects.requireNonNull(System.getenv("MONGO_URI"));
	}

	public String getMongoUsersCollectionName() {
		String env = System.getenv("MONGO_USERS_COLLECTION_NAME");
		 return env != null	? env : "users";
	}

	public String getHypixelAPIKey() {
		return Objects.requireNonNull(System.getenv("HYPIXEL_API_KEY"));
	}

	public String getHypixelGuildId() {
		return Objects.requireNonNull(System.getenv("HYPIXEL_GUILD_ID"));
	}

	public String getDiscordBotToken() {
		return Objects.requireNonNull(System.getenv("DISCORD_BOT_TOKEN"));
	}

	public String getDiscordGuildId() {
		return Objects.requireNonNull(System.getenv("DISCORD_GUILD_ID"));
	}

	public String getGuildApplicationsCategoryId() {
		return Objects.requireNonNull(System.getenv("GUILD_APPLICATIONS_CATEGORY_ID"));
	}

	public String getDiscordApplicationsCategoryId() {
		return Objects.requireNonNull(System.getenv("DISCORD_APPLICATIONS_CATEGORY_ID"));
	}

	public String getQuestionsCategoryId() {
		return Objects.requireNonNull(System.getenv("QUESTIONS_CATEGORY_ID"));
	}

	@Nullable
	public String getDiscordApplicationsArchiveForum() {
		return System.getenv("DISCORD_APPLICATIONS_ARCHIVE_FORUM_ID");
	}

	@Nullable
	public String getGuildApplicationsArchiveForum() {
		return System.getenv("GUILD_APPLICATIONS_ARCHIVE_FORUM_ID");
	}

	public String getGuildMemberRoleId() {
		return Objects.requireNonNull(System.getenv("GUILD_MEMBER_ROLE_ID"));
	}

	public String getTailRoleId() {
		return Objects.requireNonNull(System.getenv("TAIL_ROLE_ID"));
	}

	public String getVulpixRoleId() {
		return Objects.requireNonNull(System.getenv("VULPIX_ROLE_ID"));
	}

	public String getEggRoleId() {
		return Objects.requireNonNull(System.getenv("EGG_ROLE_ID"));
	}

	public String getVisitorRoleId() {
		return Objects.requireNonNull(System.getenv("VISITOR_ROLE_ID"));
	}

	public String getLinkChannelId() {
		return Objects.requireNonNull(System.getenv("LINK_CHANNEL_ID"));
	}

	public String getDiscordJoinMessageChannelId() {
		return Objects.requireNonNull(System.getenv("DISCORD_JOIN_MESSAGE_CHANNEL_ID"));
	}

	@Nullable
	public String getGuildJoinMessageChannelId() {
		return System.getenv("GUILD_JOIN_MESSAGE_CHANNEL_ID");
	}

	@Nullable
	public String getLogChannelId() {
		return System.getenv("LOG_CHANNEL_ID");
	}

	@Nullable
	public String getDebugLogChannelId() {
		return System.getenv("DEBUG_LOG_CHANNEL_ID");
	}

	@Nullable
	public String getServerBoostMessageChannelId() {
		return System.getenv("SERVER_BOOST_MESSAGE_CHANNEL_ID");
	}

}
