package ws.mia.ninetales.discord;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ws.mia.ninetales.EnvironmentService;
import ws.mia.ninetales.discord.command.SlashCommand;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class JDAConfiguration {

	@Bean
	public JDA jda(List<SlashCommand> commands, List<EventListener> listeners, EnvironmentService environmentService) throws InterruptedException {
		JDA jda = JDABuilder
				.createDefault(environmentService.getDiscordBotToken())
				.enableIntents(List.of(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MEMBERS))
				.addEventListeners(listeners.toArray(new Object[0]))
				.build().awaitReady();

		jda.updateCommands().addCommands(commands.stream().map(sc -> {
			return sc.getCommand().setContexts(InteractionContextType.GUILD);
		}).toList()).queue();

		return jda;
	}

	@Bean
	public Map<String, Command> ninetalesCommands(JDA jda, List<SlashCommand> commands) {
		Map<String, Command> commandMap = new HashMap<>();

		List<Command> registeredCommands = jda.retrieveCommands().complete();

		registeredCommands.forEach(cmd -> {
			commands.stream()
					.filter(sc -> cmd.getName().equals(sc.getCommand().getName()))
					.findFirst()
					.ifPresent(sc -> commandMap.put(cmd.getName(), cmd));
		});

		return commandMap;
	}
}
