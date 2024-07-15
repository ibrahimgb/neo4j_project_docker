package example;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;
import org.neo4j.driver.*;

public class Neo4jCypherExecutor {

    private final Driver driver;
    private final String database;

    public Neo4jCypherExecutor(String uri, String user, String password, String database) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        this.database = database;
    }

    public void close() {
        driver.close();
    }

    public void ensureDatabaseExists() {
        try (Session session = driver.session()) {
            String createDatabaseQuery = String.format("CREATE DATABASE %s IF NOT EXISTS;", database);
            session.writeTransaction(tx -> {
                tx.run(createDatabaseQuery).consume();
                return null;
            });
            System.out.println("Database '" + database + "' ensured to exist.");
        } catch (Exception e) {
            System.err.println("Error ensuring database exists: " + e.getMessage());
        }
    }

    public void executeCypherFile(String filePath) {
        try (Session session = driver.session(SessionConfig.forDatabase(database))) {
            String apocQuery = String.format("CALL apoc.cypher.runFile('%s')", filePath);
            session.writeTransaction(tx -> {
                tx.run(apocQuery).consume();
                return null;
            });
            System.out.println("Cypher queries executed successfully on database '" + database + "' using apoc.cypher.runFile.");
        } catch (Exception e) {
            System.err.println("Error executing Cypher queries: " + e.getMessage());
        }
    }

    public void reviewsCount() {
        try (Session session = driver.session(SessionConfig.forDatabase(database))) {
            String apocQuery = "MATCH (m:Movie)<-[:RATED]-(u:User)\n" +
                    "WHERE toLower(m.title) CONTAINS 'matrix'\n" +
                    "WITH m, count(*) AS reviews\n" +
                    "RETURN m.title AS movie, reviews\n" +
                    "ORDER BY reviews DESC LIMIT 5;";
            session.writeTransaction(tx -> {
                Result result = tx.run(apocQuery);
                while (result.hasNext()) {
                    org.neo4j.driver.Record record = result.next();
                    String movie = record.get("movie").asString();
                    int reviews = record.get("reviews").asInt();
                    System.out.println("Movie: " + movie + ", Reviews: " + reviews);
                }
                return null;
            });

            System.out.println("Cypher queries executed successfully on database: '" + database + "'.");
        } catch (Exception e) {
            System.err.println("Error executing Cypher queries: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String uri = "bolt://localhost:7687";
        String user = "neo4j";
        String password = "admin12345"; // replace with your actual password
        String database = "recommendations";
        String filePath = "/all-plain.cypher"; // replace with the actual path to your Cypher file

        Neo4jCypherExecutor executor = new Neo4jCypherExecutor(uri, user, password, database);
        executor.ensureDatabaseExists();
        //executor.executeCypherFile(filePath);
        executor.reviewsCount();
        executor.close();
    }
}
