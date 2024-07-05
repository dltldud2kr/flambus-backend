package flambus.app._enum;

public enum AttachmentType {
    REVIEW("review"),
    FEED("feed");
    // 추후에 추가될 다른 업로드 타입들

    private final String type;

    AttachmentType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static AttachmentType fromString(String text) {
        for (AttachmentType uploadType : AttachmentType.values()) {
            if (uploadType.type.equalsIgnoreCase(text)) {
                return uploadType;
            }
        }
        throw new IllegalArgumentException("Invalid uploadType: " + text);
    }
}
