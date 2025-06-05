package ru.mdemidkin.client.dto;

public record PagingDto(
        int pageNumber,
        int pageSize,
        boolean hasNext,
        boolean hasPrevious
) {
}
