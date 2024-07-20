package example;

import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Procedure;

import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Question2 {

    @Context
    public Transaction tx;


    public static class EntityContainer {
        public String Actor1;
        public   String Actor2;
        public String Director;
        public String Movie;
        public String Genre;


        public EntityContainer(String Actor1,  String Actor2, String Director, String Movie, String Genre ) {
            this.Actor1=Actor1;
            this.Actor2=Actor2;
            this.Director=Director;
            this.Movie=Movie;
            this.Genre=Genre;
        }
    }
////works!
    @Procedure(name = "recommend.recommendItems", mode = Mode.READ)
    public Stream<EntityContainer> recommendItems() {
        String apocQuery = "MATCH (joe:Actor {name: \"Joe Pesci\"})-[r1:ACTED_IN]->(m:Movie)\n" +
                "MATCH (robert:Actor {name: \"Robert De Niro\"})-[r2:ACTED_IN]->(m)\n" +
                "MATCH (martin:Director {name: \"Martin Scorsese\"})-[r4:DIRECTED]->(m)\n" +
                "MATCH (m)-[r3:IN_GENRE]->(genre:Genre)\n" +
                "RETURN joe.name AS Actor1, robert.name AS Actor2, martin.name AS Director, m.title AS Movie, genre.name AS Genre";

        Result result = tx.execute(apocQuery);
        Iterable<Map<String, Object>> iterable = () -> result;
        return StreamSupport.stream(iterable.spliterator(), false)
                .map(row -> new EntityContainer(
                        (String) row.get("Actor1"),
                        (String) row.get("Actor2"),
                        (String) row.get("Director"),
                        (String) row.get("Movie"),
                        (String) row.get("Genre")));


    }
}
