package ws.mia.ninetales.mongo;

import java.util.UUID;

public class NinetalesUser {

	private long discordId;
	private UUID minecraftUuid;
	private Long discordApplicationChannelId;
	private Long guildApplicationChannelId;
	private Long tailDiscussionChannelId;
	private boolean awaitingHypixelInvite;
	private Long questionChannelId;
	private boolean discordMember;
	private boolean guildJoinMessage;

	public NinetalesUser() {
	}

	public NinetalesUser(long discordId) {
		this.discordId = discordId;
	}

	public NinetalesUser(long discordId, UUID minecraftUuid) {
		this.discordId = discordId;
		this.minecraftUuid = minecraftUuid;
	}

	public long getDiscordId() {
		return discordId;
	}

	public UUID getMinecraftUuid() {
		return minecraftUuid;
	}

	public Long getDiscordApplicationChannelId() {
		return discordApplicationChannelId;
	}

	public Long getGuildApplicationChannelId() {
		return guildApplicationChannelId;
	}

	public Long getQuestionChannelId() {
		return questionChannelId;
	}

	public boolean isDiscordMember() {
		return discordMember;
	}

	protected void setDiscordId(long discordId) {
		this.discordId = discordId;
	}

	protected void setMinecraftUuid(UUID minecraftUuid) {
		this.minecraftUuid = minecraftUuid;
	}

	protected void setDiscordApplicationChannelId(Long discordApplicationChannelId) {
		this.discordApplicationChannelId = discordApplicationChannelId;
	}

	protected void setGuildApplicationChannelId(Long guildApplicationChannelId) {
		this.guildApplicationChannelId = guildApplicationChannelId;
	}

	protected void setDiscordMember(boolean b) {
		this.discordMember = b;
	}

	protected void setHasHadGuildJoinMessage(boolean b) {
		this.guildJoinMessage = b;
	}

	public boolean hasHadGuildJoinMessage() {
		return guildJoinMessage;
	}

	protected void setQuestionChannelId(Long questionChannelId) {
		this.questionChannelId = questionChannelId;
	}

	public boolean isAwaitingHypixelInvite() {
		return awaitingHypixelInvite;
	}

	protected void setAwaitingHypixelInvite(boolean awaitingHypixelInvite) {
		this.awaitingHypixelInvite = awaitingHypixelInvite;
	}

	public Long getTailDiscussionChannelId() {
		return tailDiscussionChannelId;
	}

	protected void setTailDiscussionChannelId(Long tailDiscussionChannelId) {
		this.tailDiscussionChannelId = tailDiscussionChannelId;
	}

	@Override
	public String toString() {
		return "NinetalesUser{" +
				"discordId=" + discordId +
				", minecraftUuid=" + minecraftUuid +
				", discordApplicationChannelId=" + discordApplicationChannelId +
				", guildApplicationChannelId=" + guildApplicationChannelId +
				", tailDiscussionChannelId=" + tailDiscussionChannelId +
				", awaitingHypixelInvite=" + awaitingHypixelInvite +
				", questionChannelId=" + questionChannelId +
				", discordMember=" + discordMember +
				", guildJoinMessage=" + guildJoinMessage +
				'}';
	}

	public String toJsonString() {
		return """
		{
		  "discordId": %d,
		  "minecraftUuid": %s,
		  "discordApplicationChannelId": %s,
		  "guildApplicationChannelId": %s,
		  "tailDiscussionChannelId": %s,
		  "questionChannelId": %s,
		  "awaitingHypixelInvite": %b,
		  "discordMember": %b
		  "guildJoinMessage": %b
		}""".formatted(
				discordId,
				minecraftUuid != null ? "\"" + minecraftUuid + "\"" : "null",
				discordApplicationChannelId != null ? discordApplicationChannelId : "null",
				guildApplicationChannelId != null ? guildApplicationChannelId : "null",
				tailDiscussionChannelId != null ? tailDiscussionChannelId : "null",
				questionChannelId != null ? questionChannelId : "null",
				awaitingHypixelInvite,
				discordMember,
				guildJoinMessage
		);
	}
}