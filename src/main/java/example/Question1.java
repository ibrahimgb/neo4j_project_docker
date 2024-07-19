package example;

import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Procedure;

import java.util.stream.Stream;

/**
 * This is an example returning {@link org.neo4j.graphdb.Entity Entities} from stored procedures.
 * {@link org.neo4j.graphdb.Node Nodes} and {@link org.neo4j.graphdb.Relationship relationships} are both entities
 * and can only be accessed in their transaction. So it is important that you use the injected one
 * and not open a new one; otherwise you can access them from the outside.
 */
public class Question1 {

    @Context
    public Transaction tx;

    public record EntityContainer(String title, int reviews) {
    }

    @Procedure(name = "recommend.howManyReview", mode = Mode.READ)
    public Stream<EntityContainer> howManyReview() {
        String apocQuery = "MATCH (m:Movie)<-[:RATED]-(u:User)\n" +
                "WHERE toLower(m.title) CONTAINS 'matrix'\n" +
                "WITH m, count(*) AS reviews\n" +
                "RETURN m.title AS movie, reviews\n" +
                "ORDER BY reviews DESC LIMIT 5;";

        Result result = tx.execute(apocQuery);
        return result.stream()
                .map(row -> new EntityContainer((String) row.get("movie"), ((Long) row.get("reviews")).intValue()));
    }
}
