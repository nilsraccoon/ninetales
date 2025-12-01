package ws.mia.ninetales.discord.command.application;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.EnvironmentService;
import ws.mia.ninetales.discord.ApplicationService;
import ws.mia.ninetales.discord.command.SlashCommand;

import java.util.List;

@Component
public class CloseApplicationCommand extends SlashCommand {
	private static final String COMMAND = "close-app";
	private final ApplicationService applicationService;
	private final EnvironmentService environmentService;

	public CloseApplicationCommand(ApplicationService applicationService, EnvironmentService environmentService) {
		super();
		this.applicationService = applicationService;
		this.environmentService = environmentService;
	}

	@Override
	public CommandData getCommand() {
		return Commands.slash(COMMAND, "Close application channels without accepting.")
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VIEW_AUDIT_LOGS));
	}

	@Override
	public List<String> roles() {
		return List.of(environmentService.getTailRoleId());
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {
		applicationService.closeApplication(event);
	}
}
