package com.github.scuxiayiqian;

class UrlTransit {
    private String source;
    private String target;

    public UrlTransit(String source, String target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UrlTransit that = (UrlTransit) o;

        return source.equals(that.source) && target.equals(that.target);

    }

    @Override
    public int hashCode() {
        int result = source.hashCode();
        result = 31 * result + target.hashCode();
        return result;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }
}
