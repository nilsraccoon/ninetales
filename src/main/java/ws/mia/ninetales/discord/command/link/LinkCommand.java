package ws.mia.ninetales.discord.command.link;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.EnvironmentService;
import ws.mia.ninetales.discord.GuildRankService;
import ws.mia.ninetales.discord.command.SlashCommand;
import ws.mia.ninetales.hypixel.CachedHypixelAPI;
import ws.mia.ninetales.hypixel.HypixelAPI;
import ws.mia.ninetales.mojang.MojangAPI;
import ws.mia.ninetales.mongo.MongoUserService;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Component
public class LinkCommand extends SlashCommand {
	private static final String COMMAND = "link";
	private static final Logger log = LoggerFactory.getLogger(LinkCommand.class);
	private final MojangAPI mojangAPI;
	private final HypixelAPI hypixelAPI;
	private final MongoUserService mongoUserService;
	private final EnvironmentService environmentService;
	private final TaskScheduler taskScheduler;
	private final GuildRankService guildRankService;

	public LinkCommand(MojangAPI mojangAPI, HypixelAPI hypixelAPI, MongoUserService mongoUserService, EnvironmentService environmentService, TaskScheduler taskScheduler, @Lazy GuildRankService guildRankService) {
		super();
		this.mojangAPI = mojangAPI;
		this.hypixelAPI = hypixelAPI;
		this.mongoUserService = mongoUserService;
		this.environmentService = environmentService;
		this.taskScheduler = taskScheduler;
		this.guildRankService = guildRankService;
	}

	@Override
	public CommandData getCommand() {
		return Commands.slash(COMMAND, "Let us know who you are :3")
				.addOption(OptionType.STRING, "username", "Your Minecraft Username", true);
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {
		if (!Objects.equals(event.getChannelId(), environmentService.getLinkChannelId())) {
			event.reply("If you want to link, use <#%s>".formatted(environmentService.getLinkChannelId())).setEphemeral(true).queue();
			return;
		}

		OptionMapping username = event.getOption("username");
		if (username == null) {
			event.reply("Specify your IGN").setEphemeral(true).queue();
			return;
		}

		if (mongoUserService.isUserLinked(event.getUser().getIdLong())) {
			event.reply("You're already linked you goober!\n-# (if you need help, message a tail :3)").setEphemeral(true).queue();
			return;
		}

		UUID mojangUuid = mojangAPI.getUuid(username.getAsString().trim());

		if (mojangUuid == null) {
			event.reply("We couldn't find your minecraft account :(\nPlease contact a tail\n-# (blame mia)").setEphemeral(true).queue();
			return;
		}

		String expectedDiscord = hypixelAPI.getDiscord(mojangUuid);

		if (expectedDiscord == null) {
			event.reply("We couldn't find a discord on your Hypixel profile.\nPlease link your discord on Hypixel using the method above <3\n-# (Hypixel may take a moment to update your information, if you have linked on hypixel and are seeing this, please wait a moment and try again)").setEphemeral(true).queue();
			return;
		}

		if (!expectedDiscord.equals(event.getUser().getName())) {
			event.reply("That minecraft account isn't linked to your discord!\nFollow the steps above to link your account.").setEphemeral(true).queue();
			return;
		}


		if (!mongoUserService.linkUser(event.getUser().getIdLong(), mojangUuid)) {
			log.warn("Unable to link {} (IGN) to {} (Discord)", username.getAsString(), event.getUser().getName());
			event.reply("We couldn't link you :(\nThis likely means that you are already linked.\n-# (blame mia)").setEphemeral(true).queue();
			return;
		}
		event.reply("Successfully linked you to `" + username.getAsString() + "`!\nWelcome to Ninetales :3").setEphemeral(true).queue();
		log.info("Linked {} (IGN) to {} (Discord)", username.getAsString(), event.getUser().getName());

		hypixelAPI.getGuildRanks();

		// sync roles once DB has updated
		taskScheduler.schedule(() -> {
			guildRankService.syncFirstRole(Objects.requireNonNull(event.getMember()), event.getGuild());
		}, Instant.now().plusSeconds(2));
	}


}
