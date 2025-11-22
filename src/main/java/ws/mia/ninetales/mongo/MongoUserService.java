package ws.mia.ninetales.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MongoUserService {

	private final MongoCollection<Document> usersCollection;

	public MongoUserService(MongoCollection<Document> usersCollection) {
		this.usersCollection = usersCollection;
	}

	public boolean linkUser(long discordId, UUID minecraftUuid) {
		if (isUserLinked(discordId)) {
			return false;
		}

		if (isUserLinked(minecraftUuid)) {
			return false;
		}

		Document userDoc = new Document()
				.append("discordId", discordId)
				.append("minecraftUuid", minecraftUuid.toString())
				.append("status", UserStatus.OUTSIDER.name());

		usersCollection.insertOne(userDoc);
		return true;
	}

	public NinetalesUser getUser(long discordId) {
		Document doc = usersCollection.find(Filters.eq("discordId", discordId)).first();
		return documentToUser(doc);
	}

	public NinetalesUser getUser(UUID minecraftUuid) {
		Document doc = usersCollection.find(Filters.eq("minecraftUuid", minecraftUuid.toString())).first();
		return documentToUser(doc);
	}

	public NinetalesUser getUserByApplicationChannelId(long channelId) {
		Bson filter = Filters.or(
				Filters.eq("discordApplicationChannelId", channelId),
				Filters.eq("guildApplicationChannelId", channelId)
		);
		Document doc = usersCollection.find(filter).first();
		return documentToUser(doc);
	}

	public UUID getMinecraftUuid(long discordId) {
		Document userDoc = usersCollection.find(Filters.eq("discordId", discordId)).first();
		if (userDoc == null) {
			return null;
		}
		String uuidString = userDoc.getString("minecraftUuid");
		return uuidString != null ? UUID.fromString(uuidString) : null;
	}

	public Long getDiscordId(UUID minecraftUuid) {
		Document userDoc = usersCollection.find(Filters.eq("minecraftUuid", minecraftUuid.toString())).first();
		if (userDoc == null) {
			return null;
		}
		return userDoc.getLong("discordId");
	}

	public boolean isUserLinked(long discordId) {
		Document userDoc = usersCollection.find(Filters.eq("discordId", discordId)).first();
		if (userDoc == null) {
			return false;
		}
		return userDoc.getString("minecraftUuid") != null;
	}

	public boolean isUserLinked(UUID minecraftUuid) {
		Document userDoc = usersCollection.find(Filters.eq("minecraftUuid", minecraftUuid.toString())).first();
		return userDoc != null;
	}

	public boolean unlinkUser(long discordId) {
		return usersCollection.deleteOne(Filters.eq("discordId", discordId)).getDeletedCount() > 0;
	}

	public boolean unlinkUser(UUID minecraftUuid) {
		return usersCollection.deleteOne(Filters.eq("minecraftUuid", minecraftUuid.toString())).getDeletedCount() > 0;
	}

	public void setDiscordApplicationChannelId(long discordId, Long channelId) {
		ensureUserExists(discordId);
		usersCollection.updateOne(
				Filters.eq("discordId", discordId),
				Updates.set("discordApplicationChannelId", channelId)
		);
	}

	public void setGuildApplicationChannelId(long discordId, Long channelId) {
		ensureUserExists(discordId);
		usersCollection.updateOne(
				Filters.eq("discordId", discordId),
				Updates.set("guildApplicationChannelId", channelId)
		);
	}

	public void setQuestionChannelId(long discordId, Long channelId) {
		ensureUserExists(discordId);
		usersCollection.updateOne(
				Filters.eq("discordId", discordId),
				Updates.set("questionChannelId", channelId)
		);
	}

	public void setStatus(long discordId, UserStatus status) {
		ensureUserExists(discordId);
		usersCollection.updateOne(
				Filters.eq("discordId", discordId),
				Updates.set("status", status.name())
		);
	}

	public void setAwaitingHypixelInvite(long discordId, boolean a) {
		ensureUserExists(discordId);
		usersCollection.updateOne(
				Filters.eq("discordId", discordId),
				Updates.set("awaitingHypixelInvite", a)
		);
	}

	private void ensureUserExists(long discordId) {
		if (!isUserLinked(discordId)) {
			Document userDoc = new Document()
					.append("discordId", discordId)
					.append("status", UserStatus.OUTSIDER.name());
			usersCollection.insertOne(userDoc);
		}
	}

	private NinetalesUser documentToUser(Document doc) {
		if (doc == null) {
			return null;
		}

		NinetalesUser ninetalesUser = new NinetalesUser();
		ninetalesUser.setDiscordId(doc.getLong("discordId"));

		String uuidString = doc.getString("minecraftUuid");
		if (uuidString != null) {
			ninetalesUser.setMinecraftUuid(UUID.fromString(uuidString));
		}

		ninetalesUser.setDiscordApplicationChannelId(doc.getLong("discordApplicationChannelId"));
		ninetalesUser.setGuildApplicationChannelId(doc.getLong("guildApplicationChannelId"));
		ninetalesUser.setQuestionChannelId(doc.getLong("questionChannelId"));
		ninetalesUser.setAwaitingHypixelInvite(doc.getBoolean("awaitingHypixelInvite", false));

		String statusStr = doc.getString("status");
		ninetalesUser.setStatus(statusStr != null ? UserStatus.valueOf(statusStr) : UserStatus.OUTSIDER);

		return ninetalesUser;
	}

}