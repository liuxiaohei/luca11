package org.ld.jcl;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by leon on 11/1/17.
 *
 * 必须创建 driver shim 的原因: http://www.kfu.com/~nsayer/Java/dyn-jdbc.html
 */
public class DriverShim implements Driver{
    private Driver driver;

    public DriverShim(Driver d) {
        this.driver = d;
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        return this.driver.connect(url, info);
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return this.driver.acceptsURL(url);
    }


    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return this.driver.getPropertyInfo(url, info);
    }

    @Override
    public int getMajorVersion() {
        return this.driver.getMajorVersion();
    }

    @Override
    public int getMinorVersion() {
        return this.driver.getMinorVersion();
    }

    @Override
    public boolean jdbcCompliant() {
        return this.driver.jdbcCompliant();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return this.driver.getParentLogger();
    }
}
