/**
 *
 */
package com.netfinworks.common.mysql.repository.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.netfinworks.common.mysql.repository.domain.Sequence;

/**
 * <p>注释</p>
 * @author Zhuxiangyu
 * @version $Id: SequenceDAOImpl.java, v 0.1 2014-4-25 上午11:19:27 zhuxiangyu Exp $
 */
public class SequenceDAOImpl extends JdbcDaoSupport implements SequenceDAO {

    private RowMapper<Sequence> rowMapper = new RowMapper<Sequence>() {

                                              @Override
                                              public Sequence mapRow(ResultSet paramResultSet, int paramInt)
                                                                                                            throws SQLException {

                                                  Sequence sequence = new Sequence();
                                                  sequence.setName(paramResultSet.getString("NAME"));
                                                  sequence.setCurrentValue(paramResultSet.getLong("CURRENT_VALUE"));
                                                  sequence.setIncrement(paramResultSet.getInt("INCREMENT"));
                                                  sequence.setTotal(paramResultSet.getInt("TOTAL"));
                                                  sequence.setThreshold(paramResultSet.getInt("THRESHOLD"));
                                                  for(int i = 1;i <= paramResultSet.getMetaData().getColumnCount();i++){
                                                      if("MAX_VALUE".equals(paramResultSet.getMetaData().getColumnName(i))){
                                                          sequence.setMaxValue(paramResultSet.getLong("MAX_VALUE"));
                                                          break;
                                                      }
                                                  }
                                                  return sequence;
                                              }

                                          };

    private static final String QUERY_ALL = " select * from T_SEQUENCE ";

    private static final String LOCK_BY   = " select * from T_SEQUENCE where NAME = ? for update ";

    private static final String UPDATE    = " update T_SEQUENCE set CURRENT_VALUE = ? where CURRENT_VALUE = ? and NAME = ? ";

    @Override
    public List<Sequence> loadAll() {
        return getJdbcTemplate().query(QUERY_ALL, rowMapper);
    }

    @Override
    public Sequence lock(String sequenceName) {
        return getJdbcTemplate().queryForObject(LOCK_BY, new Object[] { sequenceName }, rowMapper);
    }

    @Override
    public void update(String sequenceName, Long beforeValue, Long afterValue) {
        getJdbcTemplate().update(UPDATE, afterValue, beforeValue, sequenceName);
    }
}
