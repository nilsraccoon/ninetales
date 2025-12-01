package ws.mia.ninetales.discord.command.application;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.EnvironmentService;
import ws.mia.ninetales.discord.ApplicationService;
import ws.mia.ninetales.discord.command.SlashCommand;

import java.util.List;

@Component
public class AcceptApplicationCommand extends SlashCommand {
	private static final String COMMAND = "accept-app";

	private final ApplicationService applicationService;
	private final EnvironmentService environmentService;

	public AcceptApplicationCommand(ApplicationService applicationService, EnvironmentService environmentService) {
		super();

		this.applicationService = applicationService;
		this.environmentService = environmentService;
	}

	@Override
	public CommandData getCommand() {
		return Commands.slash(COMMAND, "Accept an application in this channel. You need to invite the player on Hypixel.")
				.addOption(OptionType.STRING, "message", "Optional message to send the user in DMs", false)
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VIEW_AUDIT_LOGS));

	}

	@Override
	public List<String> roles() {
		return List.of(environmentService.getTailRoleId());
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {
		applicationService.acceptApplication(event);
	}
}
