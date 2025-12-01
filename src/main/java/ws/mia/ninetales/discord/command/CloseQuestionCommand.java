package ws.mia.ninetales.discord.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.EnvironmentService;
import ws.mia.ninetales.discord.DiscordLogService;
import ws.mia.ninetales.mongo.MongoUserService;
import ws.mia.ninetales.mongo.NinetalesUser;

import java.util.List;
import java.util.Objects;

@Component
public class CloseQuestionCommand extends SlashCommand{
	private static final String COMMAND = "close-question";
	private final MongoUserService mongoUserService;
	private final EnvironmentService environmentService;
	private final DiscordLogService discordLogService;

	public CloseQuestionCommand(MongoUserService mongoUserService, EnvironmentService environmentService, @Lazy DiscordLogService discordLogService) {
		super();
		this.mongoUserService = mongoUserService;
		this.environmentService = environmentService;
		this.discordLogService = discordLogService;
	}

	@Override
	public CommandData getCommand() {
		return Commands.slash(COMMAND, "Close a question channel")
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VIEW_AUDIT_LOGS));
	}

	@Override
	public List<String> roles() {
		return List.of(environmentService.getTailRoleId());
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {

		 NinetalesUser ntUser = mongoUserService.getUserByQuestionChannelId(event.getChannelIdLong());
		 if(ntUser == null) {
			 event.reply("Are you in a questions channel :p").setEphemeral(true).queue();
			 return;
		 }

		mongoUserService.setQuestionChannelId(ntUser.getDiscordId(), null);
		event.getChannel().asTextChannel().delete().queue();
		discordLogService.info(event, "Closed message channel for <@" + ntUser.getDiscordId() + ">");

	}
}
