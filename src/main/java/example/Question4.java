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

public class Question4 {

    @Context
    public Transaction tx;


    public static class EntityContainer {
        public List<String> Genres ;

        public String movie ;

        public EntityContainer(String movie, List<String> Genres ) {
            this.movie=movie;
            this.Genres=Genres;

        }
    }
    // "Toy Story"  "Jumanji"  "Grumpier Old Men"  "Waiting to Exhale"
    @Procedure(name = "recommend3.similarityBasedOnCommonGenres", mode = Mode.READ)
    public Stream<EntityContainer> similarityBasedOnCommonGenres () {
        String apocQuery = "MATCH (inception:Movie {title: \"Inception\"})-[r1:IN_GENRE]->(g:Genre)\n" +
                "MATCH (m:Movie)-[r2:IN_GENRE]->(g)\n" +
                "WITH m, COLLECT(g.name) AS genres\n" +
                "RETURN  m.title AS movie, genres AS Genres";

        Result result = tx.execute(apocQuery);
        Iterable<Map<String, Object>> iterable = () -> result;
        return StreamSupport.stream(iterable.spliterator(), false)
                .map(row -> new EntityContainer(
                        (String) row.get("movie"),
                        (List<String>) row.get("Genres")));

    }
}

