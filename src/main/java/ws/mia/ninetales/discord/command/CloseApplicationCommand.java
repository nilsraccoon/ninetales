package ws.mia.ninetales.discord.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
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
		return Commands.slash(COMMAND, "Close an application once the player has joined.")
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VIEW_AUDIT_LOGS));
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {
		applicationService.attemptCloseAcceptedGuildApplication(event);
	}
}
