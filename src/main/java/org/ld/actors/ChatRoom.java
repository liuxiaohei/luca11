package org.ld.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * https://doc.akka.io/docs/akka/current/typed/actors.html
 */
public class ChatRoom {

    public interface RoomCommand {

    }

    interface SessionEvent {

    }

    public static final class GetSession implements RoomCommand {
        public final String screenName;
        public final ActorRef<SessionEvent> replyTo;

        public GetSession(String screenName, ActorRef<SessionEvent> replyTo) {
            this.screenName = screenName;
            this.replyTo = replyTo;
        }
    }

    public static final class SessionGranted implements SessionEvent {
        public final ActorRef<PostMessage> handle;

        public SessionGranted(ActorRef<PostMessage> handle) {
            this.handle = handle;
        }
    }

    public static final class SessionDenied implements SessionEvent {
        public final String reason;

        public SessionDenied(String reason) {
            this.reason = reason;
        }
    }

    public static final class MessagePosted implements SessionEvent {
        public final String screenName;
        public final String message;

        public MessagePosted(String screenName, String message) {
            this.screenName = screenName;
            this.message = message;
        }
    }

    interface SessionCommand {
    }

    public static final class PostMessage implements SessionCommand {
        public final String message;

        public PostMessage(String message) {
            this.message = message;
        }
    }

    private static final class NotifyClient implements SessionCommand {
        final MessagePosted message;

        NotifyClient(MessagePosted message) {
            this.message = message;
        }
    }

    private static final class PublishSessionMessage implements RoomCommand {
        public final String screenName;
        public final String message;

        public PublishSessionMessage(String screenName, String message) {
            this.screenName = screenName;
            this.message = message;
        }
    }

    private final ActorContext<RoomCommand> context;

    private ChatRoom(ActorContext<RoomCommand> context) {
        this.context = context;
    }

    private Behavior<RoomCommand> chatRoom(List<ActorRef<SessionCommand>> sessions) {
        return Behaviors.receive(RoomCommand.class)
                .onMessage(GetSession.class, getSession -> onGetSession(sessions, getSession))
                .onMessage(PublishSessionMessage.class, pub -> onPublishSessionMessage(sessions, pub))
                .build();
    }

    private Behavior<RoomCommand> onGetSession(
            List<ActorRef<SessionCommand>> sessions, GetSession getSession)
            throws UnsupportedEncodingException {
        ActorRef<SessionEvent> client = getSession.replyTo;
        ActorRef<SessionCommand> ses =
                context.spawn(
                        Session.create(context.getSelf(), getSession.screenName, client),
                        URLEncoder.encode(getSession.screenName, StandardCharsets.UTF_8.name()));
        client.tell(new SessionGranted(ses.narrow()));
        List<ActorRef<SessionCommand>> newSessions = new ArrayList<>(sessions);
        newSessions.add(ses);
        return chatRoom(newSessions);
    }

    private Behavior<RoomCommand> onPublishSessionMessage(
            List<ActorRef<SessionCommand>> sessions, PublishSessionMessage pub) {
        NotifyClient notification =
                new NotifyClient((new MessagePosted(pub.screenName, pub.message)));
        sessions.forEach(s -> s.tell(notification));
        return Behaviors.same();
    }

    static class Session {
        static Behavior<ChatRoom.SessionCommand> create(
                ActorRef<RoomCommand> room, String screenName, ActorRef<SessionEvent> client) {
            return Behaviors.receive(ChatRoom.SessionCommand.class)
                    .onMessage(PostMessage.class, post -> onPostMessage(room, screenName, post))
                    .onMessage(NotifyClient.class, notification -> onNotifyClient(client, notification))
                    .build();
        }

        private static Behavior<SessionCommand> onPostMessage(
                ActorRef<RoomCommand> room, String screenName, PostMessage post) {
            room.tell(new PublishSessionMessage(screenName, post.message));
            return Behaviors.same();
        }

        private static Behavior<SessionCommand> onNotifyClient(
                ActorRef<SessionEvent> client, NotifyClient notification) {
            client.tell(notification.message);
            return Behaviors.same();
        }
    }

    public static class Gabbler {

        private final ActorContext<ChatRoom.SessionEvent> context;

        private Gabbler(ActorContext<ChatRoom.SessionEvent> context) {
            this.context = context;
        }

        private Behavior<ChatRoom.SessionEvent> behavior() {
            return Behaviors.receive(ChatRoom.SessionEvent.class)
                    .onMessage(ChatRoom.SessionDenied.class, this::onSessionDenied)
                    .onMessage(ChatRoom.SessionGranted.class, this::onSessionGranted)
                    .onMessage(ChatRoom.MessagePosted.class, this::onMessagePosted)
                    .build();
        }

        private Behavior<ChatRoom.SessionEvent> onSessionDenied(ChatRoom.SessionDenied message) {
            context.getLog().info("cannot start chat room session: {}", message.reason);
            return Behaviors.stopped();
        }

        private Behavior<ChatRoom.SessionEvent> onSessionGranted(ChatRoom.SessionGranted message) {
            message.handle.tell(new ChatRoom.PostMessage("Hello World!"));
            return Behaviors.same();
        }

        private Behavior<ChatRoom.SessionEvent> onMessagePosted(ChatRoom.MessagePosted message) {
            context
                    .getLog()
                    .info("message has been posted by '{}': {}", message.screenName, message.message);
            return Behaviors.stopped();
        }
    }

    public static class Main {
        public static void main(String[] args) throws InterruptedException {
            List<ActorRef<SessionCommand>> sessions = new ArrayList<>();
            ActorSystem.create(
                    Behaviors.setup(
                            context -> {
                                ActorRef<ChatRoom.RoomCommand> chatRoom = context.spawn(
                                        Behaviors.setup(ctx -> new ChatRoom(ctx).chatRoom(new ArrayList<>())),
                                        "chatRoom");
                                ActorRef<ChatRoom.SessionEvent> gabbler = context.spawn(
                                        Behaviors.setup(ctx -> new Gabbler(ctx).behavior()),
                                        "gabbler");
                                context.watch(gabbler);
                                chatRoom.tell(new ChatRoom.GetSession("ol’ Gabbler", gabbler));
                                return Behaviors
                                        .receive(Object.class)
                                        .onSignal(Terminated.class, sig -> Behaviors.stopped())
                                        .build();
                            }),
                    "ChatRoomDemo"
            );
            Thread.sleep(10000);
        }
    }

}
