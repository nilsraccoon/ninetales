package ws.mia.ninetales.mongo;

import java.util.UUID;

public class NinetalesUser {

	private long discordId;
	private UUID minecraftUuid;
	private Long discordApplicationChannelId;
	private Long guildApplicationChannelId;
	private boolean awaitingHypixelInvite;
	private Long questionChannelId;
	private boolean discordMember;

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

	protected void setQuestionChannelId(Long questionChannelId) {
		this.questionChannelId = questionChannelId;
	}

	public boolean isAwaitingHypixelInvite() {
		return awaitingHypixelInvite;
	}

	protected void setAwaitingHypixelInvite(boolean awaitingHypixelInvite) {
		this.awaitingHypixelInvite = awaitingHypixelInvite;
	}
}