package tk.zielony.eventbus;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Marcin on 2016-10-24.
 */
class Subscriber {
    private static final String TAG = Subscriber.class.getSimpleName();

    Map<Class, Method> methods = new HashMap<>();
    Object receiver;
    boolean active;
    List<Object> events = new ArrayList<>();

    Subscriber(Object object) {
        this.receiver = object;
        Method[] methods = receiver.getClass().getMethods();
        for (Method m : methods) {
            if (m.isAnnotationPresent(Subscribe.class))
                this.methods.put(m.getParameterTypes()[0], m);
        }
        if (this.methods.isEmpty())
            throw new IllegalArgumentException("Class " + object.getClass().getSimpleName() + " doesn't contain any event handlers");
    }

    public void post(Object event) {
        events.add(event);
        if (active)
            postEvents();
    }

    void postEvents() {
        while (!events.isEmpty()) {
            final Object e = events.remove(0);
            EventBus.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        methods.get(e.getClass()).invoke(receiver, e);
                    } catch (IllegalAccessException e1) {
                        Log.e(TAG, "invoke method failed", e1);
                    } catch (InvocationTargetException e1) {
                        Log.e(TAG, "invoke method failed", e1);
                    }
                }
            });
        }
    }
}
