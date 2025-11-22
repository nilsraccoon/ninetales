package ws.mia.ninetales.discord.command;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.EnvironmentService;
import ws.mia.ninetales.mongo.MongoUserService;
import ws.mia.ninetales.mongo.NinetalesUser;

import java.util.Objects;

@Component
public class CloseQuestionCommand extends SlashCommand{
	private static final String COMMAND = "close-question";
	private final MongoUserService mongoUserService;
	private final EnvironmentService environmentService;

	public CloseQuestionCommand(MongoUserService mongoUserService, EnvironmentService environmentService) {
		super();
		this.mongoUserService = mongoUserService;
		this.environmentService = environmentService;
	}

	@Override
	public CommandData getCommand() {
		return Commands.slash(COMMAND, "Close a question channel");
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {
		if(Objects.requireNonNull(event.getMember()).getUnsortedRoles().stream().noneMatch(r -> r.getId().equals(environmentService.getTailRoleId()))) {
			event.reply("Hey! You can't do that! :p").setEphemeral(true).queue();
		}

		 NinetalesUser ntUser = mongoUserService.getUserByQuestionChannelId(event.getChannelIdLong());
		 if(ntUser == null) {
			 event.reply("Are you in a questions channel :p").setEphemeral(true).queue();
			 return;
		 }

		mongoUserService.setQuestionChannelId(ntUser.getDiscordId(), null);
		event.getChannel().asTextChannel().delete().queue();

	}
}
