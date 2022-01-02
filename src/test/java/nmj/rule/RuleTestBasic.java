package nmj.rule;

import nmj.rule.annotations.Model;
import nmj.rule.annotations.Rule;
import org.junit.Test;

public class RuleTestBasic {

    @Test
    public void test() {
        // 最基本功能是否可用
        Numbers proxy = RuleContext.create(new PlusRule(), m -> {
            m.setA(1);
            m.setB(2);
        }).getProxy();
        // 已知 a=1, b=2, c = a+b, 则c的值为 3
        System.out.println(proxy.getC());
    }

    @Test
    public void test2() {
        // 测试规则的返回类型是否允许为模型的子类型
        // 在这个例子中 模型a为Object类型 而a的规则返回类型为String, 为Object的子类型, 所以应该是允许的
        RuleContext<AssignableFromModel> numbersRuleContext = RuleContext.create(new AssignableFromRule(), m -> {
        });
        System.out.println(numbersRuleContext.get("a") + "");
    }

    public static class PlusRule implements Rules<Numbers> {
        @Rule("c")
        private Integer c(Integer a, Integer b) {
            return a + b;
        }
    }

    public static class Numbers {
        @Model("a")
        private Integer a;
        @Model("b")
        private Integer b;
        @Model("c")
        private Integer c;

        public int getA() {
            return a;
        }

        public void setA(int a) {
            this.a = a;
        }

        public int getB() {
            return b;
        }

        public void setB(int b) {
            this.b = b;
        }

        @Model("c")
        public int getC() {
            return c;
        }

        public void setC(int c) {
            this.c = c;
        }
    }

    public static class AssignableFromRule implements Rules<AssignableFromModel> {
        @Rule("a")
        private String a() {
            // 可以看到, a的类型故意定义为String
            return "a";
        }
    }

    public static class AssignableFromModel {
        @Model("a")
        private Object a;

        public Object getA() {
            return a;
        }

        public void setA(Object a) {
            this.a = a;
        }
    }
}
