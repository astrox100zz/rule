package nmj.rule;

import nmj.rule.annotations.Rule;
import nmj.rule.annotations.RuleOrder;

public class RuleClassTest2 extends RuleClassTest {

    @Rule("brand1")
    @RuleOrder(0)
    private String brand1() {
        return "oioi";
    }

    @Rule("brand")
    @RuleOrder(0)
    private String brand(String brand1, String brand2) {
        return brand1 + brand2;
    }

    @Rule("brand3")
    @RuleOrder(0)
    private String bran3() {
        return "cos";
    }
}
