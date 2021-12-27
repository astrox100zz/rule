package nmj.rule;

import nmj.rule.annotations.Model;
import nmj.rule.annotations.Rule;
import nmj.rule.annotations.RuleOrder;
import org.junit.Test;

public class RuleTest {

    @Test
    public void test() {
        final R1 r1 = new R1();
        final RuleContext<M1> m1RuleContext = RuleContext.create(r1, m -> {
        });
        final M1 proxy = m1RuleContext.getProxy();
        System.out.println(proxy.getContext());
        System.out.println(proxy.getTenantId());
    }

    public static class R1 implements Rules<M1> {

        @Rule("tenantId")
        private Long tenantId(Context context) {
            return context.getTenantId();
        }

        @RuleOrder(1)
        @Rule("tenantId")
        private Long tenantId() {
            return 1L;
        }

        @Rule("context")
        private Context context(Long tenantId) {
            return new Context(tenantId);
        }
    }

    public static class M1 {
        @Model(name = "tenantId", comment = "租户id")
        Long tenantId;
        @Model(name = "context", comment = "上下文")
        Context context;

        public Long getTenantId() {
            return tenantId;
        }

        public void setTenantId(Long tenantId) {
            this.tenantId = tenantId;
        }

        public Context getContext() {
            return context;
        }

        public void setContext(Context context) {
            this.context = context;
        }
    }

    public static class RuleClassTest implements Rules<Suit> {

        @RuleOrder(-1)
        @Rule("brand")
        private String brand(String brand2) {
            return brand2.toLowerCase();
        }

        @RuleOrder(0)
        @Rule("brand")
        private String brand() {
            return "wasup";
        }
    }

    public static class Context {
        Long tenantId;

        public Context(Long tenantId) {
            this.tenantId = tenantId;
        }

        public Long getTenantId() {
            return tenantId;
        }

        @Override
        public String toString() {
            return "Context{" +
                    "tenantId=" + tenantId +
                    '}';
        }
    }

    public static class Suit {
        @Model(name = "tenantId", comment = "租户id")
        Long tenantId;
        @Model(name = "context", comment = "上下文")
        Context context;
        @Model(name = "brand", comment = "品牌")
        private String brand;
        @Model(name = "brand1", comment = "品牌")
        private String brand1;
        @Model(name = "brand2", comment = "品牌")
        private String brand2;
        @Model(name = "brand3", comment = "品牌")
        private String brand3;
        @Model(name = "brand4", comment = "品牌")
        private String brand4;
        @Model(name = "brand5", comment = "品牌")
        private String brand5;
        @Model(name = "brand6", comment = "品牌")
        private String brand6;
        @Model(name = "brand7", comment = "品牌")
        private String brand7;
        @Model(name = "brand8", comment = "品牌")
        private String brand8;
        @Model(name = "brand9", comment = "品牌")
        private String brand9;
        @Model(name = "brand10", comment = "品牌")
        private String brand10;
        @Model(name = "brand11", comment = "品牌")
        private String brand11;
        @Model(name = "brand12", comment = "品牌")
        private String brand12;
        @Model(name = "brand13", comment = "品牌")
        private String brand13;
        @Model(name = "brand14", comment = "品牌")
        private String brand14;
        @Model(name = "brand51", comment = "品牌")
        private String brand15;
        @Model(name = "brand15", comment = "品牌")
        private String brand16;
        @Model(name = "brand16", comment = "品牌")
        private String brand17;
        @Model(name = "brand17", comment = "品牌")
        private String brand18;
        @Model(name = "brand18", comment = "品牌")
        private String brand19;
        @Model(name = "brand19", comment = "品牌")
        private String brand20;
        @Model(name = "brand20", comment = "品牌")
        private String brand21;
        @Model(name = "brand21", comment = "品牌")
        private String brand22;
        @Model(name = "brand22", comment = "品牌")
        private String brand23;
        @Model(name = "brand23", comment = "品牌")
        private String brand24;
        @Model(name = "brand24", comment = "品牌")
        private String brand25;
        @Model(name = "brand25", comment = "品牌")
        private String brand26;
        @Model(name = "brand26", comment = "品牌")
        private String brand27;
        @Model(name = "brand27", comment = "品牌")
        private String brand28;
        @Model(name = "brand28", comment = "品牌")
        private String brand29;
        @Model(name = "brand29", comment = "品牌")
        private String brand30;
        @Model(name = "brand30", comment = "品牌")
        private String brand31;
        @Model(name = "brand31", comment = "品牌")
        private String brand32;
        @Model(name = "brand32", comment = "品牌")
        private String brand33;
        @Model(name = "brand33", comment = "品牌")
        private String brand34;
        @Model(name = "brand34", comment = "品牌")
        private String brand35;
        @Model(name = "brand35", comment = "品牌")
        private String brand36;
        @Model(name = "brand36", comment = "品牌")
        private String brand37;
        @Model(name = "brand37", comment = "品牌")
        private String brand38;
        @Model(name = "brand38", comment = "品牌")
        private String brand39;
        @Model(name = "brand39", comment = "品牌")
        private String brand40;
        @Model(name = "brand40", comment = "品牌")
        private String brand41;
        @Model(name = "brand41", comment = "品牌")
        private String brand42;
        @Model(name = "brand42", comment = "品牌")
        private String brand43;
        @Model(name = "brand43", comment = "品牌")
        private String brand44;
        @Model(name = "brand44", comment = "品牌")
        private String brand45;
        @Model(name = "brand45", comment = "品牌")
        private String brand46;
        @Model(name = "brand46", comment = "品牌")
        private String brand47;
        @Model(name = "brand47", comment = "品牌")
        private String brand48;
        @Model(name = "brand48", comment = "品牌")
        private String brand49;
        @Model(name = "brand50", comment = "品牌")
        private String brand50;

        public String getBrand() {
            return brand;
        }

        public void setBrand(String brand) {
            this.brand = brand;
        }

        public String getBrand1() {
            return brand1;
        }

        public void setBrand1(String brand1) {
            this.brand1 = brand1;
        }

        public String getBrand2() {
            return brand2;
        }

        public void setBrand2(String brand2) {
            this.brand2 = brand2;
        }

        public String getBrand3() {
            return brand3;
        }

        public void setBrand3(String brand3) {
            this.brand3 = brand3;
        }

        public String getBrand4() {
            return brand4;
        }

        public void setBrand4(String brand4) {
            this.brand4 = brand4;
        }

        public String getBrand5() {
            return brand5;
        }

        public void setBrand5(String brand5) {
            this.brand5 = brand5;
        }

        public String getBrand6() {
            return brand6;
        }

        public void setBrand6(String brand6) {
            this.brand6 = brand6;
        }

        public String getBrand7() {
            return brand7;
        }

        public void setBrand7(String brand7) {
            this.brand7 = brand7;
        }

        public String getBrand8() {
            return brand8;
        }

        public void setBrand8(String brand8) {
            this.brand8 = brand8;
        }

        public String getBrand9() {
            return brand9;
        }

        public void setBrand9(String brand9) {
            this.brand9 = brand9;
        }

        public String getBrand10() {
            return brand10;
        }

        public void setBrand10(String brand10) {
            this.brand10 = brand10;
        }

        public String getBrand11() {
            return brand11;
        }

        public void setBrand11(String brand11) {
            this.brand11 = brand11;
        }

        public String getBrand12() {
            return brand12;
        }

        public void setBrand12(String brand12) {
            this.brand12 = brand12;
        }

        public String getBrand13() {
            return brand13;
        }

        public void setBrand13(String brand13) {
            this.brand13 = brand13;
        }

        public String getBrand14() {
            return brand14;
        }

        public void setBrand14(String brand14) {
            this.brand14 = brand14;
        }

        public String getBrand15() {
            return brand15;
        }

        public void setBrand15(String brand15) {
            this.brand15 = brand15;
        }

        public String getBrand16() {
            return brand16;
        }

        public void setBrand16(String brand16) {
            this.brand16 = brand16;
        }

        public String getBrand17() {
            return brand17;
        }

        public void setBrand17(String brand17) {
            this.brand17 = brand17;
        }

        public String getBrand18() {
            return brand18;
        }

        public void setBrand18(String brand18) {
            this.brand18 = brand18;
        }

        public String getBrand19() {
            return brand19;
        }

        public void setBrand19(String brand19) {
            this.brand19 = brand19;
        }

        public String getBrand20() {
            return brand20;
        }

        public void setBrand20(String brand20) {
            this.brand20 = brand20;
        }

        public String getBrand21() {
            return brand21;
        }

        public void setBrand21(String brand21) {
            this.brand21 = brand21;
        }

        public String getBrand22() {
            return brand22;
        }

        public void setBrand22(String brand22) {
            this.brand22 = brand22;
        }

        public String getBrand23() {
            return brand23;
        }

        public void setBrand23(String brand23) {
            this.brand23 = brand23;
        }

        public String getBrand24() {
            return brand24;
        }

        public void setBrand24(String brand24) {
            this.brand24 = brand24;
        }

        public String getBrand25() {
            return brand25;
        }

        public void setBrand25(String brand25) {
            this.brand25 = brand25;
        }

        public String getBrand26() {
            return brand26;
        }

        public void setBrand26(String brand26) {
            this.brand26 = brand26;
        }

        public String getBrand27() {
            return brand27;
        }

        public void setBrand27(String brand27) {
            this.brand27 = brand27;
        }

        public String getBrand28() {
            return brand28;
        }

        public void setBrand28(String brand28) {
            this.brand28 = brand28;
        }

        public String getBrand29() {
            return brand29;
        }

        public void setBrand29(String brand29) {
            this.brand29 = brand29;
        }

        public String getBrand30() {
            return brand30;
        }

        public void setBrand30(String brand30) {
            this.brand30 = brand30;
        }

        public String getBrand31() {
            return brand31;
        }

        public void setBrand31(String brand31) {
            this.brand31 = brand31;
        }

        public String getBrand32() {
            return brand32;
        }

        public void setBrand32(String brand32) {
            this.brand32 = brand32;
        }

        public String getBrand33() {
            return brand33;
        }

        public void setBrand33(String brand33) {
            this.brand33 = brand33;
        }

        public String getBrand34() {
            return brand34;
        }

        public void setBrand34(String brand34) {
            this.brand34 = brand34;
        }

        public String getBrand35() {
            return brand35;
        }

        public void setBrand35(String brand35) {
            this.brand35 = brand35;
        }

        public String getBrand36() {
            return brand36;
        }

        public void setBrand36(String brand36) {
            this.brand36 = brand36;
        }

        public String getBrand37() {
            return brand37;
        }

        public void setBrand37(String brand37) {
            this.brand37 = brand37;
        }

        public String getBrand38() {
            return brand38;
        }

        public void setBrand38(String brand38) {
            this.brand38 = brand38;
        }

        public String getBrand39() {
            return brand39;
        }

        public void setBrand39(String brand39) {
            this.brand39 = brand39;
        }

        public String getBrand40() {
            return brand40;
        }

        public void setBrand40(String brand40) {
            this.brand40 = brand40;
        }

        public String getBrand41() {
            return brand41;
        }

        public void setBrand41(String brand41) {
            this.brand41 = brand41;
        }

        public String getBrand42() {
            return brand42;
        }

        public void setBrand42(String brand42) {
            this.brand42 = brand42;
        }

        public String getBrand43() {
            return brand43;
        }

        public void setBrand43(String brand43) {
            this.brand43 = brand43;
        }

        public String getBrand44() {
            return brand44;
        }

        public void setBrand44(String brand44) {
            this.brand44 = brand44;
        }

        public String getBrand45() {
            return brand45;
        }

        public void setBrand45(String brand45) {
            this.brand45 = brand45;
        }

        public String getBrand46() {
            return brand46;
        }

        public void setBrand46(String brand46) {
            this.brand46 = brand46;
        }

        public String getBrand47() {
            return brand47;
        }

        public void setBrand47(String brand47) {
            this.brand47 = brand47;
        }

        public String getBrand48() {
            return brand48;
        }

        public void setBrand48(String brand48) {
            this.brand48 = brand48;
        }

        public String getBrand49() {
            return brand49;
        }

        public void setBrand49(String brand49) {
            this.brand49 = brand49;
        }

        public String getBrand50() {
            return brand50;
        }

        public void setBrand50(String brand50) {
            this.brand50 = brand50;
        }

        public Long getTenantId() {
            return tenantId;
        }

        public void setTenantId(Long tenantId) {
            this.tenantId = tenantId;
        }

        public Context getContext() {
            return context;
        }

        public void setContext(Context context) {
            this.context = context;
        }
    }
}
