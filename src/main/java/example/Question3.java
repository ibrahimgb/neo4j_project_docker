package example;

import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.procedure.Context;
import org.neo4j.procedure.Mode;
import org.neo4j.procedure.Procedure;
import org.neo4j.graphdb.Node;
import java.util.Map;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Question3 {

    @Context
    public Transaction tx;


    public static class EntityContainer {
        public Node u ;
        public Node m1 ;
        public Node m2 ;
        public Node m ;

        public EntityContainer(Node u ,Node m1 , Node m2 ,Node m  ) {
            this.u=u;
            this.m1=m1;
            this.m2=m2;
            this.m=m;
        }
    }
    ////Works
// "Toy Story"  "Jumanji"  "Grumpier Old Men"  "Waiting to Exhale"
    @Procedure(name = "recommend.collaborativeFiltering", mode = Mode.READ)
    public Stream<EntityContainer> collaborativeFiltering () {
        String apocQuery = "MATCH (u:User)-[: RATED]->(m1:Movie {title: \"Waiting to Exhale\"})\n" +
                "MATCH (u)-[:RATED]->(m2:Movie {title: \"Jumanji\"})\n" +
                "MATCH (u)-[:RATED]->(m:Movie )\n" +
                "RETURN u, m1, m2, m";

        Result result = tx.execute(apocQuery);
        Iterable<Map<String, Object>> iterable = () -> result;
        return StreamSupport.stream(iterable.spliterator(), false)
                .map(row -> new EntityContainer(
                        (Node) row.get("u"),
                        (Node) row.get("m1"),
                        (Node) row.get("m2"),
                        (Node) row.get("m")));

    }
}
