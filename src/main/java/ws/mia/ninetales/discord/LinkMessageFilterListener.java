package ws.mia.ninetales.discord;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.EnvironmentService;

@Component
public class LinkMessageFilterListener extends ListenerAdapter {

	private final EnvironmentService environmentService;

	public LinkMessageFilterListener(EnvironmentService environmentService) {
		this.environmentService = environmentService;
	}

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		if(!event.getChannel().getId().equals(environmentService.getLinkChannelId())) {
			return;
		}
		if(!event.getMember().getUnsortedRoles().contains(event.getGuild().getRoleById(environmentService.getTailRoleId()))) {
			event.getMessage().delete().queue();
			return;
		}

		super.onMessageReceived(event);
	}
}
