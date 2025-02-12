/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.iotdb.db.integration;

import org.apache.iotdb.integration.env.EnvFactory;
import org.apache.iotdb.itbase.category.ClusterTest;
import org.apache.iotdb.itbase.category.LocalStandaloneTest;
import org.apache.iotdb.itbase.category.RemoteTest;
import org.apache.iotdb.jdbc.IoTDBSQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category({LocalStandaloneTest.class, ClusterTest.class, RemoteTest.class})
public class IoTDBDeleteStorageGroupIT {

  @Before
  public void setUp() throws Exception {
    EnvFactory.getEnv().initBeforeTest();
  }

  @After
  public void tearDown() throws Exception {
    EnvFactory.getEnv().cleanAfterTest();
  }

  @Test
  public void testDeleteStorageGroup() throws Exception {
    try (Connection connection = EnvFactory.getEnv().getConnection();
        Statement statement = connection.createStatement()) {
      statement.execute("SET STORAGE GROUP TO root.ln.wf01.wt01");
      statement.execute("SET STORAGE GROUP TO root.ln.wf01.wt02");
      statement.execute("SET STORAGE GROUP TO root.ln.wf01.wt03");
      statement.execute("SET STORAGE GROUP TO root.ln.wf01.wt04");
      statement.execute("DELETE STORAGE GROUP root.ln.wf01.wt01");
      boolean hasResult = statement.execute("SHOW STORAGE GROUP");
      assertTrue(hasResult);
      String[] expected =
          new String[] {"root.ln.wf01.wt02", "root.ln.wf01.wt03", "root.ln.wf01.wt04"};
      List<String> expectedList = new ArrayList<>();
      Collections.addAll(expectedList, expected);
      List<String> result = new ArrayList<>();
      try (ResultSet resultSet = statement.getResultSet()) {
        while (resultSet.next()) {
          result.add(resultSet.getString(1));
        }
      }
      assertEquals(expected.length, result.size());
      assertTrue(expectedList.containsAll(result));
    }
  }

  @Test
  public void testDeleteMultipleStorageGroupWithQuote() throws Exception {
    try (Connection connection = EnvFactory.getEnv().getConnection();
        Statement statement = connection.createStatement()) {
      statement.execute("SET STORAGE GROUP TO root.ln1.wf01.wt01");
      statement.execute("SET STORAGE GROUP TO root.ln1.wf01.wt02");
      statement.execute("SET STORAGE GROUP TO root.ln1.wf02.wt03");
      statement.execute("SET STORAGE GROUP TO root.ln1.wf02.wt04");
      statement.execute("DELETE STORAGE GROUP root.ln1.wf01.wt01, root.ln1.wf02.wt03");
      boolean hasResult = statement.execute("SHOW STORAGE GROUP");
      assertTrue(hasResult);
      String[] expected = new String[] {"root.ln1.wf01.wt02", "root.ln1.wf02.wt04"};
      List<String> expectedList = new ArrayList<>();
      Collections.addAll(expectedList, expected);
      List<String> result = new ArrayList<>();
      try (ResultSet resultSet = statement.getResultSet()) {
        while (resultSet.next()) {
          result.add(resultSet.getString(1));
        }
      }
      assertEquals(expected.length, result.size());
      assertTrue(expectedList.containsAll(result));
    }
  }

  @Test(expected = IoTDBSQLException.class)
  public void deleteNonExistStorageGroup() throws Exception {
    try (Connection connection = EnvFactory.getEnv().getConnection();
        Statement statement = connection.createStatement()) {
      statement.execute("SET STORAGE GROUP TO root.ln2.wf01.wt01");
      statement.execute("DELETE STORAGE GROUP root.ln2.wf01.wt02");
    }
  }

  @Test
  public void testDeleteStorageGroupWithStar() throws Exception {
    try (Connection connection = EnvFactory.getEnv().getConnection();
        Statement statement = connection.createStatement()) {
      statement.execute("SET STORAGE GROUP TO root.ln3.wf01.wt01");
      statement.execute("SET STORAGE GROUP TO root.ln3.wf01.wt02");
      statement.execute("SET STORAGE GROUP TO root.ln3.wf02.wt03");
      statement.execute("SET STORAGE GROUP TO root.ln3.wf02.wt04");
      statement.execute("DELETE STORAGE GROUP root.ln3.wf02.*");
      boolean hasResult = statement.execute("SHOW STORAGE GROUP");
      assertTrue(hasResult);
      String[] expected = new String[] {"root.ln3.wf01.wt01", "root.ln3.wf01.wt02"};
      List<String> expectedList = new ArrayList<>();
      Collections.addAll(expectedList, expected);
      List<String> result = new ArrayList<>();
      try (ResultSet resultSet = statement.getResultSet()) {
        while (resultSet.next()) {
          result.add(resultSet.getString(1));
        }
      }
      assertEquals(expected.length, result.size());
      assertTrue(expectedList.containsAll(result));
    }
  }

  @Test
  public void testDeleteAllStorageGroups() throws Exception {
    try (Connection connection = EnvFactory.getEnv().getConnection();
        Statement statement = connection.createStatement()) {
      statement.execute("SET STORAGE GROUP TO root.ln4.wf01.wt01");
      statement.execute("SET STORAGE GROUP TO root.ln4.wf01.wt02");
      statement.execute("SET STORAGE GROUP TO root.ln4.wf02.wt03");
      statement.execute("SET STORAGE GROUP TO root.ln4.wf02.wt04");
      statement.execute("DELETE STORAGE GROUP root.**");
      boolean hasResult = statement.execute("SHOW STORAGE GROUP");
      assertTrue(hasResult);
      List<String> result = new ArrayList<>();
      try (ResultSet resultSet = statement.getResultSet()) {
        while (resultSet.next()) {
          result.add(resultSet.getString(1));
        }
      }
      assertEquals(0, result.size());
    }
  }
}
