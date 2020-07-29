package org.ld.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Terminated;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import lombok.AllArgsConstructor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * https://doc.akka.io/docs/akka/current/typed/actors.html
 */
public class ChatRoom {

    public interface RoomCommand {}

    @AllArgsConstructor
    public static final class GetSession implements RoomCommand {
        public final String screenName;
        public final ActorRef<SessionEvent> replyTo;
    }

    @AllArgsConstructor
    private static final class PublishSessionMessage implements RoomCommand {
        public final String screenName;
        public final String message;
    }

    interface SessionEvent {}

    @AllArgsConstructor
    public static final class SessionGranted implements SessionEvent {
        public final ActorRef<PostMessage> handle;
    }

    @AllArgsConstructor
    public static final class SessionDenied implements SessionEvent {
        public final String reason;
    }

    @AllArgsConstructor
    public static final class MessagePosted implements SessionEvent {
        public final String screenName;
        public final String message;
    }

    interface SessionCommand {}

    @AllArgsConstructor
    public static final class PostMessage implements SessionCommand {
        public final String message;
    }

    @AllArgsConstructor
    private static final class NotifyClient implements SessionCommand {
        final MessagePosted message;
    }

    private static Behavior<RoomCommand> chatRoom(ActorContext<RoomCommand> ctx, List<ActorRef<SessionCommand>> sessions) {
        return Behaviors.receive(RoomCommand.class)
                .onMessage(GetSession.class, getSession -> {
                    ActorRef<SessionCommand> ses =
                            ctx.spawn(
                                    Behaviors.receive(SessionCommand.class)
                                            .onMessage(PostMessage.class, post -> {
                                                ctx.getSelf().tell(
                                                        new PublishSessionMessage(
                                                                getSession.screenName,
                                                                post.message));
                                                return Behaviors.same();
                                            })
                                            .onMessage(NotifyClient.class, notification -> {
                                                getSession.replyTo.tell(notification.message);
                                                return Behaviors.same();
                                            })
                                            .build(),
                                    URLEncoder.encode(getSession.screenName, StandardCharsets.UTF_8.name()));
                    getSession.replyTo.tell(new SessionGranted(ses.narrow()));
                    var newSessions = new ArrayList<>(sessions);
                    newSessions.add(ses);
                    return chatRoom(ctx, newSessions);
                })
                .onMessage(PublishSessionMessage.class, pub -> {
                    var notification = new NotifyClient((new MessagePosted(pub.screenName, pub.message)));
                    sessions.forEach(s -> s.tell(notification));
                    return Behaviors.same();
                })
                .build();
    }

    public static class Main {
        public static void main(String[] args) throws InterruptedException {
            ActorSystem.create(
                    Behaviors.setup(
                            context -> {
                                ActorRef<ChatRoom.RoomCommand> chatRoom = context.spawn(
                                        Behaviors.setup(ctx -> chatRoom(ctx, new ArrayList<>())),
                                        "chatRoom"
                                );
                                ActorRef<ChatRoom.SessionEvent> gabbler = context.spawn(
                                        Behaviors.setup(ctx ->
                                                Behaviors.receive(ChatRoom.SessionEvent.class)
                                                .onMessage(ChatRoom.SessionDenied.class, message -> {
                                                    ctx.getLog().info("cannot start chat room session: {}", message.reason);
                                                    return Behaviors.stopped();
                                                })
                                                .onMessage(ChatRoom.SessionGranted.class, message -> {
                                                    message.handle.tell(new ChatRoom.PostMessage("Hello World!"));
                                                    return Behaviors.same();
                                                })
                                                .onMessage(ChatRoom.MessagePosted.class, message -> {
                                                    ctx.getLog().info("message has been posted by '{}': {}", message.screenName, message.message);
                                                    return Behaviors.stopped();
                                                })
                                                .build()),
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
