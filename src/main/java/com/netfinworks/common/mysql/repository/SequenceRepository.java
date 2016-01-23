/**
 *
 */
package com.netfinworks.common.mysql.repository;

/**
 * <p>MYSQL 序列仓储</p>
 * @author Zhuxiangyu
 * @version $Id: SequenceRepository.java, v 0.1 2014-4-25 上午9:55:29 zhuxiangyu Exp $
 */
public interface SequenceRepository {

    /**
     * 刷新指定序列队列
     * @param sequenceName
     */
    void flush(String sequenceName);

    /**
     * 获取序列值
     * @param sequenceName
     * @return
     */
    Long next(String sequenceName);

}
