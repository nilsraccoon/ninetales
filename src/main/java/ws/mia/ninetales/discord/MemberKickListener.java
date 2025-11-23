package ws.mia.ninetales.discord;

import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;
import ws.mia.ninetales.mongo.MongoUserService;
import ws.mia.ninetales.mongo.NinetalesUser;

@Component
public class MemberKickListener extends ListenerAdapter {

	private final MongoUserService mongoUserService;

	public MemberKickListener(MongoUserService mongoUserService) {
		this.mongoUserService = mongoUserService;
	}

	@Override
	public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
		NinetalesUser ntUser = mongoUserService.getUser(event.getUser().getIdLong());
		if(ntUser == null) return;

		mongoUserService.deleteUser(ntUser.getDiscordId());
	}

}
