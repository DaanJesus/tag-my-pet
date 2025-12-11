package com.tagmypet.data.api

import com.squareup.moshi.Json
import com.tagmypet.data.model.Notification
import com.tagmypet.data.model.Pet
import com.tagmypet.data.model.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // --- AUTH ---
    @POST("auth/login")
    suspend fun login(@Body request: Map<String, String>): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: Map<String, String>): Response<AuthResponse>

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body request: Map<String, String>): Response<Unit>

    @PATCH("auth/fcm-token")
    suspend fun updateFcmToken(@Body request: Map<String, String>): Response<Unit>

    // --- USER ---
    @GET("users/me")
    suspend fun getMe(): Response<UserWrapper>

    @PATCH("users/update-me")
    suspend fun updateProfile(@Body request: Map<String, String>): Response<UserWrapper>

    @DELETE("users/delete-me")
    suspend fun deleteAccount(): Response<Unit>

    // --- USER (PUBLIC & SOCIAL) ---
    @GET("users/public/{userId}")
    suspend fun getPublicProfile(@Path("userId") userId: String): Response<PublicProfileResponse>

    @POST("users/{userId}/follow")
    suspend fun toggleFollow(@Path("userId") userId: String): Response<FollowResponse>

    // --- SEARCH (BUSCA GLOBAL) ---
    @GET("search")
    suspend fun searchGlobal(@Query("q") query: String): Response<SearchResponse>

    // --- REPORT (DENÚNCIAS) ---
    @POST("reports")
    suspend fun createReport(@Body request: Map<String, String>): Response<Unit>

    // --- NOTIFICATIONS (ESTAS ERAM AS QUE FALTAVAM) ---
    @GET("notifications")
    suspend fun getNotifications(): Response<NotificationResponse>

    @PATCH("notifications/{id}/read")
    suspend fun markAsRead(@Path("id") id: String): Response<Unit>

    @PATCH("notifications/read-all")
    suspend fun markAllAsRead(): Response<Unit>

    // --- PETS & SAÚDE ---
    @GET("pets")
    suspend fun getMyPets(): Response<PetsResponse>

    @GET("pets/tag/{id}") // Rota pública do scanner
    suspend fun getPetByTag(@Path("id") id: String): Response<PetWrapper>

    @Multipart
    @POST("pets")
    suspend fun createPet(
        @Part("name") name: RequestBody,
        @Part("species") species: RequestBody,
        @Part("breed") breed: RequestBody,
        @Part("age") age: RequestBody,
        @Part("weight") weight: RequestBody,
        @Part("contactPhone") contactPhone: RequestBody,
        // Listas como Multipart (array)
        @Part allergies: List<MultipartBody.Part>,
        @Part medications: List<MultipartBody.Part>,
        @Part image: MultipartBody.Part?,
    ): Response<PetWrapper>

    @PATCH("pets/{id}/status")
    suspend fun togglePetStatus(
        @Path("id") id: String,
        @Body request: Map<String, Boolean>,
    ): Response<PetWrapper>

    @DELETE("pets/{id}")
    suspend fun deletePet(@Path("id") id: String): Response<Unit>

    // Vacinas
    @POST("pets/{id}/vaccines")
    suspend fun addVaccine(
        @Path("id") id: String,
        @Body vaccine: Map<String, Any>,
    ): Response<VaccineResponse>

    @DELETE("pets/{id}/vaccines/{vaccineId}")
    suspend fun removeVaccine(
        @Path("id") id: String,
        @Path("vaccineId") vaccineId: String,
    ): Response<VaccineResponse>

    // --- CHAT ---
    @GET("chats/inbox")
    suspend fun getConversations(): Response<ConversationsResponse>

    @GET("chats/{userId}") // userId é o ID do outro usuário
    suspend fun getMessages(@Path("userId") userId: String): Response<MessagesResponse>

    @POST("chats")
    suspend fun sendMessage(@Body request: Map<String, String>): Response<MessageResponse>

    // --- POSTS (FEED) ---
    @GET("posts")
    suspend fun getFeed(): Response<FeedResponse>

    @GET("posts/user/{userId}")
    suspend fun getUserPosts(@Path("userId") userId: String): Response<FeedResponse>

    @Multipart
    @POST("posts")
    suspend fun createPost(
        @Part("content") content: RequestBody,
        @Part image: MultipartBody.Part?,
        @Part("isLostAlert") isLostAlert: Boolean,
    ): Response<Unit>

    @PATCH("posts/{id}/like")
    suspend fun toggleLike(@Path("id") id: String): Response<Unit>

    // --- COMENTÁRIOS (CRÍTICO) ---
    @GET("posts/{postId}/comments")
    suspend fun getComments(@Path("postId") postId: String): Response<CommentsResponse>

    // FUNÇÃO QUE ESTAVA FALTANDO
    @POST("posts/{postId}/comments")
    suspend fun addComment(
        @Path("postId") postId: String,
        @Body request: Map<String, String>, // Mapeia para { content, parentCommentId }
    ): Response<SingleCommentResponse>


    // --- UPLOAD & PAGAMENTO ---
    @Multipart
    @POST("upload")
    suspend fun uploadImage(@Part image: MultipartBody.Part): Response<UploadResponse>

    @POST("payments/create-intent")
    suspend fun createPaymentIntent(@Body request: Map<String, Any>): Response<PaymentIntentResponse>

    @GET("posts/comments/{parentCommentId}/replies")
    suspend fun getReplies(
        @Path("parentCommentId") parentCommentId: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int = 5,
    ): Response<RepliesResponse>
}

// --- DTOs (Data Transfer Objects) ---

data class AuthResponse(val status: String, val token: String, val data: UserData)
data class UserWrapper(val status: String, val data: UserData)
data class UserData(val user: User)

data class PublicProfileResponse(val status: String, val data: PublicProfileData)
data class PublicProfileData(val user: User, val stats: UserStats, val isFollowing: Boolean)
data class UserStats(val followers: Int, val following: Int, val posts: Int)
data class FollowResponse(val status: String, val data: FollowData)
data class FollowData(val isFollowing: Boolean)

data class SearchResponse(val status: String, val data: SearchData)
data class SearchData(val users: List<User>, val pets: List<Pet>)

data class NotificationResponse(
    val status: String,
    val results: Int,
    val data: NotificationListData,
)

data class NotificationListData(val notifications: List<Notification>)

data class PetsResponse(val status: String, val results: Int, val data: PetsData)
data class PetsData(val pets: List<Pet>)
data class PetWrapper(val status: String, val data: SinglePetData)
data class SinglePetData(val pet: Pet)
data class VaccineResponse(val status: String, val data: VaccineListData)
data class VaccineListData(val vaccines: List<com.tagmypet.ui.screens.profile.health.VaccineModel>)

data class FeedResponse(val status: String, val results: Int?, val data: FeedData)
data class FeedData(val posts: List<com.tagmypet.ui.components.PostVO>)

data class ConversationsResponse(val status: String, val data: ConversationsData)
data class ConversationsData(val conversations: List<ConversationDTO>)
data class ConversationDTO(val _id: String, val lastMessage: MessageDTO, val userInfo: User)

data class MessagesResponse(val status: String, val results: Int, val data: MessagesData)
data class MessagesData(val messages: List<MessageDTO>)
data class MessageResponse(val status: String, val data: SingleMessageData)
data class SingleMessageData(val message: MessageDTO)
data class MessageDTO(
    val _id: String,
    val text: String,
    val sender: User,
    val receiver: User,
    val createdAt: String,
)

data class UploadResponse(val status: String, val data: UploadData)
data class UploadData(val url: String)

data class PaymentIntentResponse(val clientSecret: String)

// --- DTOs de Comentário (NOVOS) ---
data class SingleCommentResponse(val status: String, val data: SingleCommentData)
data class SingleCommentData(val comment: ApiCommentDTO)

data class CommentsResponse(val status: String, val results: Int, val data: CommentsData)
data class CommentsData(val comments: List<ApiCommentDTO>)

data class ApiCommentDTO(
    @Json(name = "_id") val _id: String,
    val author: User,
    val content: String,
    val createdAt: String,
    val parentComment: String?,
    val replies: List<ApiCommentDTO>? = emptyList(),
)

data class RepliesResponse(
    val status: String,
    val results: Int,
    val page: Int,
    val total: Int,
    val data: RepliesData,
)

data class RepliesData(val replies: List<ApiCommentDTO>)