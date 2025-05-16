package ru.mdemidkin.intershop.dto;

public record Paging(
        int pageNumber,
        int pageSize,
        boolean hasNext,
        boolean hasPrevious
) {
}
