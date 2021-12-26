package nmj.rule;

import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;

public class RuleTest {

    @Test
    public void test() throws Exception {
        RuleClassTest ruleClassTest = new RuleClassTest2();
        RuleContext<Suit> suitRuleContext = RuleContext.create(ruleClassTest, (m) -> {
            m.setBrand2("er");
        });
        final String brand1 = suitRuleContext.getOrNull("brand");
        System.out.println(brand1);
        Suit modelProxy = suitRuleContext.getProxy();
        String brand = modelProxy.getBrand();
        System.out.println(brand);
    }

    @Test
    public void tes3t() throws Exception {
        ConcurrentHashMap<Object, Object> hashMap = new ConcurrentHashMap<>();
        long t = System.currentTimeMillis();
        hashMap.computeIfAbsent("23", (k) -> {
            return k;
        });
        long t1 = System.currentTimeMillis();
        hashMap.computeIfAbsent("233", (k) -> {
            return k.toString() + 3;
        });
        long t2 = System.currentTimeMillis();
        hashMap.computeIfAbsent("253", (k) -> {
            return k.toString() + 5;
        });
        // 第一次访问10ms左右
        System.out.println((t1 - t));
        long t3 = System.currentTimeMillis();
        // 第一次访问10ms左右
        System.out.println((t2 - t1));
        // 第一次在1ms以内
        System.out.println((t3 - t2));
    }
}
