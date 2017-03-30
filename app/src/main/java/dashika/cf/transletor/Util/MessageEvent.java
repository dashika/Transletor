package dashika.cf.transletor.Util;

/**
 * Created by dashika on 03/03/17.
 */

public class MessageEvent {
    public enum EventType {
        LOGIN_SUCCESSFUL,
        LOGIN_FAIL
    }

    public EventType getEventType() {
        return eventType;
    }

   private EventType eventType;

    public MessageEvent(EventType eventType) {
        this.eventType = eventType;
    }
}
