// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: tr451_vomci_nbi_message.proto

package org.broadband_forum.obbaa.onu.pm.message.gpb.message;

/**
 * Protobuf type {@code tr451_vomci_nbi_message.v1.Notification}
 */
public final class Notification extends
        com.google.protobuf.GeneratedMessageV3 implements
        // @@protoc_insertion_point(message_implements:tr451_vomci_nbi_message.v1.Notification)
        NotificationOrBuilder {
    public static final int EVENT_TIMESTAMP_FIELD_NUMBER = 1;
    public static final int DATA_FIELD_NUMBER = 2;
    private static final long serialVersionUID = 0L;
    // @@protoc_insertion_point(class_scope:tr451_vomci_nbi_message.v1.Notification)
    private static final org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification DEFAULT_INSTANCE;
    private static final com.google.protobuf.Parser<Notification>
            PARSER = new com.google.protobuf.AbstractParser<Notification>() {
        @java.lang.Override
        public Notification parsePartialFrom(
                com.google.protobuf.CodedInputStream input,
                com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                throws com.google.protobuf.InvalidProtocolBufferException {
            return new Notification(input, extensionRegistry);
        }
    };

    static {
        DEFAULT_INSTANCE = new org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification();
    }

    private volatile java.lang.Object eventTimestamp_;
    private com.google.protobuf.ByteString data_;
    private byte memoizedIsInitialized = -1;

    // Use Notification.newBuilder() to construct.
    private Notification(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
        super(builder);
    }

    private Notification() {
        eventTimestamp_ = "";
        data_ = com.google.protobuf.ByteString.EMPTY;
    }

    private Notification(
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
                        java.lang.String s = input.readStringRequireUtf8();

                        eventTimestamp_ = s;
                        break;
                    }
                    case 18: {

                        data_ = input.readBytes();
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
        return org.broadband_forum.obbaa.onu.pm.message.gpb.message.Tr451VomciNbiMessage.internal_static_tr451_vomci_nbi_message_v1_Notification_descriptor;
    }

    public static org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification parseFrom(
            java.nio.ByteBuffer data)
            throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }

    public static org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification parseFrom(
            java.nio.ByteBuffer data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }

    public static org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification parseFrom(
            com.google.protobuf.ByteString data)
            throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }

    public static org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification parseFrom(
            com.google.protobuf.ByteString data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }

    public static org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification parseFrom(byte[] data)
            throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data);
    }

    public static org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification parseFrom(
            byte[] data,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws com.google.protobuf.InvalidProtocolBufferException {
        return PARSER.parseFrom(data, extensionRegistry);
    }

    public static org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification parseFrom(java.io.InputStream input)
            throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseWithIOException(PARSER, input);
    }

    public static org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification parseFrom(
            java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification parseDelimitedFrom(java.io.InputStream input)
            throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseDelimitedWithIOException(PARSER, input);
    }

    public static org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification parseDelimitedFrom(
            java.io.InputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }

    public static org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification parseFrom(
            com.google.protobuf.CodedInputStream input)
            throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseWithIOException(PARSER, input);
    }

    public static org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification parseFrom(
            com.google.protobuf.CodedInputStream input,
            com.google.protobuf.ExtensionRegistryLite extensionRegistry)
            throws java.io.IOException {
        return com.google.protobuf.GeneratedMessageV3
                .parseWithIOException(PARSER, input, extensionRegistry);
    }

    public static Builder newBuilder() {
        return DEFAULT_INSTANCE.toBuilder();
    }

    public static Builder newBuilder(org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification prototype) {
        return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }

    public static org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification getDefaultInstance() {
        return DEFAULT_INSTANCE;
    }

    public static com.google.protobuf.Parser<Notification> parser() {
        return PARSER;
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
            UnusedPrivateParameter unused) {
        return new Notification();
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
        return this.unknownFields;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
    internalGetFieldAccessorTable() {
        return org.broadband_forum.obbaa.onu.pm.message.gpb.message.Tr451VomciNbiMessage.internal_static_tr451_vomci_nbi_message_v1_Notification_fieldAccessorTable
                .ensureFieldAccessorsInitialized(
                        org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification.class, org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification.Builder.class);
    }

    /**
     * <code>string event_timestamp = 1;</code>
     *
     * @return The eventTimestamp.
     */
    public java.lang.String getEventTimestamp() {
        java.lang.Object ref = eventTimestamp_;
        if (ref instanceof java.lang.String) {
            return (java.lang.String) ref;
        } else {
            com.google.protobuf.ByteString bs =
                    (com.google.protobuf.ByteString) ref;
            java.lang.String s = bs.toStringUtf8();
            eventTimestamp_ = s;
            return s;
        }
    }

    /**
     * <code>string event_timestamp = 1;</code>
     *
     * @return The bytes for eventTimestamp.
     */
    public com.google.protobuf.ByteString
    getEventTimestampBytes() {
        java.lang.Object ref = eventTimestamp_;
        if (ref instanceof java.lang.String) {
            com.google.protobuf.ByteString b =
                    com.google.protobuf.ByteString.copyFromUtf8(
                            (java.lang.String) ref);
            eventTimestamp_ = b;
            return b;
        } else {
            return (com.google.protobuf.ByteString) ref;
        }
    }

    /**
     * <code>bytes data = 2;</code>
     *
     * @return The data.
     */
    public com.google.protobuf.ByteString getData() {
        return data_;
    }

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
        if (!getEventTimestampBytes().isEmpty()) {
            com.google.protobuf.GeneratedMessageV3.writeString(output, 1, eventTimestamp_);
        }
        if (!data_.isEmpty()) {
            output.writeBytes(2, data_);
        }
        unknownFields.writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
        int size = memoizedSize;
        if (size != -1) return size;

        size = 0;
        if (!getEventTimestampBytes().isEmpty()) {
            size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, eventTimestamp_);
        }
        if (!data_.isEmpty()) {
            size += com.google.protobuf.CodedOutputStream
                    .computeBytesSize(2, data_);
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
        if (!(obj instanceof org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification)) {
            return super.equals(obj);
        }
        org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification other = (org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification) obj;

        if (!getEventTimestamp()
                .equals(other.getEventTimestamp())) return false;
        if (!getData()
                .equals(other.getData())) return false;
        return unknownFields.equals(other.unknownFields);
    }

    @java.lang.Override
    public int hashCode() {
        if (memoizedHashCode != 0) {
            return memoizedHashCode;
        }
        int hash = 41;
        hash = (19 * hash) + getDescriptor().hashCode();
        hash = (37 * hash) + EVENT_TIMESTAMP_FIELD_NUMBER;
        hash = (53 * hash) + getEventTimestamp().hashCode();
        hash = (37 * hash) + DATA_FIELD_NUMBER;
        hash = (53 * hash) + getData().hashCode();
        hash = (29 * hash) + unknownFields.hashCode();
        memoizedHashCode = hash;
        return hash;
    }

    @java.lang.Override
    public Builder newBuilderForType() {
        return newBuilder();
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

    @java.lang.Override
    public com.google.protobuf.Parser<Notification> getParserForType() {
        return PARSER;
    }

    @java.lang.Override
    public org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification getDefaultInstanceForType() {
        return DEFAULT_INSTANCE;
    }

    /**
     * Protobuf type {@code tr451_vomci_nbi_message.v1.Notification}
     */
    public static final class Builder extends
            com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
            // @@protoc_insertion_point(builder_implements:tr451_vomci_nbi_message.v1.Notification)
            org.broadband_forum.obbaa.onu.pm.message.gpb.message.NotificationOrBuilder {
        private java.lang.Object eventTimestamp_ = "";
        private com.google.protobuf.ByteString data_ = com.google.protobuf.ByteString.EMPTY;

        // Construct using org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification.newBuilder()
        private Builder() {
            maybeForceBuilderInitialization();
        }

        private Builder(
                com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
            super(parent);
            maybeForceBuilderInitialization();
        }

        public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
            return org.broadband_forum.obbaa.onu.pm.message.gpb.message.Tr451VomciNbiMessage.internal_static_tr451_vomci_nbi_message_v1_Notification_descriptor;
        }

        @java.lang.Override
        protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
            return org.broadband_forum.obbaa.onu.pm.message.gpb.message.Tr451VomciNbiMessage.internal_static_tr451_vomci_nbi_message_v1_Notification_fieldAccessorTable
                    .ensureFieldAccessorsInitialized(
                            org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification.class, org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification.Builder.class);
        }

        private void maybeForceBuilderInitialization() {
            if (com.google.protobuf.GeneratedMessageV3
                    .alwaysUseFieldBuilders) {
            }
        }

        @java.lang.Override
        public Builder clear() {
            super.clear();
            eventTimestamp_ = "";

            data_ = com.google.protobuf.ByteString.EMPTY;

            return this;
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.Descriptor
        getDescriptorForType() {
            return org.broadband_forum.obbaa.onu.pm.message.gpb.message.Tr451VomciNbiMessage.internal_static_tr451_vomci_nbi_message_v1_Notification_descriptor;
        }

        @java.lang.Override
        public org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification getDefaultInstanceForType() {
            return org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification.getDefaultInstance();
        }

        @java.lang.Override
        public org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification build() {
            org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification result = buildPartial();
            if (!result.isInitialized()) {
                throw newUninitializedMessageException(result);
            }
            return result;
        }

        @java.lang.Override
        public org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification buildPartial() {
            org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification result = new org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification(this);
            result.eventTimestamp_ = eventTimestamp_;
            result.data_ = data_;
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
            if (other instanceof org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification) {
                return mergeFrom((org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification) other);
            } else {
                super.mergeFrom(other);
                return this;
            }
        }

        public Builder mergeFrom(org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification other) {
            if (other == org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification.getDefaultInstance())
                return this;
            if (!other.getEventTimestamp().isEmpty()) {
                eventTimestamp_ = other.eventTimestamp_;
                onChanged();
            }
            if (other.getData() != com.google.protobuf.ByteString.EMPTY) {
                setData(other.getData());
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
            org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification parsedMessage = null;
            try {
                parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
            } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                parsedMessage = (org.broadband_forum.obbaa.onu.pm.message.gpb.message.Notification) e.getUnfinishedMessage();
                throw e.unwrapIOException();
            } finally {
                if (parsedMessage != null) {
                    mergeFrom(parsedMessage);
                }
            }
            return this;
        }

        /**
         * <code>string event_timestamp = 1;</code>
         *
         * @return The eventTimestamp.
         */
        public java.lang.String getEventTimestamp() {
            java.lang.Object ref = eventTimestamp_;
            if (!(ref instanceof java.lang.String)) {
                com.google.protobuf.ByteString bs =
                        (com.google.protobuf.ByteString) ref;
                java.lang.String s = bs.toStringUtf8();
                eventTimestamp_ = s;
                return s;
            } else {
                return (java.lang.String) ref;
            }
        }

        /**
         * <code>string event_timestamp = 1;</code>
         *
         * @param value The eventTimestamp to set.
         * @return This builder for chaining.
         */
        public Builder setEventTimestamp(
                java.lang.String value) {
            if (value == null) {
                throw new NullPointerException();
            }

            eventTimestamp_ = value;
            onChanged();
            return this;
        }

        /**
         * <code>string event_timestamp = 1;</code>
         *
         * @return The bytes for eventTimestamp.
         */
        public com.google.protobuf.ByteString
        getEventTimestampBytes() {
            java.lang.Object ref = eventTimestamp_;
            if (ref instanceof String) {
                com.google.protobuf.ByteString b =
                        com.google.protobuf.ByteString.copyFromUtf8(
                                (java.lang.String) ref);
                eventTimestamp_ = b;
                return b;
            } else {
                return (com.google.protobuf.ByteString) ref;
            }
        }

        /**
         * <code>string event_timestamp = 1;</code>
         *
         * @param value The bytes for eventTimestamp to set.
         * @return This builder for chaining.
         */
        public Builder setEventTimestampBytes(
                com.google.protobuf.ByteString value) {
            if (value == null) {
                throw new NullPointerException();
            }
            checkByteStringIsUtf8(value);

            eventTimestamp_ = value;
            onChanged();
            return this;
        }

        /**
         * <code>string event_timestamp = 1;</code>
         *
         * @return This builder for chaining.
         */
        public Builder clearEventTimestamp() {

            eventTimestamp_ = getDefaultInstance().getEventTimestamp();
            onChanged();
            return this;
        }

        /**
         * <code>bytes data = 2;</code>
         *
         * @return The data.
         */
        public com.google.protobuf.ByteString getData() {
            return data_;
        }

        /**
         * <code>bytes data = 2;</code>
         *
         * @param value The data to set.
         * @return This builder for chaining.
         */
        public Builder setData(com.google.protobuf.ByteString value) {
            if (value == null) {
                throw new NullPointerException();
            }

            data_ = value;
            onChanged();
            return this;
        }

        /**
         * <code>bytes data = 2;</code>
         *
         * @return This builder for chaining.
         */
        public Builder clearData() {

            data_ = getDefaultInstance().getData();
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


        // @@protoc_insertion_point(builder_scope:tr451_vomci_nbi_message.v1.Notification)
    }

}
