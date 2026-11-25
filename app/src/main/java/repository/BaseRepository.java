package repository;
import java.util.List;

public interface BaseRepository<T> {
    List<T> getAll();
    T getById(int id);
    boolean save(T entity);
    boolean update(T entity);
    boolean delete(int id);
}