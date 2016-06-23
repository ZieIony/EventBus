package tk.zielony.eventbus;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Marcin on 2016-03-14.
 */
public class EventBus {
    private static class Subscriber {
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
        }

        void post(Object event) {
            events.add(event);
            if (active)
                postEvents();
        }

        void postEvents() {
            while (!events.isEmpty()) {
                final Object e = events.remove(0);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            methods.get(e.getClass()).invoke(receiver, e);
                        } catch (IllegalAccessException e1) {
                            Log.e(TAG,"invoke method failed",e1);
                        } catch (InvocationTargetException e1) {
                            Log.e(TAG,"invoke method failed",e1);
                        }
                    }
                });
            }
        }
    }

    private static Map<Class, Subscriber> subscribers = new HashMap<>();
    private static Handler handler = new Handler(Looper.getMainLooper());

    public static void register(Object object) {
        if (subscribers.containsKey(object.getClass())) {
            Subscriber subscriber = subscribers.get(object.getClass());
            subscriber.active = true;
            subscriber.receiver = object;
            subscriber.postEvents();
        } else {
            Subscriber subscriber = new Subscriber(object);
            subscriber.active = true;
            subscribers.put(object.getClass(), subscriber);
        }
    }

    public static void unregister(Object object) {
        Subscriber subscriber = subscribers.get(object.getClass());
        if (subscriber.events.isEmpty()) {
            subscribers.remove(object.getClass());
        } else {
            subscriber.active = false;
        }
    }

    public static void post(Object event) {
        for (Subscriber s : subscribers.values()) {
            if (s.methods.containsKey(event.getClass()))
                s.post(event);
        }
    }
}
