package org.ld.utils.grpc.proto;

public final class GrpcProto {
    private GrpcProto() {
    }

    public static void registerAllExtensions() {
    }

    static final com.google.protobuf.Descriptors.Descriptor internal_static_GrpcRequest_descriptor;
    static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_GrpcRequest_fieldAccessorTable;
    static final com.google.protobuf.Descriptors.Descriptor internal_static_GrpcReply_descriptor;
    static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_GrpcReply_fieldAccessorTable;

    public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
        return descriptor;
    }

    private static com.google.protobuf.Descriptors.FileDescriptor
            descriptor;

    static {
        String[] descriptorData = {
                "\n\013hello.proto\"\035\n\013GrpcRequest\022\016\n\006params\030\001" +
                        " \001(\t\"\034\n\tGrpcReply\022\017\n\007message\030\001 \001(\t24\n\007Gr" +
                        "eeter\022)\n\013sendMessage\022\014.GrpcRequest\032\n.Grp" +
                        "cReply\"\000B\023B\tGrpcProtoP\001\242\002\003HLWb\006proto3"
        };
        com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
                new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
                    public com.google.protobuf.ExtensionRegistry assignDescriptors(
                            com.google.protobuf.Descriptors.FileDescriptor root) {
                        descriptor = root;
                        return null;
                    }
                };
        com.google.protobuf.Descriptors.FileDescriptor
                .internalBuildGeneratedFileFrom(descriptorData,
                        new com.google.protobuf.Descriptors.FileDescriptor[]{
                        }, assigner);
        internal_static_GrpcRequest_descriptor =
                getDescriptor().getMessageTypes().get(0);
        internal_static_GrpcRequest_fieldAccessorTable = new
                com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                internal_static_GrpcRequest_descriptor,
                new String[]{"Params",});
        internal_static_GrpcReply_descriptor =
                getDescriptor().getMessageTypes().get(1);
        internal_static_GrpcReply_fieldAccessorTable = new
                com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                internal_static_GrpcReply_descriptor,
                new String[]{"Message",});
    }
}
