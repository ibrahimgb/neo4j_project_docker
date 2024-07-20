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

public class Question5 {

    @Context
    public Transaction tx;


    public static class EntityContainer {
        public String suggested_movies ;

        public EntityContainer(String suggested_movies ) {
            this.suggested_movies=suggested_movies;


        }
    }
    // "Omar Huffman" "Mr. Jason Love" "Angela Thompson"
    @Procedure(name = "recommend4.personalizedRecommendationsBasedOnGenres", mode = Mode.READ)
    public Stream<EntityContainer> personalizedRecommendationsBasedOnGenres () {
        String apocQuery = "MATCH (u:User{name: \"Jessica Sherman\"})-[r:RATED]->(watched_movies:Movie)-[:IN_GENRE]->(g:Genre)\n" +
                "WITH u, COLLECT(DISTINCT g.name) AS genres, COLLECT(watched_movies) AS watched_movies_list\n" +
                "MATCH (all_movies:Movie)-[:IN_GENRE]->(gm:Genre)\n" +
                "WHERE gm.name IN genres AND NOT all_movies IN watched_movies_list\n" +
                "RETURN DISTINCT all_movies.title AS suggested_movies";

        Result result = tx.execute(apocQuery);
        Iterable<Map<String, Object>> iterable = () -> result;
        return StreamSupport.stream(iterable.spliterator(), false)
                .map(row -> new EntityContainer((String) row.get("suggested_movies")));

    }
}

