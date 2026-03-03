package cat.copernic.aguamap1.presentation.fountain.detailFountain.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cat.copernic.aguamap1.R
import cat.copernic.aguamap1.domain.model.Comment
import cat.copernic.aguamap1.ui.theme.Blue10
import cat.copernic.aguamap1.ui.theme.Negro
import cat.copernic.aguamap1.ui.theme.NegroMuySuave

@Composable
fun FountainCommentsSection(
    comments: List<Comment>,
    currentUserId: String?,
    isAdmin: Boolean,
    onAddClick: () -> Unit,
    onEditComment: (Comment) -> Unit,
    onDeleteComment: (Comment) -> Unit,
    onReportComment: (Comment) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.ratings_title, comments.size),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                modifier = Modifier.weight(1f),
                color = Negro
            )
            TextButton(onClick = onAddClick) {
                Text(
                    text = stringResource(R.string.add_comment),
                    color = Blue10,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (comments.isEmpty()) {
            Text(
                text = stringResource(R.string.first_comment_prompt),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                color = NegroMuySuave,
                fontStyle = FontStyle.Italic
            )
        } else {
            comments.forEach { comment ->
                CommentItem(
                    commentObj = comment,
                    isMyComment = comment.userId == currentUserId,
                    isAdmin = isAdmin,
                    onCensor = { /* Lógica admin si la tienes */ },
                    onDelete = { onDeleteComment(comment) },
                    onEdit = { onEditComment(comment) },
                    onReport = { onReportComment(comment) }
                )
            }
        }
    }
}