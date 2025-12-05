package ws.mia.ninetales.discord.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import org.springframework.boot.logging.LogLevel;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import ws.mia.ninetales.EnvironmentService;

import java.awt.*;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Lazy
@DependsOn("jda")
public class DiscordLogService {

	private final JDA jda;
	private final EnvironmentService environmentService;
	private final Map<String, Command> ninetalesCommands;

	public DiscordLogService(@Lazy JDA jda, EnvironmentService environmentService, @Lazy Map<String, Command> ninetalesCommands) {
		this.jda = jda;
		this.environmentService = environmentService;
		this.ninetalesCommands = ninetalesCommands;
	}

	public void log(LogLevel level, String title, String message) {
		if(level != LogLevel.DEBUG && environmentService.getLogChannelId() == null) return;
		if(level == LogLevel.DEBUG && environmentService.getDebugLogChannelId() == null) return;

		TextChannel channel = jda.getTextChannelById(level != LogLevel.DEBUG ? environmentService.getLogChannelId() : environmentService.getDebugLogChannelId());

		Color color = switch (level) {
			case ERROR, FATAL -> new Color(248, 102, 102);
			case WARN -> new Color(237, 187, 22);
			case INFO -> new Color(175, 150, 219);
			case DEBUG -> new Color(168, 168, 168);
			default -> new Color(0, 0, 0);
		};

		MessageEmbed bEmebd = new EmbedBuilder()
				.setTitle(title)
				.setDescription(message)
				.setColor(color)
				.setFooter(level.toString())
				.setTimestamp(Instant.now())
				.build();

		channel.sendMessageEmbeds(bEmebd).queue();
	}

	public void log(LogLevel level, SlashCommandInteractionEvent event, String message) {
		Command cmd = ninetalesCommands.get(event.getName());
		String commandId = cmd != null ? cmd.getId() : "0";

		String fullMessage = "by <@" + event.getUser().getId() + "> in <#" + event.getChannelId() + ">";
		fullMessage += formatOptions(event);

		if (message != null && !message.isEmpty()) {
			if(!fullMessage.trim().endsWith("```")) fullMessage +="\n"; // spacing
			fullMessage += "\n" + message;
		}

		this.log(level, "</" + event.getName() + ":" + commandId + ">", fullMessage);
	}

	public void log(LogLevel level, SlashCommandInteractionEvent event) {
		this.log(level, event, (String) null);
	}

	public void log(LogLevel level, SlashCommandInteractionEvent event, Message message) {
		Command cmd = ninetalesCommands.get(event.getName());
		String commandId = cmd != null ? cmd.getId() : "0";

		String fullMessage = "<@" + event.getUser().getId() + "> used at " + message.getJumpUrl();
		fullMessage += formatOptions(event);

		this.log(level, "</" + event.getName() + ":" + commandId + ">", fullMessage);
	}

	private String formatOptions(SlashCommandInteractionEvent event) {
		String options = event.getOptions().stream()
				.map(opt -> {
					Object value = switch (opt.getType()) {
						case INTEGER -> opt.getAsLong();
						case BOOLEAN -> opt.getAsBoolean();
						case NUMBER -> opt.getAsDouble();
						case USER -> "\"" + opt.getAsUser().getId() + "\"";
						case CHANNEL -> "\"" + opt.getAsChannel().getId() + "\"";
						case ROLE -> "\"" + opt.getAsRole().getId() + "\"";
						case MENTIONABLE -> "\"" + opt.getAsMentionable().getId() + "\"";
						default -> "\"" + opt.getAsString() + "\"";
					};
					return "\"" + opt.getName() + "\": " + value;
				})
				.collect(Collectors.joining(",\n"));

		return options.isEmpty() ? "" : "\n```json\n// Options\n" + options + "\n```";
	}

	public void info(String title, String message) {
		this.log(LogLevel.INFO, title, message);
	}

	public void debug(String title, String message) {
		this.log(LogLevel.DEBUG, title, message);
	}

	public void error(String title, String message) {
		this.log(LogLevel.ERROR, title, message);
	}

	public void warn(String title, String message) {
		this.log(LogLevel.WARN, title, message);
	}

	public void info(SlashCommandInteractionEvent event, String message) {
		this.log(LogLevel.INFO, event, message);
	}

	public void debug(SlashCommandInteractionEvent event, String message) {
		this.log(LogLevel.DEBUG, event, message);
	}

	public void error(SlashCommandInteractionEvent event, String message) {
		this.log(LogLevel.ERROR, event, message);
	}

	public void warn(SlashCommandInteractionEvent event, String message) {
		this.log(LogLevel.WARN, event, message);
	}

	public void info(SlashCommandInteractionEvent event) {
		this.log(LogLevel.INFO, event);
	}

	public void debug(SlashCommandInteractionEvent event) {
		this.log(LogLevel.DEBUG, event);
	}

	public void error(SlashCommandInteractionEvent event) {
		this.log(LogLevel.ERROR, event);
	}

	public void warn(SlashCommandInteractionEvent event) {
		this.log(LogLevel.WARN, event);
	}

	public void info(SlashCommandInteractionEvent event, Message message) {
		this.log(LogLevel.INFO, event, message);
	}

	public void debug(SlashCommandInteractionEvent event, Message message) {
		this.log(LogLevel.DEBUG, event, message);
	}

	public void error(SlashCommandInteractionEvent event, Message message) {
		this.log(LogLevel.ERROR, event, message);
	}

	public void warn(SlashCommandInteractionEvent event, Message message) {
		this.log(LogLevel.WARN, event, message);
	}


}