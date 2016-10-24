package tk.zielony.eventbus;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Marcin on 2016-03-14.
 */
public class EventBus {

    private static Map<Object, Subscriber> subscribers = new HashMap<>();
    private static Handler handler = new Handler(Looper.getMainLooper());

    public static void register(Object object) {
        Subscriber subscriber = subscribers.get(object);
        if (subscriber != null) {
            subscriber.active = true;
            subscriber.receiver = object;
            subscriber.postEvents();
        } else {
            subscriber = new Subscriber(object);
            subscriber.active = true;
            subscribers.put(object, subscriber);
        }
    }

    public static void unregister(Object object) {
        Subscriber subscriber = subscribers.get(object);
        if (subscriber == null)
            return;
        if (subscriber.events.isEmpty()) {
            subscribers.remove(object);
        } else {
            subscriber.active = false;
        }
    }

    public static void post(Object event) {
        for (Subscriber s : subscribers.values()) {
            if (s.methods.containsKey(event.getClass())) {
                s.post(event);
                return;
            }
        }
        Log.w(EventBus.class.getSimpleName(), "No subscriber for event of type " + event.getClass().getSimpleName());
    }

    static Handler getHandler() {
        return handler;
    }
}
