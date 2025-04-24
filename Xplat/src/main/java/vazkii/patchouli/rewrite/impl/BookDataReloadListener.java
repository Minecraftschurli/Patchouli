package vazkii.patchouli.rewrite.impl;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import vazkii.patchouli.rewrite.api.Loader;
import vazkii.patchouli.rewrite.api.data.BookCategory;
import vazkii.patchouli.rewrite.api.data.BookContent;
import vazkii.patchouli.rewrite.api.data.BookEntry;
import vazkii.patchouli.rewrite.api.data.BookTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BookDataReloadListener implements PreparableReloadListener {
    public static final String DEFAULT_LANG = "en_us";
    public static final BookDataReloadListener INSTANCE = new BookDataReloadListener();

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Pattern ID_READER = Pattern.compile(
            "(?<bookId>[a-z0-9._-]+)" +
                    "/(?<lang>[a-z0-9._-]+)" +
                    "/(?<folder>[a-z0-9._-]+)" +
                    "/(?<id>[a-z0-9/._-]+)" +
                    "\\.(?<extension>[a-z])"
    );

    private final Gson gson = new GsonBuilder().create();
    private final List<Loader<?, BookEntry.Builder>> entryLoaders = List.of(new JsonLoader<>(gson, BookEntry.Builder.CODEC));
    private final Loader<?, BookCategory.Builder> categoryLoader = new JsonLoader<>(gson, BookCategory.Builder.CODEC);
    private final Loader<?, BookTemplate.Builder> templateLoader = new JsonLoader<>(gson, BookTemplate.Builder.CODEC);
    private final String directory;
    private Map<ResourceLocation, RawBookData> rawData;
    private ImmutableMap<ResourceLocation, BookContent> books;

    private BookDataReloadListener() {
        this.directory = "patchouli_books";
    }

    @Nullable
    public BookContent getBook(ResourceLocation id) {
        if (this.books == null) {
            throw new IllegalStateException("Books accessed before they were built");
        }
        return this.books.get(id);
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager resourceManager, ProfilerFiller profiler1, ProfilerFiller profiler2, Executor executor1, Executor executor2) {
        this.rawData = null;
        return CompletableFuture
                .supplyAsync(() -> {
                    profiler1.push("patchouli_books");
                    return resourceManager.listResources(this.directory, $ -> true);
                }, executor1)
                .thenComposeAsync(resources -> {
                    Map<ResourceLocation, RawBookData> books = new ConcurrentHashMap<>();
                    return CompletableFuture.allOf(resources.entrySet().stream().map(entry -> CompletableFuture.runAsync(() -> parse(resourceManager, profiler1, entry, books), executor1)).toArray(CompletableFuture[]::new))
                            .thenApply($ -> {
                                profiler1.pop();
                                return books;
                            });
                }, executor1)
                .thenCompose(barrier::wait)
                .thenAcceptAsync(rawData -> this.rawData = rawData, executor2);
    }

    protected void build(HolderLookup.Provider registries, String lang) {
        if (this.rawData == null) {
            LOGGER.warn("BookDataReloadListener.build called too early, no raw data to build present!");
            return;
        }
        ImmutableMap.Builder<ResourceLocation, BookContent> builder = ImmutableMap.builder();
        for (RawBookData rawBookData : this.rawData.values()) {
            ResourceLocation bookId = rawBookData.bookId();
            try {
                RawBookContent rawBookContent = rawBookData.languages().get(lang);
                if (rawBookContent == null) {
                    rawBookContent = rawBookData.languages().get(DEFAULT_LANG);
                }
                if (rawBookContent == null) {
                    throw new BookBuildException("No content for current language or fallback language present");
                }
                BookContent content = rawBookContent.build(registries);
                builder.put(bookId, content);
            } catch (BookBuildException e) {
                LOGGER.error("Failed to build book data for book '{}'", bookId, e);
            }
        }
        this.books = builder.build();
    }

    private void parse(ResourceManager resourceManager, ProfilerFiller profiler, Map.Entry<ResourceLocation, Resource> entry, Map<ResourceLocation, RawBookData> books) {
        ResourceLocation file = entry.getKey();
        String path = file.getPath();
        Matcher matcher = ID_READER.matcher(path);
        if (!matcher.matches()) {
            LOGGER.warn("Skipping file '{}'", file);
            return;
        }
        profiler.push("parse");
        ResourceLocation bookId = file.withPath(matcher.group("bookId"));
        String lang = matcher.group("lang");
        RawBookContent bookContent = books.computeIfAbsent(bookId, RawBookData::new).languages().computeIfAbsent(lang, RawBookContent::new);
        String folder = matcher.group("folder");
        String extension = matcher.group("extension");
        ResourceLocation id = file.withPath(matcher.group("id"));
        Resource resource = entry.getValue();
        switch (folder) {
            case "entries" -> parse(resourceManager, bookId, lang, "entry", id, extension, resource, profiler, this::getEntryLoader, bookContent.entries());
            case "categories" -> parse(resourceManager, bookId, lang, "category", id, extension, resource, profiler, this::getCategoryLoader, bookContent.categories());
            case "templates" -> parse(resourceManager, bookId, lang, "template", id, extension, resource, profiler, this::getTemplateLoader, bookContent.templates());
            default -> LOGGER.warn("Skipping file '{}' in unknown folder '{}'", id.withSuffix("." + extension), folder);
        }
        profiler.pop();
    }

    private static <T> void parse(ResourceManager resourceManager, ResourceLocation bookId, String lang, String thing, ResourceLocation id, String extension, Resource resource, ProfilerFiller profiler, BiFunction<ResourceLocation, String, @Nullable Loader<?, T>> loaderGetter, Map<ResourceLocation, RawData<?, T>> map) {
        profiler.push(thing);
        profiler.push("get_loader");
        Loader<?, T> loader = loaderGetter.apply(id, extension);
        profiler.pop();
        if (loader == null) {
            LOGGER.warn("Skipping {} file '{}' in language '{}' of book '{}' as there is no loader handling it", thing, id.withSuffix("." + extension), lang, bookId);
            return;
        }
        try {
            map.put(id, readWithLoader(resourceManager, id, extension, resource, loader));
        } catch (IllegalArgumentException | IOException | JsonParseException e) {
            ResourceLocation file = id.withPath(path -> bookId.getPath() + "/" + lang + "/" + path + "." + extension);
            LOGGER.error("Couldn't parse data file {} from {}", id, file, e);
        }
        profiler.pop();
    }

    private static <TRaw, T> @NotNull RawData<TRaw, T> readWithLoader(ResourceManager resourceManager, ResourceLocation id, String extension, Resource resource, Loader<TRaw, T> loader) throws IOException {
        TRaw readData = loader.read(id, extension, resource, resourceManager);
        return new RawData<>(id, resource.sourcePackId(), loader, readData);
    }

    @Nullable
    private Loader<?, BookTemplate.Builder> getTemplateLoader(ResourceLocation id, String extension) {
        return  "json".equals(extension) ? templateLoader : null;
    }

    @Nullable
    private Loader<?, BookCategory.Builder> getCategoryLoader(ResourceLocation id, String extension) {
        return  "json".equals(extension) ? categoryLoader : null;
    }

    @Nullable
    private Loader<?, BookEntry.Builder> getEntryLoader(ResourceLocation id, String extension) {
        for (Loader<?, BookEntry.Builder> loader : entryLoaders) {
            if (loader.handles(id, extension)) {
                return loader;
            }
        }
        return null;
    }

    private static List<ResourceLocation> topologicalSort(Map<ResourceLocation, Pair<BookCategory.Builder, String>> map) {
        try {
            return Util.topologicalSort(map, v -> v.getFirst().getParent());
        } catch (Util.SortException e) {
            switch (e) {
                case Util.SortException.Loop l ->
                        throw new BookBuildException("Loop detected! The category '" + l.getKey() + "' is part of a cycle.");
                case Util.SortException.Missing m ->
                        throw new BookBuildException("No category '" + m.getReferenced() + "' present in graph but was referenced as parent by '" + m.getReferencedBy() + "'");
            }
        }
    }

    private static class BookBuildException extends RuntimeException {
        BookBuildException(String message) {
            super(message);
        }
    }

    protected record RawData<TRaw, T>(ResourceLocation id, String addedBy, Loader<TRaw, T> loader, TRaw data) {
        private DataResult<Map<ResourceLocation, T>> build(HolderLookup.Provider registries) {
            return loader().parse(id(), data(), registries);
        }
    }

    protected record RawBookContent(String lang, Map<ResourceLocation, RawData<?, BookCategory.Builder>> categories, Map<ResourceLocation, RawData<?, BookEntry.Builder>> entries, Map<ResourceLocation, RawData<?, BookTemplate.Builder>> templates) {
        public RawBookContent(String lang) {
            this(lang, new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
        }

        private BookContent build(HolderLookup.Provider registries) {
            RawBookContent rawBookContent = this;
            BookContent.Builder contentBuilder = BookContent.builder();
            Map<ResourceLocation, Pair<BookTemplate.Builder, String>> templates = rawBookContent.buildTemplates(registries).getOrThrow(BookBuildException::new);
            Map<ResourceLocation, BookTemplate> builtTemplates = new HashMap<>();
            for (var builderEntry : templates.entrySet()) {
                var id  = builderEntry.getKey();
                var builder = builderEntry.getValue().getFirst();
                String addedBy = builderEntry.getValue().getSecond();
                builtTemplates.put(id, builder.build(addedBy));
            }
            Map<ResourceLocation, Pair<BookCategory.Builder, String>> categories = rawBookContent.buildCategories(registries).getOrThrow(BookBuildException::new);
            // TODO add template resolving
            Map<ResourceLocation, Pair<BookEntry.Builder, String>> entries = rawBookContent.buildEntries(registries).getOrThrow(BookBuildException::new);
            for (var builderEntry : entries.entrySet()) {
                var id  = builderEntry.getKey();
                var builder = builderEntry.getValue().getFirst();
                String addedBy = builderEntry.getValue().getSecond();
                ResourceLocation categoryId = builder.getCategoryId();
                BookCategory.Builder category = categories.get(categoryId).getFirst();
                if (category == null) {
                    throw new BookBuildException("No category '" + categoryId + "' present for entry '" + id + "'");
                }
                BookEntry entry = builder.build(addedBy);
                category.addEntry(id, entry);
                contentBuilder.addEntry(id, entry);
            }
            for (ResourceLocation id : topologicalSort(categories)) {
                var value = categories.get(id);
                var builder = value.getFirst();
                String addedBy = value.getSecond();
                BookCategory category = builder.build(addedBy);
                var parent = builder.getParent();
                parent.ifPresent(p -> categories.get(p).getFirst().addChild(id, category));
                contentBuilder.addCategory(id, category);
            }
            return contentBuilder.build();
        }

        private DataResult<Map<ResourceLocation, Pair<BookCategory.Builder, String>>> buildCategories(HolderLookup.Provider registries) {
            return RawBookContent.buildThing(registries, categories());
        }

        private DataResult<Map<ResourceLocation, Pair<BookEntry.Builder, String>>> buildEntries(HolderLookup.Provider registries) {
            return RawBookContent.buildThing(registries, entries());
        }

        private DataResult<Map<ResourceLocation, Pair<BookTemplate.Builder, String>>> buildTemplates(HolderLookup.Provider registries) {
            return RawBookContent.buildThing(registries, templates());
        }

        private static <T> DataResult<Map<ResourceLocation, Pair<T, String>>> buildThing(HolderLookup.Provider registries, Map<ResourceLocation, RawData<?, T>> rawData) {
            Map<ResourceLocation, Pair<T, String>> content = new HashMap<>();
            DataResult<Unit> result = DataResult.success(Unit.INSTANCE);
            for (RawData<?, T> raw : rawData.values()) {
                DataResult<Map<ResourceLocation, T>> r = raw.build(registries);
                r.resultOrPartial().map(m -> m.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> Pair.of(entry.getValue(), raw.addedBy())))).ifPresent(content::putAll);
                result = result.apply2stable((a, b) -> a, r);
            }
            return result.map($ -> content).setPartial(content);
        }
    }

    protected record RawBookData(ResourceLocation bookId, Map<String, RawBookContent> languages) {
        RawBookData(ResourceLocation bookId) {
            this(bookId, new ConcurrentHashMap<>());
        }
    }
}
