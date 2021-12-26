package nmj.rule;

import nmj.rule.annotations.Rule;
import nmj.rule.annotations.RuleOrder;

public class RuleClassTest implements Rules<Suit> {

    @RuleOrder(0)
    @Rule("brand")
    private String brand() {
        return "wasup";
    }
}
