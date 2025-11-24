package ws.mia.ninetales.discord;

import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.mongo.MongoUserService;
import ws.mia.ninetales.mongo.NinetalesUser;

@Component
public class MemberKickListener extends ListenerAdapter {

	private final MongoUserService mongoUserService;
	private final DiscordLogService discordLogService;

	public MemberKickListener(MongoUserService mongoUserService, @Lazy DiscordLogService discordLogService) {
		this.mongoUserService = mongoUserService;
		this.discordLogService = discordLogService;
	}

	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		NinetalesUser ntUser = mongoUserService.getUser(event.getUser().getIdLong());
		if(ntUser == null) return;

		mongoUserService.deleteUser(ntUser.getDiscordId());
		discordLogService.debug("Leave/Kick", "Deleted data for <@"+ntUser.getDiscordId()+"> due to removal from the discord server");
	}

}
