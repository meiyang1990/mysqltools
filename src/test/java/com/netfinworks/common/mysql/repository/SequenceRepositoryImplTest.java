/**
 *
 */
package com.netfinworks.common.mysql.repository;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.locks.ReentrantLock;

/**
 * <p>注释</p>
 * @author Zhuxiangyu
 * @version $Id: SequenceRepositoryImplTest.java, v 0.1 2014-4-29 下午1:52:00 zhuxiangyu Exp $
 */
//@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/META-INF/spring/netfinworks-common-mysql-repository.xml",
                                   "/spring/persistence-test.xml" })
public class SequenceRepositoryImplTest {

    @Resource(name = "unitySequenceRepository")
    private static SequenceRepository sequenceRepository;

    /**
     * test_seq	1	2	10	0	15
     * @throws Exception
     */
    @Test
    public void test() throws Exception {
//        for(int i = 0; i<20;i++) {
//            Long next = sequenceRepository.next("test_seq");
//            Thread.sleep(1000);
//            System.out.println(next);
//        }
        ClassPathXmlApplicationContext ac;
        ac = new ClassPathXmlApplicationContext(new String[] { "/spring/persistence-test.xml",
                "/META-INF/spring/netfinworks-common-mysql-repository.xml"
        });
        sequenceRepository = (SequenceRepository)ac.getBean("unitySequenceRepository");
        Long next = sequenceRepository.next("test_seq");
        System.out.println(next);
        for(int i = 0; i<20;i++) {
            next = sequenceRepository.next("test_seq");
            Thread.sleep(1000);
            System.out.println(next);
        }
    }

    public static void main(String... args){
        final ReentrantLock lock = new ReentrantLock();
        ClassPathXmlApplicationContext ac;
        ac = new ClassPathXmlApplicationContext(new String[] { "/spring/persistence-test.xml",
                "/META-INF/spring/netfinworks-common-mysql-repository.xml"
                 });
        sequenceRepository = (SequenceRepository)ac.getBean("unitySequenceRepository");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while(true){
//                    if(lock.tryLock()) {
//                        try {
//                            System.out.println(Thread.currentThread().toString()+"获得锁");
//                        }finally {
//                            lock.unlock();
//                        }
//                    }else{
//                        System.out.println(Thread.currentThread().toString()+"没有获得锁");
//                    }
//                    try {
//                        Thread.sleep(1000l);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
                    Long next = sequenceRepository.next("seq_friend_id");
                    System.out.println(Thread.currentThread().getName() + next);

                }
            }
        };
        Thread t1 = new Thread(runnable);
        Thread t2 = new Thread(runnable);
        t1.start();
        t2.start();
    }

}
