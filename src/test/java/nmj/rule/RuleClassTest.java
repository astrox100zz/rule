package nmj.rule;

import nmj.rule.annotations.Rule;
import nmj.rule.annotations.RuleOrder;

public class RuleClassTest implements Rules<Suit> {

    @RuleOrder(-1)
    @Rule("brand")
    private String brand(String brand) {
        return "wasup";
    }

    @RuleOrder(0)
    @Rule("brand")
    private String brand() {
        return "wasup";
    }
}
