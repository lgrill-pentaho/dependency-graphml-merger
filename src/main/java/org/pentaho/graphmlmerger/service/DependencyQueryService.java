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

package org.pentaho.graphmlmerger.service;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;
import org.pentaho.graphmlmerger.model.DependencyType;
import org.pentaho.graphmlmerger.model.MavenDependency;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.in;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.inE;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;

/**
 * Created by rfellows on 12/8/16.
 */
public class DependencyQueryService {

  private Graph graph;

  public DependencyQueryService( Graph graph ) {
    this.graph = graph;
  }

  /**
   * Finds all direct downstream dependent projects (Those projects that resolve the one in question)
   * @param dependency
   * @return
   */
  public List<MavenDependency> findDependents( MavenDependency dependency ) {
    return findDependents( dependency, false );
  }

  public List<MavenDependency> findDependents( MavenDependency dependency, boolean includeTransitives ) {
    final Set<MavenDependency> tmp = new HashSet<>();

    GraphTraversal<Vertex, Vertex> traversal = identifyDependency( dependency );

    // add all of the direct dependencies
    traversal.in()
      .forEachRemaining( vertex -> tmp.add( convertVertex( vertex ) ) );

    if ( includeTransitives ) {
      // set up the traversal again
      traversal = identifyDependency( dependency );

      // get all of the transitive dependencies
      traversal.in()
        .repeat( in() )
        .until( inE().count().is( 0 ) )
        .emit()
        .forEachRemaining( vertex -> tmp.add( convertVertex( vertex, true ) ) );
    }

    return Lists.newArrayList( tmp );
  }

  private GraphTraversal<Vertex, Vertex> identifyDependency( MavenDependency dependency ) {
    GraphTraversal<Vertex, Vertex> traversal = graph.traversal().V().has( "artifactId", dependency.getArtifactId() );

    if ( !Strings.isNullOrEmpty( dependency.getGroupId() ) ) {
      traversal = traversal.has( "groupId", dependency.getGroupId() );
    }

    if ( !Strings.isNullOrEmpty( dependency.getVersion() ) ) {
      traversal = traversal.has( "version", dependency.getVersion() );
    }

    if ( !Strings.isNullOrEmpty( dependency.getType() ) ) {
      traversal = traversal.has( "type", dependency.getType() );
    }

    if ( !Strings.isNullOrEmpty( dependency.getClassifier() ) ) {
      traversal = traversal.has( "classifier", dependency.getClassifier() );
    }
    return traversal;
  }

  private MavenDependency convertVertex( Vertex vertex ) {
    return convertVertex( vertex, false );
  }

  private MavenDependency convertVertex( Vertex vertex, boolean isTransitive ) {
    MavenDependency md = new MavenDependency();
    md.setArtifactId( getStringProperty( vertex, "artifactId" ) );
    md.setGroupId( getStringProperty( vertex, "groupId" ) );
    md.setVersion( getStringProperty( vertex, "version" ) );
    md.setType( getStringProperty( vertex, "type" ) );
    md.setClassifier( getStringProperty( vertex, "classifier" ) );
    md.setDependencyType( isTransitive ? DependencyType.TRANSITIVE : DependencyType.DIRECT );
    return md;
  }

  private String getStringProperty( Vertex v, String propertyKey ) {
    VertexProperty<Object> prop = v.property( propertyKey );
    if ( prop != null && prop.isPresent() && prop.value() != null ) {
      return prop.value().toString();
    } else {
      return null;
    }
  }

  public DependencyType determineDependencyType( MavenDependency source, MavenDependency target ) {
    assert ( source != null );
    assert ( source.getArtifactId() != null );

    GraphTraversal<Vertex, Vertex> sourceTraversal = identifyDependency( source );
    Long pathLength;
    try {
      pathLength = IteratorUtils.stream(
        sourceTraversal
          .repeat( out().simplePath() )
          .until( vertexTraverser -> {
            // do they match?
            return logicallyEqual( target, vertexTraverser.get() );
          } )
          .limit( 1 )
          .path()
          .count( Scope.local )
      ).findAny().get();
    } catch ( NoSuchElementException e ) {
      return DependencyType.NONE;
    }

    switch( pathLength.intValue() ) {
      case 2:
        return DependencyType.DIRECT;
      case 0:
      case 1:
        return DependencyType.NONE;
      default:
        return DependencyType.TRANSITIVE;
    }

  }

  protected boolean logicallyEqual( MavenDependency dependency, Vertex vertex ) {
    boolean match = dependency.getArtifactId().equals( getStringProperty( vertex, "artifactId" ) );

    if ( !Strings.isNullOrEmpty( dependency.getGroupId() ) ) {
      match = match && dependency.getGroupId().equals( getStringProperty( vertex, "groupId" ) );
    }
    if ( !Strings.isNullOrEmpty( dependency.getVersion() ) ) {
      match = match && dependency.getVersion().equals( getStringProperty( vertex, "version" ) );
    }
    if ( !Strings.isNullOrEmpty( dependency.getType() ) ) {
      match = match && dependency.getType().equals( getStringProperty( vertex, "type" ) );
    }
    if ( !Strings.isNullOrEmpty( dependency.getClassifier() ) ) {
      match = match && dependency.getClassifier().equals( getStringProperty( vertex, "classifier" ) );
    }

    return match;

  }


}
