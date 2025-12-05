package ws.mia.ninetales.discord.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.EnvironmentService;
import ws.mia.ninetales.discord.misc.DiscordLogService;

import java.util.List;

@Component
public class NtReactCommand extends SlashCommand {
	private static final String COMMAND = "nt-react";
	private final EnvironmentService environmentService;
	private final DiscordLogService discordLogService;

	public NtReactCommand(EnvironmentService environmentService, @Lazy DiscordLogService discordLogService) {
		super();
		this.environmentService = environmentService;
		this.discordLogService = discordLogService;
	}

	@Override
	public CommandData getCommand() {
		return Commands.slash(COMMAND, ":3")
				.addOption(OptionType.STRING, "message", "message ID", true)
				.addOption(OptionType.STRING, "emoji", "the emoji", true)
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VIEW_AUDIT_LOGS));
	}

	@Override
	public List<String> roles() {
		return List.of(environmentService.getTailRoleId());
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {
		OptionMapping msgOpt = event.getOption("message");
		OptionMapping emojiOpt = event.getOption("emoji");

		if (msgOpt == null || emojiOpt == null) return;

		String messageId = msgOpt.getAsString();
		String emojiStr = emojiOpt.getAsString();

		event.deferReply(true).queue(hook -> {
			event.getChannel().retrieveMessageById(messageId).queue(
					message -> {
						discordLogService.debug(event, message);
						message.addReaction(Emoji.fromFormatted(emojiStr)).queue(
								success -> hook.deleteOriginal().queue(),
								error -> hook.editOriginal("failed to add reaction `[" + error.getMessage() + "]`").queue()
						);
					},
					error -> hook.editOriginal("failed to find message `[" + error.getMessage() + "]`").queue()
			);
		});
	}

}
