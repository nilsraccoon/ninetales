package ws.mia.ninetales.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.springframework.stereotype.Service;
import ws.mia.ninetales.EnvironmentService;
import ws.mia.ninetales.mojang.MojangAPI;
import ws.mia.ninetales.mongo.MongoUserService;
import ws.mia.ninetales.mongo.NinetalesUser;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ApplicationArchiveService {

	private final ApplicationService applicationService;
	private final MongoUserService mongoUserService;
	private final EnvironmentService environmentService;
	private final DiscordLogService discordLogService;
	private final MojangAPI mojangAPI;

	public ApplicationArchiveService(ApplicationService applicationService, MongoUserService mongoUserService, EnvironmentService environmentService, DiscordLogService discordLogService, MojangAPI mojangAPI) {
		this.applicationService = applicationService;
		this.mongoUserService = mongoUserService;
		this.environmentService = environmentService;
		this.discordLogService = discordLogService;
		this.mojangAPI = mojangAPI;
	}

	public void archive(TextChannel applicationChannel) {
		NinetalesUser ntUser = mongoUserService.getUserByApplicationChannelId(applicationChannel.getIdLong());
		if (ntUser == null) {
			return;
		}
		ForumChannel forum = null;
		String archContent = null;
		if (ntUser.getGuildApplicationChannelId() != null) {
			String arch = environmentService.getGuildApplicationsArchiveForum();
			if (arch == null) return;
			forum = applicationChannel.getGuild().getForumChannelById(arch);
			archContent = "Archive of <@" + ntUser.getDiscordId() + ">'s guild application";
		}
		if (ntUser.getDiscordApplicationChannelId() != null) {
			String arch = environmentService.getDiscordApplicationsArchiveForum();
			if (arch == null) return;
			forum = applicationChannel.getGuild().getForumChannelById(arch);
			archContent = "Archive of <@" + ntUser.getDiscordId() + ">'s discord application";
		}

		Member ntMember = applicationChannel.getGuild().retrieveMemberById(ntUser.getDiscordId()).complete();
		System.out.println(ntMember);
		if (forum == null) return;

		String mcName = mojangAPI.getUsername(ntUser.getMinecraftUuid());
		if (mcName == null) mcName = applicationChannel.getName();

		List<Message> messages = new ArrayList<>(applicationChannel.getHistory().retrievePast(100).complete());
		Collections.reverse(messages);

		forum.createForumPost(mcName, MessageCreateData.fromContent(archContent)).queue(archiveChannel -> {
			List<MessageEmbed> embeds = new ArrayList<>();
			messages.forEach(message -> {
				Color c = message.getMember() != null ? message.getMember().getColor() : null;
				if (ntMember != null) c = ntMember.getColor(); // more up-to-date
				EmbedBuilder eb = new EmbedBuilder()
						.setAuthor(message.getAuthor().getEffectiveName(), null, message.getAuthor().getAvatarUrl())
						.setTimestamp(message.getTimeCreated())
						.setDescription(message.getContentDisplay())
						.setColor(c);
				embeds.add(eb.build());

			});

			// we can send up to 10 embeds at a time, so group.
			List<List<MessageEmbed>> partitions = new ArrayList<>();
			for (int i = 0; i < embeds.size(); i += 10) {
				partitions.add(embeds.subList(i, Math.min(i + 10, embeds.size())));
			}
			if (ntMember != null) {
				ntMember.getUser().openPrivateChannel().queue(pc -> {
					String ap = ntUser.getDiscordApplicationChannelId() != null ? "discord" : "guild";
					pc.sendMessage("Below is a transcript of your recent application to join the Ninetales %s:".formatted(ap)).queue();
					partitions.forEach(m -> {
						pc.sendMessageEmbeds(m).queue();
					});
				});
			}

			partitions.forEach(m -> {
				archiveChannel.getThreadChannel().sendMessageEmbeds(m).queue();
			});

			discordLogService.debug("Application Archive", "Archived <@" + ntUser.getDiscordId() + ">'s application to " + archiveChannel.getThreadChannel().getJumpUrl());
			archiveChannel.getThreadChannel().getManager().setLocked(true).queue();
		});

	}


}
