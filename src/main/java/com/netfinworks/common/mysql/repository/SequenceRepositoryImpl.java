/**
 *
 */
package com.netfinworks.common.mysql.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.netfinworks.common.mysql.repository.SequenceRepository;
import com.netfinworks.common.mysql.repository.dao.SequenceDAO;
import com.netfinworks.common.mysql.repository.domain.Sequence;
import com.netfinworks.common.mysql.repository.exception.SequenceUpdateException;

/**
 * <p>序列缓存仓储实现</p>
 * @author Zhuxiangyu
 * @version $Id: SequenceRepositoryImpl.java, v 0.1 2014-4-25 上午10:00:05 zhuxiangyu Exp $
 */
public class SequenceRepositoryImpl implements SequenceRepository, InitializingBean {

    /**
     * 序列DAO
     */
    @Resource(name = "unitySequenceDAO")
    private SequenceDAO                                            sequenceDAO;

    /**
     * 缓存队列
     */
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<Long>> sequenceQueue = new ConcurrentHashMap<String, ConcurrentLinkedQueue<Long>>();

    /**
     * 刷新锁
     */
    private ConcurrentHashMap<String, ReentrantLock>               locks         = new ConcurrentHashMap<String, ReentrantLock>();

    /**
     * 计数器
     */
    private ConcurrentHashMap<String, AtomicInteger>               counts        = new ConcurrentHashMap<String, AtomicInteger>();

    /**
     * 异步刷新线程池
     */
    @Resource(name = "unitySequenceFlushTreadPoolTaskExecutor")
    private ThreadPoolTaskExecutor                                 threadPoolTaskExecutor;

    /**
     * 阀值
     */
    private Map<String, Integer>                                   thresholds    = new HashMap<String, Integer>();

    /**
     * 总数
     */
    private Map<String, Integer>                                   totals        = new HashMap<String, Integer>();

    /**
     * 事务模板
     */
    @Resource(name = "unitySequenceTransactionTemplate")
    private TransactionTemplate                                    transactionTemplate;

    /* (non-Javadoc)
     * @see com.netfinworks.repository.SequenceRepository#flush(java.util.List)
     */
    @Override
    public void flush(final String sequenceName) {

        //  检查阀值
        if (overThreshold(sequenceName)) {

            //  尝试获取锁
            //  若返回false则已被锁定，当前线程则不做操作
//            if (locks.get(sequenceName).tryLock()) {
                try {
                    locks.get(sequenceName).lock();
                    //  double check
                    if (overThreshold(sequenceName)) {
                        try {
                            //  刷新缓冲区
                            flushBuffer(sequenceName);

                        } catch (SequenceUpdateException e) {
                            //  更新异常则异步线程更新
                            asyncFlush(sequenceName);
                        }
                    }
                } finally {
                    //  释放锁
                    locks.get(sequenceName).unlock();
                }
//            }
        }

    }

    /**
     * 刷新缓冲区
     * @param sequenceName
     */
    private void flushBuffer(final String sequenceName) {

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus arg0) {
                //  锁取
                Sequence sequence = sequenceDAO.lock(sequenceName);
                if(sequence == null){
                    throw new RuntimeException("序列"+sequenceName+"没有初始化.");
                }
                if(thresholds.get(sequence.getName()) == null) {
                    //  置阀值
                    thresholds.put(sequence.getName(), sequence.getThreshold());
                }
                if(totals.get(sequence.getName()) == null) {
                    //  设总数
                    totals.put(sequence.getName(), sequence.getTotal());
                }
                if(locks.get(sequence.getName()) == null) {
                    //  初始化锁队列
                    locks.put(sequence.getName(), new ReentrantLock());
                }
                //  计算偏移量
                int increment = sequence.getIncrement();
                int total = sequence.getTotal();
                Integer offset =  increment * total;
                //  获得最大值
                Long maxValue = (sequence.getMaxValue() == null || sequence.getMaxValue() <=0)
                        ?Long.MAX_VALUE:sequence.getMaxValue();
                //  计算更新值
                Long beforeValue = sequence.getCurrentValue();
                Long afterValue = beforeValue + offset;
                while(afterValue > maxValue){
                    afterValue = afterValue - maxValue;
                }
                //  更新
                try {
                    sequenceDAO.update(sequenceName, beforeValue, afterValue);
                } catch (Exception e) {
                    //  更新失败抛出异常准备重试
                    throw new SequenceUpdateException(e);
                }

                //  设置更新后值
//                sequence.setAfterValue(afterValue);

                //  创建新的队列
                ConcurrentLinkedQueue<Long> newQueue = new ConcurrentLinkedQueue<Long>();

                //  设值
//                for (long seq = beforeValue; seq < afterValue; seq = seq + sequence.getIncrement().longValue()) {
//                    sequenceQueue.get(sequenceName).add(seq);
//                }
                for (int i = 0; i < total; i++) {
                    newQueue.add(beforeValue);
                    beforeValue += increment;
                    if(beforeValue > maxValue){
                        beforeValue = beforeValue - maxValue;
                    }
                }
                //  清空数据，初始化
                sequenceQueue.put(sequenceName, newQueue);
                //  计数器初始化
                counts.put(sequenceName, new AtomicInteger(0));

            }

        });
    }

    /* (non-Javadoc)
     * @see com.netfinworks.repository.SequenceRepository#get(java.lang.String)
     */
    @Override
    public Long next(final String sequenceName) {
        ConcurrentLinkedQueue<Long> queue = sequenceQueue.get(sequenceName);
        if(queue == null){
            flushBuffer(sequenceName);
            queue = sequenceQueue.get(sequenceName);
        }
        Long sequence = queue.poll();

        //  取到了值
        if (sequence != null) {

            //  计数增长
            counts.get(sequenceName).incrementAndGet();

            //  若达到刷新阀值,异步刷新
            if (overThreshold(sequenceName)) {
                asyncFlush(sequenceName);
            }

            return sequence;
        }

        //  若队列取空
        try {
            //  立即刷新
            flush(sequenceName);
            //  重新抓取
            return next(sequenceName);
        } catch (Exception e) {
            //  目前失败了递归重新取
            return next(sequenceName);
        }

    }

    /**
     * 是否越过刷新阀值
     * @param sequenceName
     * @return
     */
    private boolean overThreshold(final String sequenceName) {
        return counts.get(sequenceName).intValue() >= totals.get(sequenceName) - thresholds.get(sequenceName);
    }

    /**
     * 异步刷新
     * 有较小几率线程池耗尽导致刷新任务丢失
     * @param sequenceName
     */
    private void asyncFlush(final String sequenceName) {

        try {

            threadPoolTaskExecutor.execute(new Runnable() {

                @Override
                public void run() {

                    flush(sequenceName);
                }
            });
        } catch (TaskRejectedException e) {
            // 捕获线程池拒绝异常
            // 不作处理

        }

    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {

        //  初始化queue map
        if (MapUtils.isEmpty(sequenceQueue)) {
            //  获取序列列表
            List<Sequence> sequenceList = sequenceDAO.loadAll();

            for (Sequence sequence : sequenceList) {

                //  置阀值
                thresholds.put(sequence.getName(), sequence.getThreshold());

                //  设总数
                totals.put(sequence.getName(), sequence.getTotal());

                //  初始化锁队列
                locks.put(sequence.getName(), new ReentrantLock());

                //  更新缓冲区
                flushBuffer(sequence.getName());

            }
        }

        System.out.println("-----------------");
    }

}
