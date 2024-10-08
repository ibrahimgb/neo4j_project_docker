package example;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Procedure;

import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Question1 {

    @Context
    public Transaction tx;

    public static class EntityContainer {
        public String title;
        public long reviews; // Use long because the database stores counts as long

        public EntityContainer(String title, long reviews) {
            this.title = title;
            this.reviews = reviews;
        }
    }

    @Procedure(name = "recommend.howManyReview", mode = Mode.READ)
    public Stream<EntityContainer> howManyReview() {
        String query = "MATCH (m:Movie)<-[:RATED]-(u:User) " +
                "WHERE toLower(m.title) CONTAINS 'matrix' " +
                "WITH m, count(*) AS reviews " +
                "RETURN m.title AS movie, reviews " +
                "ORDER BY reviews DESC LIMIT 5";


            Result result = tx.execute(query);
            Iterable<Map<String, Object>> iterable = () -> result;
            return StreamSupport.stream(iterable.spliterator(), false)
                    .map(row -> new EntityContainer((String) row.get("movie"), (Long) row.get("reviews")));

    }
}
