package ws.mia.ninetales.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ws.mia.ninetales.EnvironmentService;

import java.util.concurrent.TimeUnit;

@Configuration
public class MongoConfiguration {



	@Bean
	public MongoClient mongoClient(EnvironmentService environmentService) {
		String uri = environmentService.getMongoUri();
		if (uri == null || uri.isBlank()) {
			throw new IllegalStateException("MONGO_URI environment variable must be set!");
		}

		ConnectionString connectionString = new ConnectionString(uri);
		MongoClientSettings settings = MongoClientSettings.builder()
				.applyConnectionString(connectionString)
				.applyToConnectionPoolSettings(builder -> builder
						.maxSize(100)
						.minSize(5)
						.maxWaitTime(5, TimeUnit.SECONDS))
				.build();

		return MongoClients.create(settings);
	}

	@Bean
	public String mongoDatabaseName(EnvironmentService environmentService) {
		String uri = environmentService.getMongoUri();
		ConnectionString connectionString = new ConnectionString(uri);
		String db = connectionString.getDatabase();
		if (db == null) {
			throw new IllegalStateException("No database name specified in MONGO_URI");
		}
		return db;
	}

	@Bean
	public MongoDatabase mongoDatabase(MongoClient mongoClient, String mongoDatabaseName) {
		return mongoClient.getDatabase(mongoDatabaseName);
	}

	@Bean
	public MongoCollection<Document> routesCollection(MongoDatabase database, EnvironmentService environmentService) {
		return database.getCollection(environmentService.getMongoUsersCollectionName());
	}

}
