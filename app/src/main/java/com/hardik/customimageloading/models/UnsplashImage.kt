package com.hardik.customimageloading.models

import com.google.gson.annotations.SerializedName

data class ProfileImage(

	@field:SerializedName("small")
	val small: String? = null,

	@field:SerializedName("large")
	val large: String? = null,

	@field:SerializedName("medium")
	val medium: String? = null
)

data class Health(

	@field:SerializedName("approved_on")
	val approvedOn: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class UnsplashImage(

	@field:SerializedName("topic_submissions")
	val topicSubmissions: TopicSubmissions? = null,

	@field:SerializedName("current_user_collections")
	val currentUserCollections: List<Any?>? = null,

	@field:SerializedName("color")
	val color: String? = null,

	@field:SerializedName("sponsorship")
	val sponsorship: Any? = null,

	@field:SerializedName("created_at")
	val createdAt: String? = null,

	@field:SerializedName("description")
	val description: Any? = null,

	@field:SerializedName("liked_by_user")
	val likedByUser: Boolean? = null,

	@field:SerializedName("urls")
	val urls: Urls? = null,

	@field:SerializedName("alternative_slugs")
	val alternativeSlugs: AlternativeSlugs? = null,

	@field:SerializedName("alt_description")
	val altDescription: String? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("width")
	val width: Int? = null,

	@field:SerializedName("blur_hash")
	val blurHash: String? = null,

	@field:SerializedName("asset_type")
	val assetType: String? = null,

	@field:SerializedName("links")
	val links: Links? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("promoted_at")
	val promotedAt: String? = null,

	@field:SerializedName("user")
	val user: User? = null,

	@field:SerializedName("slug")
	val slug: String? = null,

	@field:SerializedName("breadcrumbs")
	val breadcrumbs: List<Any?>? = null,

	@field:SerializedName("height")
	val height: Int? = null,

	@field:SerializedName("likes")
	val likes: Int? = null
)

data class TopicSubmissions(

	@field:SerializedName("street-photography")
	val streetPhotography: StreetPhotography? = null,

	@field:SerializedName("fashion-beauty")
	val fashionBeauty: FashionBeauty? = null,

	@field:SerializedName("wallpapers")
	val wallpapers: Wallpapers? = null,

	@field:SerializedName("health")
	val health: Health? = null
)

data class Urls(

	@field:SerializedName("small")
	val small: String? = null,

	@field:SerializedName("small_s3")
	val smallS3: String? = null,

	@field:SerializedName("thumb")
	val thumb: String? = null,

	@field:SerializedName("raw")
	val raw: String? = null,

	@field:SerializedName("regular")
	val regular: String? = null,

	@field:SerializedName("full")
	val full: String? = null
)

data class Social(

	@field:SerializedName("twitter_username")
	val twitterUsername: String? = null,

	@field:SerializedName("paypal_email")
	val paypalEmail: Any? = null,

	@field:SerializedName("instagram_username")
	val instagramUsername: String? = null,

	@field:SerializedName("portfolio_url")
	val portfolioUrl: Any? = null
)

data class User(

	@field:SerializedName("total_photos")
	val totalPhotos: Int? = null,

	@field:SerializedName("accepted_tos")
	val acceptedTos: Boolean? = null,

	@field:SerializedName("social")
	val social: Social? = null,

	@field:SerializedName("twitter_username")
	val twitterUsername: String? = null,

	@field:SerializedName("last_name")
	val lastName: String? = null,

	@field:SerializedName("bio")
	val bio: String? = null,

	@field:SerializedName("total_likes")
	val totalLikes: Int? = null,

	@field:SerializedName("portfolio_url")
	val portfolioUrl: Any? = null,

	@field:SerializedName("profile_image")
	val profileImage: ProfileImage? = null,

	@field:SerializedName("updated_at")
	val updatedAt: String? = null,

	@field:SerializedName("total_promoted_illustrations")
	val totalPromotedIllustrations: Int? = null,

	@field:SerializedName("for_hire")
	val forHire: Boolean? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("total_promoted_photos")
	val totalPromotedPhotos: Int? = null,

	@field:SerializedName("location")
	val location: String? = null,

	@field:SerializedName("links")
	val links: Links? = null,

	@field:SerializedName("total_collections")
	val totalCollections: Int? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("total_illustrations")
	val totalIllustrations: Int? = null,

	@field:SerializedName("first_name")
	val firstName: String? = null,

	@field:SerializedName("instagram_username")
	val instagramUsername: String? = null,

	@field:SerializedName("username")
	val username: String? = null
)

data class StreetPhotography(

	@field:SerializedName("approved_on")
	val approvedOn: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)

data class AlternativeSlugs(

	@field:SerializedName("de")
	val de: String? = null,

	@field:SerializedName("ko")
	val ko: String? = null,

	@field:SerializedName("pt")
	val pt: String? = null,

	@field:SerializedName("ja")
	val ja: String? = null,

	@field:SerializedName("en")
	val en: String? = null,

	@field:SerializedName("it")
	val it: String? = null,

	@field:SerializedName("fr")
	val fr: String? = null,

	@field:SerializedName("es")
	val es: String? = null
)

data class Links(

	@field:SerializedName("followers")
	val followers: String? = null,

	@field:SerializedName("portfolio")
	val portfolio: String? = null,

	@field:SerializedName("following")
	val following: String? = null,

	@field:SerializedName("self")
	val self: String? = null,

	@field:SerializedName("html")
	val html: String? = null,

	@field:SerializedName("photos")
	val photos: String? = null,

	@field:SerializedName("likes")
	val likes: String? = null,

	@field:SerializedName("download")
	val download: String? = null,

	@field:SerializedName("download_location")
	val downloadLocation: String? = null
)

data class Wallpapers(

	@field:SerializedName("status")
	val status: String? = null
)

data class FashionBeauty(

	@field:SerializedName("approved_on")
	val approvedOn: String? = null,

	@field:SerializedName("status")
	val status: String? = null
)
