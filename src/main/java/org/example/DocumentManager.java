package org.example;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * For implement this task focus on clear code, and make this solution as simple readable as possible
 * Don't worry about performance, concurrency, etc
 * You can use in Memory collection for sore data
 * <p>
 * Please, don't change class name, and signature for methods save, search, findById
 * Implementations should be in a single class
 * This class could be auto tested
 */
public class DocumentManager {

    private final Map<String, Document> storage = new HashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1); // AtomicLong used for generating unique ID in a thread-safe env

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        String documentId = Optional.ofNullable(document.getId()).orElseGet(this::generateUniqueId);
        System.out.println("Document ID: " + documentId);
        document.setId(documentId);
        document.setCreated(Instant.now());
        storage.put(documentId, document);
        return document;
    }

    private String generateUniqueId() {
        return String.valueOf(idCounter.getAndIncrement());
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return storage.values().stream()
                .filter(doc -> matches(doc, request))
                .collect(Collectors.toList());
    }

    private boolean matches(Document document, SearchRequest request) {
        return matchesTitlePrefixes(document, request.getTitlePrefixes()) &&
                matchesContainsContents(document, request.getContainsContents()) &&
                matchesAuthorIds(document, request.getAuthorIds()) &&
                matchesCreatedFrom(document, request.getCreatedFrom()) &&
                matchesCreatedTo(document, request.getCreatedTo());
    }

    private boolean matchesTitlePrefixes(Document document, List<String> titlePrefixes) {
        return titlePrefixes == null || titlePrefixes.isEmpty() ||
                titlePrefixes.stream().anyMatch(prefix -> document.getTitle().startsWith(prefix));
    }

    private boolean matchesContainsContents(Document document, List<String> containsContents) {
        return containsContents == null || containsContents.isEmpty() ||
                containsContents.stream().anyMatch(content -> document.getContent().contains(content));
    }

    private boolean matchesAuthorIds(Document document, List<String> authorIds) {
        return authorIds == null || authorIds.isEmpty() ||
                authorIds.contains(document.getAuthor().getId());
    }

    private boolean matchesCreatedFrom(Document document, Instant createdFrom) {
        return createdFrom == null || !document.getCreated().isBefore(createdFrom);
    }

    private boolean matchesCreatedTo(Document document, Instant createdTo) {
        return createdTo == null || !document.getCreated().isAfter(createdTo);
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}
