/*
 * ******************************************************************************
 *
 * Copyright (C) 2002-2016 by Pentaho : http://www.pentaho.com
 *
 * ******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pentaho.graphmlmerger.util;

import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jGraph;
import org.apache.tinkerpop.gremlin.neo4j.structure.Neo4jVertex;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;

import java.util.Optional;

/**
 * Created by rfellows on 12/13/16.
 */
public class Neo4JGraphmlMerger extends GraphmlMerger {

  private Neo4jGraph graph;

  public Neo4JGraphmlMerger( Neo4jGraph graph ) {
    super( graph );
    this.graph = graph;
  }

  @Override
  protected Vertex mergeVertex( Vertex originalVertex ) throws IllegalArgumentException {

    boolean exists = getGraph().traversal().V().has( "_id", originalVertex.id() ).hasNext();

    if ( !exists ) {
      graph.tx().readWrite();

      /*
      Unfortunately I had to resort to Neo4J specific code to get the graph verticies to have a "Label" applied to them.
      Label's are Neo4J's way of classifying node types. In the cypher query below, I create a node Label of Artifact
      with all of the needed properties on it.
      */
      String cypherQuery = "create (dep:Artifact { _id: \"" + originalVertex.id()
        + "\", artifactId: \"" + originalVertex.property( "artifactId" ).value()
        + "\", groupId: \"" + originalVertex.property( "groupId" ).value()
        + "\", version: \"" + originalVertex.property( "version" ).value() + "\" }) return dep";

      GraphTraversal<Object, Object> cypher = graph.cypher( cypherQuery );
      Optional<Object> v = IteratorUtils.stream( cypher ).findFirst();
      graph.tx().commit();
      if ( v.isPresent() && v.get() instanceof Neo4jVertex ) {
        return (Neo4jVertex) v.get();
      } else {
        return null;
      }
    }
    throw new IllegalArgumentException( "Vertex already exists in the graph" );

  }

  @Override
  protected Edge mergeEdge( Edge originalEdge ) throws IllegalArgumentException {
    Vertex out = getGraph().traversal().V().has( "_id", originalEdge.outVertex().id() ).next();
    Vertex in = getGraph().traversal().V().has( "_id", originalEdge.inVertex().id() ).next();
    boolean exists = IteratorUtils.count( getGraph().traversal().E().has( "_id", originalEdge.id() ) ) > 0;

    if ( !exists ) {
      graph.tx().readWrite();
      Edge edge = out.addEdge( originalEdge.label(), in, "_id", originalEdge.id() );
      graph.tx().commit();
      return edge;
    }
    throw new IllegalArgumentException( "Edge already exists in the graph" );

  }


}
