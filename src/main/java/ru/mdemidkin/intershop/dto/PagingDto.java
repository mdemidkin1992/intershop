package ru.mdemidkin.intershop.dto;

public record PagingDto(
        int pageNumber,
        int pageSize,
        boolean hasNext,
        boolean hasPrevious
) {
}
