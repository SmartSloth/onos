COMPILE_DEPS = CORE_DEPS + [
    "//protocols/grpc/api:onos-protocols-grpc-api",
    "//protocols/grpc/proto:onos-protocols-grpc-proto",
    "@io_grpc_grpc_java//core",
    "@io_grpc_grpc_java//netty",
    "@io_grpc_grpc_java//protobuf-lite",
    "@com_google_protobuf//:protobuf_java",
    "@com_google_api_grpc_proto_google_common_protos//jar",
]

osgi_jar(
    deps = COMPILE_DEPS,
)
