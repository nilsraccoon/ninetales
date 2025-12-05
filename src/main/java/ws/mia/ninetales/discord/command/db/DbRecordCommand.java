package ws.mia.ninetales.discord.command.db;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.discord.misc.DiscordLogService;
import ws.mia.ninetales.discord.command.SlashCommand;
import ws.mia.ninetales.mongo.MongoUserService;
import ws.mia.ninetales.mongo.NinetalesUser;

@Component
public class DbRecordCommand extends SlashCommand {
	private static final String COMMAND = "db-record";
	private final MongoUserService mongoUserService;
	private final DiscordLogService discordLogService;

	public DbRecordCommand(MongoUserService mongoUserService, @Lazy DiscordLogService discordLogService) {
		super();
		this.mongoUserService = mongoUserService;
		this.discordLogService = discordLogService;
	}

	@Override
	public CommandData getCommand() {
		return Commands.slash(COMMAND, "View the database record of a user")
				.addOption(OptionType.USER, "user", "Discord User", true)
				.setDefaultPermissions(DefaultMemberPermissions.DISABLED);
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {
		OptionMapping opt = event.getOption("user");
		if(opt == null) return;
		User user = opt.getAsUser();

		NinetalesUser ntUser = mongoUserService.getUser(user.getIdLong());
		if(ntUser == null) {
			String reply = "Could not find user record for <@%s>.".formatted(user.getId());
			event.reply(reply).setEphemeral(true).queue();
			discordLogService.debug(event, reply);
			return;
		}

		String reply = "Mongo record for <@%s>:\n```json\n%s\n```".formatted(user.getId(), ntUser.toJsonString());
		event.reply(reply).setEphemeral(true).queue();
		discordLogService.debug(event, reply);
	}
}

