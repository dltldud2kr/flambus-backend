package flambus.app._enum;

public enum FileType {
    ZIP("application/zip", 100 * 1024 * 1024), // 100MB
    PNG("image/png", 10 * 1024 * 1024), // 10MB
    JPEG("image/jpeg", 20 * 1024 * 1024), // 20MB
    PDF("application/pdf", 50 * 1024 * 1024); // 50MB

    private final String contentType;
    private final long maxSize;

    FileType(String contentType, long maxSize) {
        this.contentType = contentType;
        this.maxSize = maxSize;
    }

    public String getContentType() {
        return contentType;
    }

    public long getMaxSize() {
        return maxSize;
    }

    public static FileType fromContentType(String contentType) {
        for (FileType type : FileType.values()) {
            if (type.getContentType().equals(contentType)) {
                return type;
            }
        }
        return null; // 해당 contentType이 없으면 null 반환 or 예외 처리
    }
}

