package ws.mia.ninetales.discord.misc;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.EnvironmentService;
import ws.mia.ninetales.mongo.MongoUserService;
import ws.mia.ninetales.mongo.NinetalesUser;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Component
public class QuestionService {

	private final MongoUserService mongoUserService;
	private final DiscordUtilService discordUtilService;
	private final EnvironmentService environmentService;
	private final DiscordLogService discordLogService;

	public QuestionService(MongoUserService mongoUserService, DiscordUtilService discordUtilService, EnvironmentService environmentService, @Lazy DiscordLogService discordLogService) {
		this.mongoUserService = mongoUserService;
		this.discordUtilService = discordUtilService;
		this.environmentService = environmentService;
		this.discordLogService = discordLogService;
	}

	public boolean createQuestionChannel(User user, Guild guild, Consumer<NinetalesUser> hasChannelFailure, BiConsumer<TextChannel, @Nullable NinetalesUser> success) {
		NinetalesUser ntUser = mongoUserService.getUser(user.getIdLong()); // may be null

		if (ntUser != null && ntUser.getQuestionChannelId() != null) {
			hasChannelFailure.accept(ntUser);
			return false;
		}
	discordUtilService.prepareUserStaffChannel(guild, user, user.getName(), environmentService.getQuestionsCategoryId())
				.queue(tc -> {
					mongoUserService.setQuestionChannelId(user.getIdLong(), tc.getIdLong());
					success.accept(tc, ntUser);

					discordLogService.debug("Created Question application channel", "For <@%s> at <#%s>"
							.formatted(user.getId(), tc.getId()));
				});
		return true;
	}

}
