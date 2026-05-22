package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "code") val code: Int,
    @Json(name = "msg") val msg: String,
    @Json(name = "data") val data: T?
)

@JsonClass(generateAdapter = true)
data class StorageSourceResponse(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "type") val type: String,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class StorageProviderType(
    @Json(name = "type") val type: String,
    @Json(name = "display_name") val displayName: String,
    @Json(name = "config_fields") val configFields: List<StorageConfigField>
)

@JsonClass(generateAdapter = true)
data class StorageConfigField(
    @Json(name = "name") val name: String,
    @Json(name = "type") val type: String,
    @Json(name = "required") val required: Boolean,
    @Json(name = "default") val defaultVal: Any?,
    @Json(name = "description") val description: String?
)

@JsonClass(generateAdapter = true)
data class AddStorageSourceRequest(
    @Json(name = "name") val name: String,
    @Json(name = "type") val type: String,
    @Json(name = "config") val config: Map<String, Any>
)

@JsonClass(generateAdapter = true)
data class StoragePreviewRequest(
    @Json(name = "type") val type: String,
    @Json(name = "config") val config: Map<String, Any>,
    @Json(name = "target_path") val targetPath: String = "/",
    @Json(name = "dirs_only") val dirsOnly: Boolean = true
)

@JsonClass(generateAdapter = true)
data class StoragePreviewData(
    @Json(name = "storage_type") val storageType: String,
    @Json(name = "current_path") val currentPath: String,
    @Json(name = "parent_path") val parentPath: String?,
    @Json(name = "items") val items: List<StorageBrowseItem>?
)

@JsonClass(generateAdapter = true)
data class StorageBrowseItem(
    @Json(name = "name") val name: String,
    @Json(name = "path") val path: String,
    @Json(name = "type") val type: String,
    @Json(name = "size") val size: Long?
)

@JsonClass(generateAdapter = true)
data class ScanActiveItem(
    @Json(name = "label") val label: String?,
    @Json(name = "path") val path: String?,
    @Json(name = "file_count") val fileCount: Int?,
    @Json(name = "started_at") val startedAt: String?
)

@JsonClass(generateAdapter = true)
data class ScanStatus(
    @Json(name = "status") val status: String,
    @Json(name = "phase") val phase: String,
    @Json(name = "current_source") val currentSource: String?,
    @Json(name = "current_path") val currentPath: String?,
    @Json(name = "current_item") val currentItem: String?,
    @Json(name = "current_file") val currentFile: String?,
    @Json(name = "total_items") val totalItems: Int?,
    @Json(name = "processed_items") val processedItems: Int?,
    @Json(name = "total_items_known") val totalItemsKnown: Boolean?,
    @Json(name = "discovered_files") val discoveredFiles: Int?,
    @Json(name = "total_files") val totalFiles: Int?,
    @Json(name = "total_files_known") val totalFilesKnown: Boolean?,
    @Json(name = "processed_files") val processedFiles: Int?,
    @Json(name = "indexed_dirs") val indexedDirs: Int?,
    @Json(name = "active_items") val activeItems: List<ScanActiveItem>?
)

@JsonClass(generateAdapter = true)
data class MovieSimple(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "poster_url") val posterUrl: String?,
    @Json(name = "poster_asset_url") val posterAssetUrl: String?,
    @Json(name = "rating") val rating: Double?,
    @Json(name = "year") val year: Int?,
    @Json(name = "quality_badge") val qualityBadge: String?
)

@JsonClass(generateAdapter = true)
data class HomepageHero(
    @Json(name = "mode") val mode: String?,
    @Json(name = "movie") val movie: MovieSimple? // We can just use MovieSimple here since we just need basic info for hero
)

@JsonClass(generateAdapter = true)
data class HomepageSection(
    @Json(name = "key") val key: String,
    @Json(name = "title") val title: String,
    @Json(name = "limit") val limit: Int?,
    @Json(name = "items") val items: List<MovieSimple>?
)

@JsonClass(generateAdapter = true)
data class HomepageData(
    @Json(name = "hero") val hero: HomepageHero?,
    @Json(name = "sections") val sections: List<HomepageSection>?
)

