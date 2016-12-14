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
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.tinkerpop.api.Neo4jGraphAPI;
import org.neo4j.tinkerpop.api.impl.Neo4jGraphAPIImpl;
import org.pentaho.graphmlmerger.model.DependencyType;
import org.pentaho.graphmlmerger.model.MavenDependency;
import org.pentaho.graphmlmerger.service.DependencyQueryService;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by rfellows on 12/6/16.
 */
public class GraphmlMergerTest {

  @Test
  public void testGraphmlMerger_mergeFile() throws Exception {
    GraphmlMerger graphmlMerger = new GraphmlMerger();
    mergeFile( graphmlMerger );
    graphmlMerger.writeMergedGraph( "target/merged.graphml" );
    graphmlMerger.getGraph().close();
  }

  @Test
  public void testNeo4jGraphmlMerger_mergeFile() throws Exception {
    String graphDbLocation = "target/neo4j/data/databases/graph.db";
    Neo4jGraph neo4jGraph = Neo4jGraph.open( graphDbLocation );
    Neo4JGraphmlMerger neo4jMerger = new Neo4JGraphmlMerger( neo4jGraph );
    mergeFile( neo4jMerger );
    neo4jMerger.getGraph().close();
  }

  private void mergeFile( GraphmlMerger merger ) throws Exception {

    merger.mergeFile( "src/test/resources/deps.graphml" );
    assertEquals( 10, IteratorUtils.count( merger.getGraph().vertices() ) );
    assertEquals( 9, IteratorUtils.count( merger.getGraph().edges() ) );

    merger.mergeFile( "src/test/resources/deps2.graphml" );
    assertEquals( 13, IteratorUtils.count( merger.getGraph().vertices() ) );
    assertEquals( 12, IteratorUtils.count( merger.getGraph().edges() ) );


    merger.mergeFile( "src/test/resources/deps3.graphml" );
    assertEquals( 30, IteratorUtils.count( merger.getGraph().vertices() ) );
    assertEquals( 39, IteratorUtils.count( merger.getGraph().edges() ) );

    DependencyQueryService service = new DependencyQueryService( merger.getGraph() );

    MavenDependency geoCore = new MavenDependency( "pentaho-geo-core" );
    List<MavenDependency> dependencies = service.findDependents( geoCore );

    assertEquals( 1, dependencies.size() );
    dependencies.stream().forEach( dependency -> {
      System.out.println( dependency.toString() + " depends on pentaho-geo-core" );
    } );

    MavenDependency analyzerCore = new MavenDependency( "pentaho-analyzer-core" );
    MavenDependency kettleEngine = new MavenDependency( "pentaho-kettle", "kettle-engine" );

    assertEquals( DependencyType.TRANSITIVE, service.determineDependencyType( analyzerCore, kettleEngine ) );
    assertEquals( DependencyType.NONE, service.determineDependencyType( geoCore, analyzerCore ) );
    assertEquals( DependencyType.DIRECT, service.determineDependencyType( analyzerCore, geoCore ) );

    // multi-level depth transitives
    assertEquals( DependencyType.TRANSITIVE, service.determineDependencyType( analyzerCore, new MavenDependency( "metastore" ) ) );

    // granular requests
    assertEquals( DependencyType.TRANSITIVE, service.determineDependencyType( analyzerCore, new MavenDependency( "pentaho", "pentaho-registry" ) ) );
    assertEquals( DependencyType.NONE, service.determineDependencyType( analyzerCore, new MavenDependency( "pentaho", "pentaho-registry", "6.1-SNAPSHOT" ) ) );
    assertEquals( DependencyType.TRANSITIVE, service.determineDependencyType( analyzerCore, new MavenDependency( "pentaho", "pentaho-registry", "7.1-SNAPSHOT" ) ) );

    dependencies = service.findDependents( kettleEngine, true );
    dependencies.stream().forEach( dependency -> {
      if ( dependency.getDependencyType() == DependencyType.DIRECT ) {
        System.out.println( dependency.toString() + " depends directly on " + kettleEngine.toString() );
      } else {
        System.out.println( dependency.toString() + " depends transitively on " + kettleEngine.toString() );
      }
    } );




  }
}

