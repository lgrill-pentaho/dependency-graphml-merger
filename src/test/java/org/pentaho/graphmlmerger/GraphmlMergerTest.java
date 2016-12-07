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

package org.pentaho.graphmlmerger;

import org.apache.tinkerpop.gremlin.util.iterator.IteratorUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by rfellows on 12/6/16.
 */
public class GraphmlMergerTest {

  GraphmlMerger merger;

  @Before
  public void setUp() throws Exception {
    merger = new GraphmlMerger();
  }

  @Test
  public void mergeFile() throws Exception {
    merger.mergeFile( "src/test/resources/deps.graphml" );
    assertEquals( 10, IteratorUtils.count( merger.getGraph().vertices() ) );
    assertEquals( 9, IteratorUtils.count( merger.getGraph().edges() ) );

    merger.mergeFile( "src/test/resources/deps2.graphml" );
    assertEquals( 13, IteratorUtils.count( merger.getGraph().vertices() ) );
    assertEquals( 12, IteratorUtils.count( merger.getGraph().edges() ) );


    merger.mergeFile( "src/test/resources/deps3.graphml" );
    assertEquals( 30, IteratorUtils.count( merger.getGraph().vertices() ) );
    assertEquals( 39, IteratorUtils.count( merger.getGraph().edges() ) );

    merger.writeMergedGraph( "target/merged.graphml" );
  }

}