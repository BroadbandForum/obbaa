// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: tr451_vomci_nbi_message.proto

package org.broadband_forum.obbaa.onu.message.gpb.message;

public interface ReplaceConfigOrBuilder extends
    // @@protoc_insertion_point(interface_extends:tr451_vomci_nbi_message.v1.ReplaceConfig)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * Full configuration instance to
   * </pre>
   *
   * <code>bytes config_inst = 1;</code>
   * @return The configInst.
   */
  com.google.protobuf.ByteString getConfigInst();

  /**
   * <pre>
   * be used as a replacement of what
   * exists for the target
   * </pre>
   *
   * <code>string datastore_tag = 2;</code>
   * @return The datastoreTag.
   */
  java.lang.String getDatastoreTag();
  /**
   * <pre>
   * be used as a replacement of what
   * exists for the target
   * </pre>
   *
   * <code>string datastore_tag = 2;</code>
   * @return The bytes for datastoreTag.
   */
  com.google.protobuf.ByteString
      getDatastoreTagBytes();
}
