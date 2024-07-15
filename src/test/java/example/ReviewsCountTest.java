package example;
import org.neo4j.driver.*;

public class ReviewsCountTest {

    private final Driver driver;
    private final String database;

    public ReviewsCountTest(Driver driver, String database) {
        this.driver = driver;
        this.database = database;
    }

    public void reviewsCount() {
        try (Session session = driver.session(SessionConfig.forDatabase(database))) {
            String apocQuery = "MATCH (m:Movie)<-[:RATED]-(u:User)\n" +
                    "WHERE toLower(m.title) CONTAINS 'matrix'\n" +
                    "WITH m, count(*) AS reviews\n" +
                    "RETURN m.title AS movie, reviews\n" +
                    "ORDER BY reviews DESC LIMIT 5;";

            Result result = session.writeTransaction(tx -> tx.run(apocQuery));

            // Iterate over the result and print the movie titles and reviews
            while (result.hasNext()) {
                org.neo4j.driver.Record record = result.next();
                String movie = record.get("movie").asString();
                int reviews = record.get("reviews").asInt();
                System.out.println("Movie: " + movie + ", Reviews: " + reviews);
            }

            System.out.println("Cypher queries executed successfully on database: '" + database + "'.");

        } catch (Exception e) {
            System.err.println("Error executing Cypher queries: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Replace these with your actual Neo4j credentials and URI
        String uri = "bolt://localhost:7687";
        String user = "neo4j";
        String password = "password";
        String database = "your-database-name";

        Driver driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        ReviewsCountTest reviewsCountTest = new ReviewsCountTest(driver, database);
        reviewsCountTest.reviewsCount();

        driver.close();
    }
}
