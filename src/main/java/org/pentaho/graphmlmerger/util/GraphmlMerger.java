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

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.apache.tinkerpop.gremlin.structure.io.graphml.GraphMLReader;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * Created by rfellows on 12/6/16.
 */
public class GraphmlMerger {

  private Graph graph;

  public GraphmlMerger() {
    this.graph = TinkerGraph.open();
  }

  public GraphmlMerger( Graph graph ) {
    this.graph = graph;
  }

  public void mergeFile( String graphmlFile ) throws FileNotFoundException {
    GraphMLReader reader = GraphMLReader.build()
      .vertexLabelKey( "_name" )
      .edgeLabelKey( "_label" )
      .strict( false )
      .batchSize( 1024 )
      .create();

    Graph tmpGraph = TinkerGraph.open();
    FileInputStream fis = new FileInputStream( graphmlFile );
    try {
      reader.readGraph( fis, tmpGraph );

      // add all of the vertices
      Iterator<Vertex> vertexIterator = tmpGraph.vertices();
      IteratorUtils.stream( vertexIterator ).forEach( vertex -> {
        try {
          mergeVertex( vertex );
        } catch ( IllegalArgumentException e ) {
          // vertex is already there, ignore this error
          System.out.println( e.getMessage() );
        }
      } );

      Iterator<Edge> edgeIterator = tmpGraph.edges();
      IteratorUtils.stream( edgeIterator ).forEach( edge -> {
        try {
          mergeEdge( edge );
        } catch ( IllegalArgumentException e ) {
          // edge is already there, ignore it
          System.out.println( e.getMessage() );
        }
      } );

    } catch ( IOException e ) {
      e.printStackTrace();
    }
  }

  protected Vertex mergeVertex( Vertex originalVertex ) throws IllegalArgumentException {
    return getGraph().addVertex( T.id, originalVertex.id(),
      T.label, originalVertex.label(),
      "groupId", originalVertex.property( "groupId" ).value(),
      "artifactId", originalVertex.property( "artifactId" ).value(),
      "version", originalVertex.property( "version" ).value()
    );
  }

  protected Edge mergeEdge( Edge originalEdge ) throws IllegalArgumentException {
    Vertex out = getGraph().traversal().V( originalEdge.outVertex().id() ).next();
    Vertex in = getGraph().traversal().V( originalEdge.inVertex().id() ).next();
    return out.addEdge( originalEdge.label(), in, T.id, originalEdge.id() );
  }

  protected Graph getGraph() {
    return graph;
  }

  public void writeMergedGraph( String outputFile ) throws IOException {
    try ( final OutputStream os = new FileOutputStream( outputFile ) ) {
      graph.io( IoCore.graphml() ).writer()
        .normalize( true )
        .edgeLabelKey( "_label" )
        .vertexLabelKey( "_name" )
        .create().writeGraph( os, graph );
    }
  }
}
