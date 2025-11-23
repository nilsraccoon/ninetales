package ws.mia.ninetales.discord;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class ApplicationProcessListener extends ListenerAdapter {

	private final ApplicationService applicationService;

	public ApplicationProcessListener(ApplicationService applicationService) {
		this.applicationService = applicationService;
	}

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		super.onMessageReceived(event);
		if(event.getChannelType() != ChannelType.TEXT) return;
		if(event.getAuthor().isBot()) return;
		applicationService.attemptSendNextApplicationProcessMessage(event.getChannel().asTextChannel());
	}


}
