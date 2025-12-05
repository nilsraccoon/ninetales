package ws.mia.ninetales.discord.misc;

import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.mojang.MojangAPI;
import ws.mia.ninetales.mongo.MongoUserService;
import ws.mia.ninetales.mongo.NinetalesUser;

@Component
public class MemberRemovalListener extends ListenerAdapter {

	private final MongoUserService mongoUserService;
	private final DiscordLogService discordLogService;
	private final MojangAPI mojangAPI;

	public MemberRemovalListener(MongoUserService mongoUserService, @Lazy DiscordLogService discordLogService, MojangAPI mojangAPI) {
		this.mongoUserService = mongoUserService;
		this.discordLogService = discordLogService;
		this.mojangAPI = mojangAPI;
	}

	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		NinetalesUser ntUser = mongoUserService.getUser(event.getUser().getIdLong());
		if(ntUser == null) return;


		if(ntUser.getDiscordApplicationChannelId() != null) {
			event.getGuild().getTextChannelById(ntUser.getDiscordApplicationChannelId()).delete().queue();

			discordLogService.info("Cleanup", "Closed discord application channel for <@%s> (`%s`) due to discord leave/kick"
					.formatted(ntUser.getDiscordId(), mojangAPI.getUsername(ntUser.getMinecraftUuid())));
		}

		if(ntUser.getGuildApplicationChannelId() != null) {
			event.getGuild().getTextChannelById(ntUser.getGuildApplicationChannelId()).delete().queue();

			discordLogService.info("Cleanup", "Closed guild application channel for <@%s> (`%s`) due to discord leave/kick"
					.formatted(ntUser.getDiscordId(), mojangAPI.getUsername(ntUser.getMinecraftUuid())));
		}

		if(ntUser.getTailDiscussionChannelId() != null) {
			event.getGuild().getTextChannelById(ntUser.getTailDiscussionChannelId()).delete().queue();

			discordLogService.debug("Cleanup", "Closed tail discussion channel for <@%s> (`%s`) due to discord leave/kick"
					.formatted(ntUser.getDiscordId(), mojangAPI.getUsername(ntUser.getMinecraftUuid())));
		}

		if(ntUser.getQuestionChannelId() != null) {
			event.getGuild().getTextChannelById(ntUser.getQuestionChannelId()).delete().queue();

			discordLogService.info("Cleanup", "Closed question channel for <@%s> due to discord leave/kick"
					.formatted(ntUser.getDiscordId()));
		}

		mongoUserService.deleteUser(ntUser.getDiscordId());
		discordLogService.debug("Leave/Kick", "Deleted data for <@"+ntUser.getDiscordId()+"> due to removal from the discord server");
	}

}
