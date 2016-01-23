/**
 *
 */
package com.netfinworks.common.mysql.repository.exception;

/**
 * <p>注释</p>
 * @author Zhuxiangyu
 * @version $Id: SequenceUpdateException.java, v 0.1 2014-4-25 下午6:40:16 zhuxiangyu Exp $
 */
public class SequenceUpdateException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 7671343143205456067L;

    public SequenceUpdateException(Exception e) {
        super(e);
    }

}
