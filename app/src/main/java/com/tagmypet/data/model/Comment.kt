import com.tagmypet.ui.screens.home.Comment

data class Comment(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String,
    val text: String,
    val timeAgo: String,
    val replies: List<Comment> = emptyList(),
    val parentCommentId: String? = null, // CORREÇÃO: Campo adicionado
)