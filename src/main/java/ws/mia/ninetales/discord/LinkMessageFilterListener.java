package ws.mia.ninetales.discord;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.EnvironmentService;

@Component
public class LinkMessageFilterListener extends ListenerAdapter {

	private final EnvironmentService environmentService;
	private final DiscordLogService discordLogService;

	public LinkMessageFilterListener(EnvironmentService environmentService, @Lazy DiscordLogService discordLogService) {
		this.environmentService = environmentService;
		this.discordLogService = discordLogService;
	}

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		if(!event.getChannel().getId().equals(environmentService.getLinkChannelId())) {
			return;
		}

		if(event.getAuthor().isBot()) {
			return;
		}

		if(event.getMember().getUnsortedRoles().stream().noneMatch(r -> r.getId().equals(environmentService.getTailRoleId()))) {
			event.getMessage().delete().queue(null, error -> {});
			discordLogService.debug("Deleted message in link channel", "by <@%s>:\n```%s```".formatted(event.getAuthor().getId(), event.getMessage().getContentRaw()));
			return;
		}

		super.onMessageReceived(event);
	}
}
