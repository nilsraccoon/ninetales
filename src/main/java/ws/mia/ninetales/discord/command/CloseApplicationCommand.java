package ws.mia.ninetales.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.discord.ApplicationService;

@Component
public class CloseApplicationCommand extends SlashCommand {
	private static final String COMMAND = "close-accepted-app";

	private final ApplicationService applicationService;

	public CloseApplicationCommand(ApplicationService applicationService) {
		super();

		this.applicationService = applicationService;
	}

	@Override
	public CommandData getCommand() {
		return Commands.slash(COMMAND, "Close an application once the player has joined.");
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {
		applicationService.closeAcceptedApplication(event);
	}
}
