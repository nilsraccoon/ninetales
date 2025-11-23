package ws.mia.ninetales.discord.command;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.mongo.MongoUserService;

@Component
public class DeleteUserDataCommand extends SlashCommand {
	private static final String COMMAND = "delete-user-data";
	private final MongoUserService mongoUserService;

	public DeleteUserDataCommand(MongoUserService mongoUserService) {
		super();
		this.mongoUserService = mongoUserService;
	}

	@Override
	public CommandData getCommand() {
		return Commands.slash(COMMAND, "Delete Mongo data for a user. This will not modify any discord roles.")
				.addOption(OptionType.USER, "discord", "Discord User", true)
				.setDefaultPermissions(DefaultMemberPermissions.DISABLED);
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {
		OptionMapping opt = event.getOption("discord");
		if(opt == null ) return;

		User user = opt.getAsUser();

		mongoUserService.deleteUser(user.getIdLong());

		event.reply("Successfully wiped <@" + user.getId() + ">").setEphemeral(true).queue();
	}
}
