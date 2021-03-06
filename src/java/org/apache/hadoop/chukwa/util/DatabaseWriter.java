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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.chukwa.util;


import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.chukwa.inputtools.mdl.DataConfig;

public class DatabaseWriter {
  private static Log log = LogFactory.getLog(DatabaseWriter.class);
  private Connection conn = null;
  private Statement stmt = null;
  private ResultSet rs = null;

  public DatabaseWriter(String host, String user, String password) {
    DataConfig mdlConfig = new DataConfig();
    String jdbc_url = "jdbc:mysql://" + host + "/";
    if (user != null) {
      jdbc_url = jdbc_url + "?user=" + user;
      if (password != null) {
        jdbc_url = jdbc_url + "&password=" + password;
      }
    }
    try {
      // The newInstance() call is a work around for some
      // broken Java implementations
      DriverManagerUtil.loadDriver().newInstance();
    } catch (Exception ex) {
      // handle the error
      log.error(ex, ex);
    }
    try {
      conn = org.apache.hadoop.chukwa.util.DriverManagerUtil.getConnection(jdbc_url);
      log.debug("Initialized JDBC URL: " + jdbc_url);
    } catch (SQLException ex) {
      log.error(ex, ex);
    }
  }

  public DatabaseWriter(String cluster) {
    ClusterConfig cc = new ClusterConfig();
    String jdbc_url = cc.getURL(cluster);
    try {
      // The newInstance() call is a work around for some
      // broken Java implementations
      DriverManagerUtil.loadDriver().newInstance();
    } catch (Exception ex) {
      // handle the error
      log.error(ex, ex);
    }
    try {
      conn = org.apache.hadoop.chukwa.util.DriverManagerUtil.getConnection(jdbc_url);
      log.debug("Initialized JDBC URL: " + jdbc_url);
    } catch (SQLException ex) {
      log.error(ex, ex);
    }
  }

  public void execute(String query) {
    try {
      stmt = conn.createStatement();
      stmt.execute(query);
    } catch (SQLException ex) {
      // handle any errors
      log.error(ex, ex);
      log.error("SQL Statement:" + query);
      log.error("SQLException: " + ex.getMessage());
      log.error("SQLState: " + ex.getSQLState());
      log.error("VendorError: " + ex.getErrorCode());
    } finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException sqlEx) {
          // ignore
        }
        stmt = null;
      }
    }
  }

  public Connection getConnection() {
    return conn;
  }

  public ResultSet query(String query) throws SQLException {
    try {
      stmt = conn.createStatement();
      rs = stmt.executeQuery(query);
    } catch (SQLException ex) {
      // handle any errors
      //only log at debug level because caller will still see exception
      //only log at debug level because caller will still see exception
      log.debug(ex, ex);
      log.debug("SQL Statement:" + query);
      log.debug("SQLException: " + ex.getMessage());
      log.debug("SQLState: " + ex.getSQLState());
      log.debug("VendorError: " + ex.getErrorCode());
      throw ex;
    } finally {
    }
    return rs;
  }

  public void close() {
    // it is a good idea to release
    // resources in a finally{} block
    // in reverse-order of their creation
    // if they are no-longer needed
    if (rs != null) {
      try {
        rs.close();
      } catch (SQLException sqlEx) {
        // ignore
      }
      rs = null;
    }
    if (stmt != null) {
      try {
        stmt.close();
      } catch (SQLException sqlEx) {
        // ignore
      }
      stmt = null;
    }
    if (conn != null) {
      try {
        conn.close();
      } catch (SQLException sqlEx) {
        // ignore
      }
      conn = null;
    }
  }

  public static String formatTimeStamp(long timestamp) {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String format = formatter.format(timestamp);

    return format;
  }
}
