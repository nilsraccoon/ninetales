package ws.mia.ninetales.discord.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.EnvironmentService;
import ws.mia.ninetales.discord.misc.DiscordLogService;
import ws.mia.ninetales.mongo.MongoUserService;

import java.util.List;
import java.util.function.Consumer;

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
				.addOption(OptionType.STRING, "reply", "message ID of msg to reply to (same channel) ", false)
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VIEW_AUDIT_LOGS));
	}

	@Override
	public List<String> roles() {
		return List.of(environmentService.getTailRoleId());
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {

		OptionMapping msgOpt = event.getOption("message");
		if (msgOpt == null) return;

		String msg = msgOpt.getAsString();

		if (mongoUserService.getUserByApplicationChannelId(event.getChannelIdLong()) != null) {
			// We use bot messages as a counter, this would screw that up
			event.reply("Not here, sorry").setEphemeral(true).queue();
			return;
		}

		event.deferReply(true).queue(hook -> hook.deleteOriginal().queue());

		OptionMapping replyOpt = event.getOption("reply");

		Consumer<MessageCreateAction> finalize = (mc) -> {
		mc.setAllowedMentions(List.of(Message.MentionType.ROLE, Message.MentionType.USER, Message.MentionType.CHANNEL, Message.MentionType.HERE))
				.queue(m -> discordLogService.debug(event, m));
		};

		if(replyOpt != null) {
			event.getChannel().asTextChannel().retrieveMessageById(replyOpt.getAsString()).queue((s) -> {
				finalize.accept(s.reply(msg));
			}, (t) -> { /* :( */ });
			return;
		}

		finalize.accept(event.getChannel().asTextChannel().sendMessage(msg));


	}

}
