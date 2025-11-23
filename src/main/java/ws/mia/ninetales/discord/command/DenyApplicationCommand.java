package ws.mia.ninetales.discord.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.discord.ApplicationService;

@Component
public class DenyApplicationCommand extends SlashCommand {
	private static final String COMMAND = "deny-app";
	private final ApplicationService applicationService;

	public DenyApplicationCommand(ApplicationService applicationService) {
		super();
		this.applicationService = applicationService;
	}

	@Override
	public CommandData getCommand() {
		return Commands.slash(COMMAND, "Deny an application in this channel")
				.addOption(OptionType.STRING, "reason", "Deny reason", true)
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VIEW_AUDIT_LOGS));
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {
		applicationService.denyApplication(event);
	}
}
