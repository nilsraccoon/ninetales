package ws.mia.ninetales.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.discord.DiscordLogService;
import ws.mia.ninetales.discord.GuildRankService;

@Component
public class ForceRoleSyncCommand extends SlashCommand{

	private static final String COMMAND = "force-role-sync";
	private final GuildRankService guildRankService;
	private final DiscordLogService discordLogService;

	public ForceRoleSyncCommand(@Lazy GuildRankService guildRankService, @Lazy DiscordLogService discordLogService) {
		super();
		this.guildRankService = guildRankService;
		this.discordLogService = discordLogService;
	}

	@Override
	public CommandData getCommand() {
		return Commands.slash(COMMAND, ":3")
				.setDefaultPermissions(DefaultMemberPermissions.DISABLED);
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {
		guildRankService.syncRoles(true);
		event.reply("synced roles owo").setEphemeral(true).queue();
		discordLogService.debug(event);
	}
}
