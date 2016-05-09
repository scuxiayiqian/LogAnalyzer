package com.github.scuxiayiqian;

class LogRecord {
    private String userId;
    private String url;

    public LogRecord(String userId, String url) {
        this.userId = userId;
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogRecord logRecord = (LogRecord) o;

        return userId.equals(logRecord.userId) && url.equals(logRecord.url);

    }

    @Override
    public int hashCode() {
        int result = userId.hashCode();
        result = 31 * result + url.hashCode();
        return result;
    }

    public String getUserId() {
        return userId;
    }

    public String getUrl() {
        return url;
    }
}
