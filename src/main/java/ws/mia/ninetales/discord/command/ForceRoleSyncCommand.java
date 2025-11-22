package ws.mia.ninetales.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.discord.GuildRankService;

@Component
public class ForceRoleSyncCommand extends SlashCommand{

	private static final String COMMAND = "force-role-sync";
	private final GuildRankService guildRankService;

	public ForceRoleSyncCommand(@Lazy GuildRankService guildRankService) {
		super();
		this.guildRankService = guildRankService;
	}

	@Override
	public CommandData getCommand() {
		return Commands.slash(COMMAND, ":3")
				.setDefaultPermissions(DefaultMemberPermissions.DISABLED);
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {
		guildRankService.syncRoles();
		event.reply("synced roles owo").setEphemeral(true).queue();
	}
}
