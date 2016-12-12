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

package org.pentaho.graphmlmerger.model;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

/**
 * Created by rfellows on 12/8/16.
 */
public class MavenDependency {

  private String groupId;
  private String artifactId;
  private String version;
  private String type;
  private String classifier;
  private DependencyType dependencyType;


  public MavenDependency() {
  }

  public MavenDependency( String groupId, String artifactId, String version, String type, String classifier ) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.type = type;
    this.classifier = classifier;
  }

  public MavenDependency( String artifactId ) {
    this( null, artifactId, null, null, null );
  }

  public MavenDependency( String groupId, String artifactId ) {
    this( groupId, artifactId, null, null, null );
  }

  public MavenDependency( String groupId, String artifactId, String version ) {
    this( groupId, artifactId, version, null, null );
  }

  public MavenDependency( String groupId, String artifactId, String version, String type ) {
    this( groupId, artifactId, version, type, null );
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId( String groupId ) {
    this.groupId = groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public void setArtifactId( String artifactId ) {
    this.artifactId = artifactId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion( String version ) {
    this.version = version;
  }

  public String getType() {
    return type;
  }

  public void setType( String type ) {
    this.type = type;
  }

  public String getClassifier() {
    return classifier;
  }

  public void setClassifier( String classifier ) {
    this.classifier = classifier;
  }

  @Override public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append( getGroupId() )
      .append( ":" )
      .append( getArtifactId() )
      .append( ":" )
      .append( getVersion() == null ? "" : getVersion() );

    if ( !Strings.isNullOrEmpty( getType() ) ) {
      sb.append( ":" )
        .append( getType() == null ? "" : getType() );
    }
    if ( !Strings.isNullOrEmpty( getClassifier() ) ) {
      sb.append( ":" )
        .append( getClassifier() == null ? "" : getClassifier() );
    }
    return sb.toString();
  }

  @Override public boolean equals( Object o ) {
    if ( this == o ) {
      return true;
    }
    if ( o == null || getClass() != o.getClass() ) {
      return false;
    }
    MavenDependency that = (MavenDependency) o;
    return Objects.equal( groupId, that.groupId ) &&
      Objects.equal( artifactId, that.artifactId ) &&
      Objects.equal( version, that.version ) &&
      Objects.equal( type, that.type ) &&
      Objects.equal( classifier, that.classifier );
  }

  @Override public int hashCode() {
    return Objects.hashCode( groupId, artifactId, version, type, classifier );
  }

  public DependencyType getDependencyType() {
    return dependencyType;
  }

  public void setDependencyType( DependencyType dependencyType ) {
    this.dependencyType = dependencyType;
  }
}
