package ws.mia.ninetales.discord.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.EnvironmentService;
import ws.mia.ninetales.discord.DiscordLogService;
import ws.mia.ninetales.mongo.MongoUserService;

import java.util.List;
import java.util.Objects;

@Component
public class NtSayCommand extends SlashCommand {
	private static final String COMMAND = "nt-say";
	private final MongoUserService mongoUserService;
	private final EnvironmentService environmentService;
	private final DiscordLogService discordLogService;


	public NtSayCommand(MongoUserService mongoUserService, EnvironmentService environmentService, @Lazy DiscordLogService discordLogService) {
		super();
		this.mongoUserService = mongoUserService;
		this.environmentService = environmentService;
		this.discordLogService = discordLogService;
	}

	@Override
	public CommandData getCommand() {
		return Commands.slash(COMMAND, ":3")
				.addOption(OptionType.STRING, "message", "message", true)
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VIEW_AUDIT_LOGS));
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {
		if (Objects.requireNonNull(event.getMember()).getUnsortedRoles().stream().noneMatch(r -> r.getId().equals(environmentService.getTailRoleId()))) {
			event.reply("You can't do that! :p").setEphemeral(true).queue();
			return;
		}

		OptionMapping opt = event.getOption("message");
		if (opt == null) return;

		String msg = opt.getAsString();

		if (mongoUserService.getUserByApplicationChannelId(event.getChannelIdLong()) != null) {
			// We use bot messages as a counter, this would screw that up
			event.reply("Not here, sorry").setEphemeral(true).queue();
			return;
		}

		event.deferReply(true).queue(hook -> hook.deleteOriginal().queue());

		event.getChannel().asTextChannel().sendMessage(msg)
				.setAllowedMentions(List.of(Message.MentionType.ROLE, Message.MentionType.USER, Message.MentionType.CHANNEL, Message.MentionType.HERE))
				.queue(m -> discordLogService.debug(event, m));


	}

}
