package net.ebh.exam.TempVo;

import org.springframework.data.mongodb.core.query.Update;

import java.lang.reflect.Field;

/**
 * Created by xh on 2017/5/5.
 */
public class UpdateUtil {
    public static Update buildBaseUpdate(Object obj) {
        Update update = new Update();
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                if (value != null) {
                    update.set(field.getName(), value);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return update;
    }
}
