package ws.mia.ninetales.discord.command;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NtSayCommand extends SlashCommand {
	private static final String COMMAND = "nt-say";


	public NtSayCommand() {
		super();
	}

	@Override
	public CommandData getCommand() {
		return Commands.slash(COMMAND, ":3")
				.addOption(OptionType.STRING, "message", "message", true)
				.setDefaultPermissions(DefaultMemberPermissions.DISABLED);
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {
		OptionMapping opt = event.getOption("message");
		if (opt == null) return;

		String msg = opt.getAsString();

		event.deferReply(true).queue(hook -> hook.deleteOriginal().queue());

		event.getChannel().asTextChannel().sendMessage(msg)
				.setAllowedMentions(List.of(Message.MentionType.ROLE, Message.MentionType.USER, Message.MentionType.CHANNEL, Message.MentionType.HERE))
				.queue();
	}

}
