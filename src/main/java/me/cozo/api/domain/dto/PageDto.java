package me.cozo.api.domain.dto;

import java.io.Serializable;

public record PageDto<T, P>(T item, P prevCursor, P nextCursor) implements Serializable {
}
