package ws.mia.ninetales.discord.command;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.discord.DiscordLogService;
import ws.mia.ninetales.mongo.MongoUserService;

@Component
public class DeleteUserDataCommand extends SlashCommand {
	private static final String COMMAND = "delete-user-data";
	private final MongoUserService mongoUserService;
	private final DiscordLogService discordLogService;

	public DeleteUserDataCommand(MongoUserService mongoUserService, @Lazy DiscordLogService discordLogService) {
		super();
		this.mongoUserService = mongoUserService;
		this.discordLogService = discordLogService;
	}

	@Override
	public CommandData getCommand() {
		return Commands.slash(COMMAND, "Delete Mongo data for a user. This will not modify any discord roles.")
				.addOption(OptionType.USER, "user", "Discord User", true)
				.setDefaultPermissions(DefaultMemberPermissions.DISABLED);
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {
		OptionMapping opt = event.getOption("user");
		if(opt == null) return;

		User user = opt.getAsUser();

		if(mongoUserService.deleteUser(user.getIdLong())) {
			String reply = "Successfully wiped <@" + user.getId() + ">";
			event.reply(reply).setEphemeral(true).queue();
			discordLogService.debug(event, reply);
		} else {
			String reply = "Failed to wipe <@" + user.getId() + ">";
			event.reply(reply).setEphemeral(true).queue();
			discordLogService.debug(event, reply);
		}


	}
}
