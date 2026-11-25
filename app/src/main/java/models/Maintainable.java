package models;

public interface Maintainable {
    boolean needsService();
    void performService();
}