/**
 *
 */
package com.netfinworks.common.mysql.repository.dao;

import java.util.List;

import com.netfinworks.common.mysql.repository.domain.Sequence;

/**
 * <p>序列DAO</p>
 * @author Zhuxiangyu
 * @version $Id: SequenceDAO.java, v 0.1 2014-4-25 上午10:00:41 zhuxiangyu Exp $
 */
public interface SequenceDAO {

    List<Sequence> loadAll();

    Sequence lock(String sequenceName);

    void update(String sequenceName, Long beforeValue, Long afterValue);

}
