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
import java.util.List;

import org.neo4j.driver.Record;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


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
public void separations(){
        System.out.println("///////////////////////////////////////////////////////////");
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

    // Recommend items that are similar to those that a user is viewing, rated highly or purchased
    //previously

    public void recommendItems() {
        try (Session session = driver.session(SessionConfig.forDatabase(database))) {
            String apocQuery = "MATCH (joe:Actor {name: \"Joe Pesci\"})-[r1:ACTED_IN]->(m:Movie)\n" +
                    "MATCH (robert:Actor {name: \"Robert De Niro\"})-[r2:ACTED_IN]->(m)\n" +
                    "MATCH (martin:Director {name: \"Martin Scorsese\"})-[r4:DIRECTED]->(m)\n" +
                    "MATCH (m)-[r3:IN_GENRE]->(genre:Genre)\n" +
                    "RETURN joe.name AS Actor1, robert.name AS Actor2, martin.name AS Director, m.title AS Movie, genre.name AS Genre   ";
            session.writeTransaction(tx -> {
                Result result = tx.run(apocQuery);
                while (result.hasNext()) {
                    org.neo4j.driver.Record record = result.next();
                    System.out.println("Actor1: " + record.get("Actor1").asString());
                    System.out.println("Actor2: " + record.get("Actor2").asString());
                    System.out.println("Director: " + record.get("Director").asString());
                    System.out.println("Movie: " + record.get("Movie").asString());
                    System.out.println("Genre: " + record.get("Genre").asString());
                    System.out.println();
                }
                return null;
            });

            System.out.println("Cypher queries executed successfully on database: '" + database + "'.");
        } catch (Exception e) {
            System.err.println("Error executing Cypher queries: " + e.getMessage());
        }
    }
    public void collaborativeFiltering() {
        try (Session session = driver.session(SessionConfig.forDatabase(database))) {
            String apocQuery = "MATCH (u1:User {name: \"Guy Davis\"})-[r1:RATED]->(m:Movie)\n" +
                    "MATCH (u2:User {name: \"Misty Williams\"})-[r2:RATED]->(m)\n" +
                    //"RETURN u1 AS User1, u2 AS User2,r1,r2 , m AS Movie\n" +
                    "RETURN u1.name AS User1, u2.name AS User2, r1.rating AS Rating1, r2.rating AS Rating2, m.title AS Movie";

            session.readTransaction(tx -> {
                Result result = tx.run(apocQuery);
                while (result.hasNext()) {
                    Record record = result.next();
                    System.out.println("User1: " + record.get("User1").asString());
                    System.out.println("User2: " + record.get("User2").asString());
                    System.out.println("Rating1: " + record.get("Rating1").asDouble());
                    System.out.println("Rating2: " + record.get("Rating2").asDouble());
                    System.out.println("Movie: " + record.get("Movie").asString());
                    System.out.println();
                }
                return null;
            });

            System.out.println("Cypher queries executed successfully on database: '" + database + "'.");
        } catch (Exception e) {
            System.err.println("Error executing Cypher queries: " + e.getMessage());
        }
    }

    public void contentBasedFiltering() {
        try (Session session = driver.session(SessionConfig.forDatabase(database))) {
            String apocQuery = "MATCH (inception:Movie {title: \"Inception\"})-[r1:IN_GENRE]->(g:Genre)\n" +
                    "MATCH (m:Movie)-[r2:IN_GENRE]->(g)\n" +
                    "WITH m, COLLECT(g.name) AS genres\n" +
                    "RETURN m.title AS movie, genres AS Genres";
                    /*
                    * MATCH (inception:Movie {title: "Inception"})-[r1:IN_GENRE]->(g:Genre)
                    MATCH (m:Movie)-[r2:IN_GENRE]->(g)
                    WITH m, g, r2, COLLECT(g.name) AS genres
                    RETURN m, g, r2
                    LIMIT 50*/
            session.readTransaction(tx -> {
                Result result = tx.run(apocQuery);
                while (result.hasNext()) {
                    Record record = result.next();
                    // Retrieve values from the record
                    String movieTitle = record.get("movie").asString();
                    List<Object> genres = record.get("Genres").asList();

                    // Print the retrieved values
                    System.out.println("Movie: " + movieTitle);
                    System.out.print("Genres: ");
                    for (Object genre : genres) {
                        System.out.print(genre.toString() + ", ");
                    }
                    System.out.println("\n");
                }
                return null;
            });

            System.out.println("Cypher query executed successfully on database: '" + database + "'.");
        } catch (Exception e) {
            System.err.println("Error executing Cypher query: " + e.getMessage());
        }
    }

    //Personalized Recommendations Based on Genres
    public void personalizedRecommendations() {
        try (Session session = driver.session(SessionConfig.forDatabase(database))) {
            String apocQuery = "MATCH (u:User)-[r:RATED]->(watched_movies:Movie)-[:IN_GENRE]->(g:Genre)\n" +
                    "WHERE r.rating > 4.5\n" +
                    "WITH COLLECT(DISTINCT g.name) AS genres\n" +
                    "MATCH (all_movies:Movie)-[:IN_GENRE]->(gm:Genre)\n" +
                    "WHERE gm.name IN genres\n" +
                    //"AND NOT (u)-[:RATED]->(all_movies)\n" +  // Exclude movies already rated by the user
                    "RETURN all_movies.title as suggested_movies";

            session.readTransaction(tx -> {
                Result result = tx.run(apocQuery);
                while (result.hasNext()) {
                    Record record = result.next();
                    // Retrieve values from the record
                    String movieTitle = record.get("suggested_movies").asString();

                    // Print the retrieved values
                    System.out.println("Suggested movie: " + movieTitle);
                }

                return null;
            });

            System.out.println("Cypher query executed successfully on database: '" + database + "'.");
        } catch (Exception e) {
            System.err.println("Error executing Cypher query: " + e.getMessage());
        }
    }

    //                       Weighted Content Algorithm
    // Compute a weighted sum based on the number and types of overlapping traits

    public void recommendationWeightedContent(){
        try (Session session = driver.session(SessionConfig.forDatabase(database))) {
            String apocQuery = "MATCH (u:User{name: \"Omar Huffman\"})-[r:RATED]->(m:Movie)-[:IN_GENRE]->(g:Genre)\n" +
                    "WHERE r.rating > 3\n" +
                    "WITH COLLECT(DISTINCT g.name) AS allGenres, m\n" +
                    "MATCH (A:Person)-[:ACTED_IN]->(m)\n" +
                    "WITH allGenres, COLLECT(DISTINCT A.name) AS allActors, m\n" +
                    "MATCH (D:Person)-[:DIRECTED]->(m)\n" +
                    "WITH allGenres, allActors, COLLECT(DISTINCT D.name) AS allDirectors\n" +
                    "\n" +
                    "MATCH (gm:Movie)\n" +
                    "WITH gm, allGenres, allActors, allDirectors,\n" +
                    "     size([a IN allActors WHERE (gm)<-[:ACTED_IN]-(:Person {name: a})]) * 3 AS actorScore,\n" +
                    "     size([g IN allGenres WHERE (gm)-[:IN_GENRE]->(:Genre {name: g})]) * 5 AS genreScore,\n" +
                    "     size([d IN allDirectors WHERE (gm)<-[:DIRECTED]-(:Person {name: d})]) * 4 AS directorScore\n" +
                    "WITH gm.title AS movie_name, (actorScore + genreScore + directorScore) AS score\n" +
                    "RETURN movie_name, score\n";
                    //+"ORDER BY score DESC";

            session.readTransaction(tx -> {
                Result result = tx.run(apocQuery);
                while (result.hasNext()) {
                    Record record = result.next();
                    // Retrieve values from the record
                    String movieTitle = record.get("movie_name").asString();
                    Double movieScore = record.get("score").asDouble();

                    // System.out.println(record.get("score").asInt());
                    // Print the retrieved values
                    System.out.println("Suggested movie: ''" + movieTitle+ "'' with score:"+ movieScore + ".");
                }

                return null;
            });

            System.out.println("Cypher query executed successfully on database: '" + database + "'.");
        } catch (Exception e) {
            System.err.println("Error executing Cypher query: " + e.getMessage());
        }
    }


    // Content-Based Similarity Metric




    public static void main(String[] args) {
        String uri = "bolt://localhost:7687";
        String user = "neo4j";
        String password = "admin12345"; // replace with your actual password
        String database = "recommendations";
        String filePath = "/all-plain.cypher"; // replace with the actual path to your Cypher file

        Neo4jCypherExecutor executor = new Neo4jCypherExecutor(uri, user, password, database);
        //executor.ensureDatabaseExists();
        //executor.executeCypherFile(filePath);
        //executor.reviewsCount();
        //executor.recommendItems();
        executor.separations();
        //executor.collaborativeFiltering();
        //executor.contentBasedFiltering();
        //executor.personalizedRecommendations();
        executor.recommendationWeightedContent();

        executor.close();
    }
}
