// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: tr451_vomci_nbi_message.proto

package org.broadband_forum.obbaa.onu.pm.message.gpb.message;

public interface NotificationOrBuilder extends
        // @@protoc_insertion_point(interface_extends:tr451_vomci_nbi_message.v1.Notification)
        com.google.protobuf.MessageOrBuilder {

    /**
     * <code>string event_timestamp = 1;</code>
     *
     * @return The eventTimestamp.
     */
    java.lang.String getEventTimestamp();

    /**
     * <code>string event_timestamp = 1;</code>
     *
     * @return The bytes for eventTimestamp.
     */
    com.google.protobuf.ByteString
    getEventTimestampBytes();

    /**
     * <code>bytes data = 2;</code>
     *
     * @return The data.
     */
    com.google.protobuf.ByteString getData();
}
