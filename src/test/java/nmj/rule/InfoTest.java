package nmj.rule;

import nmj.rule.annotations.Model;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class InfoTest {

    @Test
    public void test3() throws Exception {
        long t0 = System.currentTimeMillis();
        InfoCache.getRules(Suit.class);
        long t = System.currentTimeMillis();
        InfoCache.getRules(Suit.class);
        long t1 = System.currentTimeMillis();
        InfoCache.getModels(Suit.class);
        long t2 = System.currentTimeMillis();
        InfoCache.getModels(Suit.class);
        // 第一次访问10ms左右
        System.out.println((t - t0));
        // 第一次访问10ms左右
        System.out.println((t1 - t));
        long t3 = System.currentTimeMillis();
        // 第一次访问10ms左右
        System.out.println((t2 - t1));
        // 第一次在1ms以内
        System.out.println((t3 - t2));
    }

    @Test
    public void test() throws Exception {
        long t1 = System.currentTimeMillis();
        getModels();
        long t2 = System.currentTimeMillis();
        getModels();
        long t3 = System.currentTimeMillis();
        // 第一次访问10ms左右
        System.out.println((t2 - t1));
        // 第一次在1ms以内
        System.out.println((t3 - t2));
    }

    private List<InfoCache.Model> getModels() {
        Field[] declaredFields = Suit.class.getDeclaredFields();
        List<InfoCache.Model> models = new ArrayList<>(declaredFields.length);
        for (Field field : declaredFields) {
            Model annotation = field.getAnnotation(Model.class);
            if (annotation == null) {
                continue;
            }
            String name = annotation.name();
            if (name == null || name.isEmpty()) {
                throw new IllegalArgumentException();
            }
            models.add(new InfoCache.Model(name, field));
        }
        return models;
    }
}