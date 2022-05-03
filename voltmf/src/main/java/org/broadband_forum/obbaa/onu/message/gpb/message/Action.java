// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: tr451_vomci_nbi_message.proto

package org.broadband_forum.obbaa.onu.message.gpb.message;

/**
 * Protobuf type {@code tr451_vomci_nbi_message.v1.Action}
 */
public  final class Action extends
    com.google.protobuf.GeneratedMessageV3 implements
    // @@protoc_insertion_point(message_implements:tr451_vomci_nbi_message.v1.Action)
    ActionOrBuilder {
private static final long serialVersionUID = 0L;
  // Use Action.newBuilder() to construct.
  private Action(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
    super(builder);
  }
  private Action() {
    inputData_ = com.google.protobuf.ByteString.EMPTY;
    datastoreTag_ = "";
  }

  @java.lang.Override
  @SuppressWarnings({"unused"})
  protected java.lang.Object newInstance(
      UnusedPrivateParameter unused) {
    return new Action();
  }

  @java.lang.Override
  public final com.google.protobuf.UnknownFieldSet
  getUnknownFields() {
    return this.unknownFields;
  }
  private Action(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    this();
    if (extensionRegistry == null) {
      throw new java.lang.NullPointerException();
    }
    com.google.protobuf.UnknownFieldSet.Builder unknownFields =
        com.google.protobuf.UnknownFieldSet.newBuilder();
    try {
      boolean done = false;
      while (!done) {
        int tag = input.readTag();
        switch (tag) {
          case 0:
            done = true;
            break;
          case 10: {

            inputData_ = input.readBytes();
            break;
          }
          case 18: {
            java.lang.String s = input.readStringRequireUtf8();

            datastoreTag_ = s;
            break;
          }
          default: {
            if (!parseUnknownField(
                input, unknownFields, extensionRegistry, tag)) {
              done = true;
            }
            break;
          }
        }
      }
    } catch (com.google.protobuf.InvalidProtocolBufferException e) {
      throw e.setUnfinishedMessage(this);
    } catch (java.io.IOException e) {
      throw new com.google.protobuf.InvalidProtocolBufferException(
          e).setUnfinishedMessage(this);
    } finally {
      this.unknownFields = unknownFields.build();
      makeExtensionsImmutable();
    }
  }
  public static final com.google.protobuf.Descriptors.Descriptor
      getDescriptor() {
    return org.broadband_forum.obbaa.onu.message.gpb.message.Tr451VomciNbiMessage.internal_static_tr451_vomci_nbi_message_v1_Action_descriptor;
  }

  @java.lang.Override
  protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internalGetFieldAccessorTable() {
    return org.broadband_forum.obbaa.onu.message.gpb.message.Tr451VomciNbiMessage.internal_static_tr451_vomci_nbi_message_v1_Action_fieldAccessorTable
        .ensureFieldAccessorsInitialized(
            org.broadband_forum.obbaa.onu.message.gpb.message.Action.class, org.broadband_forum.obbaa.onu.message.gpb.message.Action.Builder.class);
  }

  public static final int INPUT_DATA_FIELD_NUMBER = 1;
  private com.google.protobuf.ByteString inputData_;
  /**
   * <code>bytes input_data = 1;</code>
   * @return The inputData.
   */
  public com.google.protobuf.ByteString getInputData() {
    return inputData_;
  }

  public static final int DATASTORE_TAG_FIELD_NUMBER = 2;
  private volatile java.lang.Object datastoreTag_;
  /**
   * <pre>
   * Optional: Datastore tag used to
   * </pre>
   *
   * <code>string datastore_tag = 2;</code>
   * @return The datastoreTag.
   */
  public java.lang.String getDatastoreTag() {
    java.lang.Object ref = datastoreTag_;
    if (ref instanceof java.lang.String) {
      return (java.lang.String) ref;
    } else {
      com.google.protobuf.ByteString bs = 
          (com.google.protobuf.ByteString) ref;
      java.lang.String s = bs.toStringUtf8();
      datastoreTag_ = s;
      return s;
    }
  }
  /**
   * <pre>
   * Optional: Datastore tag used to
   * </pre>
   *
   * <code>string datastore_tag = 2;</code>
   * @return The bytes for datastoreTag.
   */
  public com.google.protobuf.ByteString
      getDatastoreTagBytes() {
    java.lang.Object ref = datastoreTag_;
    if (ref instanceof java.lang.String) {
      com.google.protobuf.ByteString b = 
          com.google.protobuf.ByteString.copyFromUtf8(
              (java.lang.String) ref);
      datastoreTag_ = b;
      return b;
    } else {
      return (com.google.protobuf.ByteString) ref;
    }
  }

  private byte memoizedIsInitialized = -1;
  @java.lang.Override
  public final boolean isInitialized() {
    byte isInitialized = memoizedIsInitialized;
    if (isInitialized == 1) return true;
    if (isInitialized == 0) return false;

    memoizedIsInitialized = 1;
    return true;
  }

  @java.lang.Override
  public void writeTo(com.google.protobuf.CodedOutputStream output)
                      throws java.io.IOException {
    if (!inputData_.isEmpty()) {
      output.writeBytes(1, inputData_);
    }
    if (!getDatastoreTagBytes().isEmpty()) {
      com.google.protobuf.GeneratedMessageV3.writeString(output, 2, datastoreTag_);
    }
    unknownFields.writeTo(output);
  }

  @java.lang.Override
  public int getSerializedSize() {
    int size = memoizedSize;
    if (size != -1) return size;

    size = 0;
    if (!inputData_.isEmpty()) {
      size += com.google.protobuf.CodedOutputStream
        .computeBytesSize(1, inputData_);
    }
    if (!getDatastoreTagBytes().isEmpty()) {
      size += com.google.protobuf.GeneratedMessageV3.computeStringSize(2, datastoreTag_);
    }
    size += unknownFields.getSerializedSize();
    memoizedSize = size;
    return size;
  }

  @java.lang.Override
  public boolean equals(final java.lang.Object obj) {
    if (obj == this) {
     return true;
    }
    if (!(obj instanceof org.broadband_forum.obbaa.onu.message.gpb.message.Action)) {
      return super.equals(obj);
    }
    org.broadband_forum.obbaa.onu.message.gpb.message.Action other = (org.broadband_forum.obbaa.onu.message.gpb.message.Action) obj;

    if (!getInputData()
        .equals(other.getInputData())) return false;
    if (!getDatastoreTag()
        .equals(other.getDatastoreTag())) return false;
    if (!unknownFields.equals(other.unknownFields)) return false;
    return true;
  }

  @java.lang.Override
  public int hashCode() {
    if (memoizedHashCode != 0) {
      return memoizedHashCode;
    }
    int hash = 41;
    hash = (19 * hash) + getDescriptor().hashCode();
    hash = (37 * hash) + INPUT_DATA_FIELD_NUMBER;
    hash = (53 * hash) + getInputData().hashCode();
    hash = (37 * hash) + DATASTORE_TAG_FIELD_NUMBER;
    hash = (53 * hash) + getDatastoreTag().hashCode();
    hash = (29 * hash) + unknownFields.hashCode();
    memoizedHashCode = hash;
    return hash;
  }

  public static org.broadband_forum.obbaa.onu.message.gpb.message.Action parseFrom(
      java.nio.ByteBuffer data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.broadband_forum.obbaa.onu.message.gpb.message.Action parseFrom(
      java.nio.ByteBuffer data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.broadband_forum.obbaa.onu.message.gpb.message.Action parseFrom(
      com.google.protobuf.ByteString data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.broadband_forum.obbaa.onu.message.gpb.message.Action parseFrom(
      com.google.protobuf.ByteString data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.broadband_forum.obbaa.onu.message.gpb.message.Action parseFrom(byte[] data)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data);
  }
  public static org.broadband_forum.obbaa.onu.message.gpb.message.Action parseFrom(
      byte[] data,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws com.google.protobuf.InvalidProtocolBufferException {
    return PARSER.parseFrom(data, extensionRegistry);
  }
  public static org.broadband_forum.obbaa.onu.message.gpb.message.Action parseFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.broadband_forum.obbaa.onu.message.gpb.message.Action parseFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.broadband_forum.obbaa.onu.message.gpb.message.Action parseDelimitedFrom(java.io.InputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input);
  }
  public static org.broadband_forum.obbaa.onu.message.gpb.message.Action parseDelimitedFrom(
      java.io.InputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
  }
  public static org.broadband_forum.obbaa.onu.message.gpb.message.Action parseFrom(
      com.google.protobuf.CodedInputStream input)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input);
  }
  public static org.broadband_forum.obbaa.onu.message.gpb.message.Action parseFrom(
      com.google.protobuf.CodedInputStream input,
      com.google.protobuf.ExtensionRegistryLite extensionRegistry)
      throws java.io.IOException {
    return com.google.protobuf.GeneratedMessageV3
        .parseWithIOException(PARSER, input, extensionRegistry);
  }

  @java.lang.Override
  public Builder newBuilderForType() { return newBuilder(); }
  public static Builder newBuilder() {
    return DEFAULT_INSTANCE.toBuilder();
  }
  public static Builder newBuilder(org.broadband_forum.obbaa.onu.message.gpb.message.Action prototype) {
    return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
  }
  @java.lang.Override
  public Builder toBuilder() {
    return this == DEFAULT_INSTANCE
        ? new Builder() : new Builder().mergeFrom(this);
  }

  @java.lang.Override
  protected Builder newBuilderForType(
      com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
    Builder builder = new Builder(parent);
    return builder;
  }
  /**
   * Protobuf type {@code tr451_vomci_nbi_message.v1.Action}
   */
  public static final class Builder extends
      com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
      // @@protoc_insertion_point(builder_implements:tr451_vomci_nbi_message.v1.Action)
      org.broadband_forum.obbaa.onu.message.gpb.message.ActionOrBuilder {
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.broadband_forum.obbaa.onu.message.gpb.message.Tr451VomciNbiMessage.internal_static_tr451_vomci_nbi_message_v1_Action_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.broadband_forum.obbaa.onu.message.gpb.message.Tr451VomciNbiMessage.internal_static_tr451_vomci_nbi_message_v1_Action_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.broadband_forum.obbaa.onu.message.gpb.message.Action.class, org.broadband_forum.obbaa.onu.message.gpb.message.Action.Builder.class);
    }

    // Construct using org.broadband_forum.obbaa.onu.message.gpb.message.Action.newBuilder()
    private Builder() {
      maybeForceBuilderInitialization();
    }

    private Builder(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      super(parent);
      maybeForceBuilderInitialization();
    }
    private void maybeForceBuilderInitialization() {
      if (com.google.protobuf.GeneratedMessageV3
              .alwaysUseFieldBuilders) {
      }
    }
    @java.lang.Override
    public Builder clear() {
      super.clear();
      inputData_ = com.google.protobuf.ByteString.EMPTY;

      datastoreTag_ = "";

      return this;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
      return org.broadband_forum.obbaa.onu.message.gpb.message.Tr451VomciNbiMessage.internal_static_tr451_vomci_nbi_message_v1_Action_descriptor;
    }

    @java.lang.Override
    public org.broadband_forum.obbaa.onu.message.gpb.message.Action getDefaultInstanceForType() {
      return org.broadband_forum.obbaa.onu.message.gpb.message.Action.getDefaultInstance();
    }

    @java.lang.Override
    public org.broadband_forum.obbaa.onu.message.gpb.message.Action build() {
      org.broadband_forum.obbaa.onu.message.gpb.message.Action result = buildPartial();
      if (!result.isInitialized()) {
        throw newUninitializedMessageException(result);
      }
      return result;
    }

    @java.lang.Override
    public org.broadband_forum.obbaa.onu.message.gpb.message.Action buildPartial() {
      org.broadband_forum.obbaa.onu.message.gpb.message.Action result = new org.broadband_forum.obbaa.onu.message.gpb.message.Action(this);
      result.inputData_ = inputData_;
      result.datastoreTag_ = datastoreTag_;
      onBuilt();
      return result;
    }

    @java.lang.Override
    public Builder clone() {
      return super.clone();
    }
    @java.lang.Override
    public Builder setField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.setField(field, value);
    }
    @java.lang.Override
    public Builder clearField(
        com.google.protobuf.Descriptors.FieldDescriptor field) {
      return super.clearField(field);
    }
    @java.lang.Override
    public Builder clearOneof(
        com.google.protobuf.Descriptors.OneofDescriptor oneof) {
      return super.clearOneof(oneof);
    }
    @java.lang.Override
    public Builder setRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        int index, java.lang.Object value) {
      return super.setRepeatedField(field, index, value);
    }
    @java.lang.Override
    public Builder addRepeatedField(
        com.google.protobuf.Descriptors.FieldDescriptor field,
        java.lang.Object value) {
      return super.addRepeatedField(field, value);
    }
    @java.lang.Override
    public Builder mergeFrom(com.google.protobuf.Message other) {
      if (other instanceof org.broadband_forum.obbaa.onu.message.gpb.message.Action) {
        return mergeFrom((org.broadband_forum.obbaa.onu.message.gpb.message.Action)other);
      } else {
        super.mergeFrom(other);
        return this;
      }
    }

    public Builder mergeFrom(org.broadband_forum.obbaa.onu.message.gpb.message.Action other) {
      if (other == org.broadband_forum.obbaa.onu.message.gpb.message.Action.getDefaultInstance()) return this;
      if (other.getInputData() != com.google.protobuf.ByteString.EMPTY) {
        setInputData(other.getInputData());
      }
      if (!other.getDatastoreTag().isEmpty()) {
        datastoreTag_ = other.datastoreTag_;
        onChanged();
      }
      this.mergeUnknownFields(other.unknownFields);
      onChanged();
      return this;
    }

    @java.lang.Override
    public final boolean isInitialized() {
      return true;
    }

    @java.lang.Override
    public Builder mergeFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      org.broadband_forum.obbaa.onu.message.gpb.message.Action parsedMessage = null;
      try {
        parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        parsedMessage = (org.broadband_forum.obbaa.onu.message.gpb.message.Action) e.getUnfinishedMessage();
        throw e.unwrapIOException();
      } finally {
        if (parsedMessage != null) {
          mergeFrom(parsedMessage);
        }
      }
      return this;
    }

    private com.google.protobuf.ByteString inputData_ = com.google.protobuf.ByteString.EMPTY;
    /**
     * <code>bytes input_data = 1;</code>
     * @return The inputData.
     */
    public com.google.protobuf.ByteString getInputData() {
      return inputData_;
    }
    /**
     * <code>bytes input_data = 1;</code>
     * @param value The inputData to set.
     * @return This builder for chaining.
     */
    public Builder setInputData(com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      inputData_ = value;
      onChanged();
      return this;
    }
    /**
     * <code>bytes input_data = 1;</code>
     * @return This builder for chaining.
     */
    public Builder clearInputData() {
      
      inputData_ = getDefaultInstance().getInputData();
      onChanged();
      return this;
    }

    private java.lang.Object datastoreTag_ = "";
    /**
     * <pre>
     * Optional: Datastore tag used to
     * </pre>
     *
     * <code>string datastore_tag = 2;</code>
     * @return The datastoreTag.
     */
    public java.lang.String getDatastoreTag() {
      java.lang.Object ref = datastoreTag_;
      if (!(ref instanceof java.lang.String)) {
        com.google.protobuf.ByteString bs =
            (com.google.protobuf.ByteString) ref;
        java.lang.String s = bs.toStringUtf8();
        datastoreTag_ = s;
        return s;
      } else {
        return (java.lang.String) ref;
      }
    }
    /**
     * <pre>
     * Optional: Datastore tag used to
     * </pre>
     *
     * <code>string datastore_tag = 2;</code>
     * @return The bytes for datastoreTag.
     */
    public com.google.protobuf.ByteString
        getDatastoreTagBytes() {
      java.lang.Object ref = datastoreTag_;
      if (ref instanceof String) {
        com.google.protobuf.ByteString b = 
            com.google.protobuf.ByteString.copyFromUtf8(
                (java.lang.String) ref);
        datastoreTag_ = b;
        return b;
      } else {
        return (com.google.protobuf.ByteString) ref;
      }
    }
    /**
     * <pre>
     * Optional: Datastore tag used to
     * </pre>
     *
     * <code>string datastore_tag = 2;</code>
     * @param value The datastoreTag to set.
     * @return This builder for chaining.
     */
    public Builder setDatastoreTag(
        java.lang.String value) {
      if (value == null) {
    throw new NullPointerException();
  }
  
      datastoreTag_ = value;
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Optional: Datastore tag used to
     * </pre>
     *
     * <code>string datastore_tag = 2;</code>
     * @return This builder for chaining.
     */
    public Builder clearDatastoreTag() {
      
      datastoreTag_ = getDefaultInstance().getDatastoreTag();
      onChanged();
      return this;
    }
    /**
     * <pre>
     * Optional: Datastore tag used to
     * </pre>
     *
     * <code>string datastore_tag = 2;</code>
     * @param value The bytes for datastoreTag to set.
     * @return This builder for chaining.
     */
    public Builder setDatastoreTagBytes(
        com.google.protobuf.ByteString value) {
      if (value == null) {
    throw new NullPointerException();
  }
  checkByteStringIsUtf8(value);
      
      datastoreTag_ = value;
      onChanged();
      return this;
    }
    @java.lang.Override
    public final Builder setUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.setUnknownFields(unknownFields);
    }

    @java.lang.Override
    public final Builder mergeUnknownFields(
        final com.google.protobuf.UnknownFieldSet unknownFields) {
      return super.mergeUnknownFields(unknownFields);
    }


    // @@protoc_insertion_point(builder_scope:tr451_vomci_nbi_message.v1.Action)
  }

  // @@protoc_insertion_point(class_scope:tr451_vomci_nbi_message.v1.Action)
  private static final org.broadband_forum.obbaa.onu.message.gpb.message.Action DEFAULT_INSTANCE;
  static {
    DEFAULT_INSTANCE = new org.broadband_forum.obbaa.onu.message.gpb.message.Action();
  }

  public static org.broadband_forum.obbaa.onu.message.gpb.message.Action getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  private static final com.google.protobuf.Parser<Action>
      PARSER = new com.google.protobuf.AbstractParser<Action>() {
    @java.lang.Override
    public Action parsePartialFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return new Action(input, extensionRegistry);
    }
  };

  public static com.google.protobuf.Parser<Action> parser() {
    return PARSER;
  }

  @java.lang.Override
  public com.google.protobuf.Parser<Action> getParserForType() {
    return PARSER;
  }

  @java.lang.Override
  public org.broadband_forum.obbaa.onu.message.gpb.message.Action getDefaultInstanceForType() {
    return DEFAULT_INSTANCE;
  }

}

