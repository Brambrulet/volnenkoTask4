package entity;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import lombok.Data;
import lombok.experimental.Accessors;

@MappedSuperclass
@Data
@Accessors(chain = true)
public class IndexedComparableEntity implements Comparable<IndexedComparableEntity> {
    @Id
    @GeneratedValue
    private Long id;

    @Override
    public int compareTo(IndexedComparableEntity other) {
        long result = id - other.id;
        return result < Integer.MAX_VALUE && result > Integer.MIN_VALUE ? (int)result : (int)(result >> 32);
    }

}
