package example;

import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Procedure;
import org.neo4j.graphdb.Node;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Question6 {

    @Context
    public Transaction tx;


    public static class EntityContainer {
        public String movie_name;
        public long score;

        public EntityContainer(String movie_name, long score) {
            this.movie_name = movie_name;
            this.score = score;
        }
    }
    ////works
    // "Omar Huffman" "Mr. Jason Love" "Angela Thompson"
    @Procedure(name = "recommend.weightedContentAlgorithm", mode = Mode.READ)
    public Stream<EntityContainer> weightedContentAlgorithm() {
        String apocQuery =
                // Collect the genres, actors, and directors for movies rated above 3 by Omar Huffman
                        "MATCH (u:User {name: \"Omar Huffman\"})-[r:RATED]->(m:Movie)-[:IN_GENRE]->(g:Genre) \n" +
                        "WHERE r.rating > 3 \n" +
                        "WITH COLLECT(DISTINCT g.name) AS allGenres, m \n" +
                        "MATCH (m)<-[:ACTED_IN]-(A:Person) \n" +
                        "WITH allGenres, COLLECT(DISTINCT A.name) AS allActors, m \n" +
                        "MATCH (m)<-[:DIRECTED]-(D:Person) \n" +
                        "WITH allGenres, allActors, COLLECT(DISTINCT D.name) AS allDirectors, m \n" +
                        "// Calculate scores for other movies based on the collected genres, actors, and directors \n" +
                        "MATCH (gm:Movie) \n" +
                        "WITH gm, allGenres, allActors, allDirectors, \n" +
                        "     size([a IN allActors WHERE (gm)<-[:ACTED_IN]-(:Person {name: a})]) * 3 AS actorScore, \n" +
                        "     size([g IN allGenres WHERE (gm)-[:IN_GENRE]->(:Genre {name: g})]) * 5 AS genreScore, \n" +
                        "     size([d IN allDirectors WHERE (gm)<-[:DIRECTED]-(:Person {name: d})]) * 4 AS directorScore \n" +
                        "WITH gm.title AS movie_name, (actorScore + genreScore + directorScore) AS score \n" +
                        //"ORDER BY score DESC \n"+
                        "RETURN movie_name, score ";

        Result result = tx.execute(apocQuery);
        Iterable<Map<String, Object>> iterable = () -> result;
        return StreamSupport.stream(iterable.spliterator(), false)
                .map(row -> new EntityContainer(
                        (String) row.get("suggested_movies"),
                        (long) row.get("score")));

    }
}

