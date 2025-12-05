package ws.mia.ninetales.discord.command;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.discord.misc.DiscordLogService;
import ws.mia.ninetales.mojang.MojangAPI;
import ws.mia.ninetales.mongo.MongoUserService;
import ws.mia.ninetales.mongo.NinetalesUser;

@Component
public class WhoCommand extends SlashCommand {
	private static final String COMMAND = "ign";
	private final MongoUserService mongoUserService;
	private final MojangAPI mojangAPI;
	private final DiscordLogService discordLogService;

	public WhoCommand(MongoUserService mongoUserService, MojangAPI mojangAPI, @Lazy DiscordLogService discordLogService) {
		super();
		this.mongoUserService = mongoUserService;
		this.mojangAPI = mojangAPI;
		this.discordLogService = discordLogService;
	}


	@Override
	public CommandData getCommand() {
		return Commands.slash(COMMAND, "Find out someone's minecraft IGN if you're too shy to ask")
				.addOption(OptionType.USER, "user", "user", true);
	}

	@Override
	public void onCommand(SlashCommandInteractionEvent event) {
		OptionMapping optUser = event.getOption("user");
		if (optUser == null) {
			event.reply("Who?").setEphemeral(true).queue();
			return;
		}

		User user = optUser.getAsUser();

		NinetalesUser ntUser = mongoUserService.getUser(user.getIdLong());
		if(ntUser == null || ntUser.getMinecraftUuid() == null) {
			event.reply("That user hasn't linked yet :(").setEphemeral(true).queue();
			return;
		}

		String mcIgn = mojangAPI.getUsername(ntUser.getMinecraftUuid());

		if(mcIgn == null) {
			event.reply("The Mojang API is having a few issues right now :(\nJust ask <@%s> directly!".formatted(user.getId())).setEphemeral(true).queue();
			return;
		}
		String rep = "<@%s> is `%s` on Minecraft!".formatted(user.getId(), mcIgn);
		event.reply(rep).setEphemeral(true).queue();
		discordLogService.debug(event, rep);
	}
}
