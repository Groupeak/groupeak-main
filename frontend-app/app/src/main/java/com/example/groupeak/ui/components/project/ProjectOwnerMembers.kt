package com.example.groupeak.ui.components.project

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.groupeak.R
import com.example.groupeak.ui.screens.project_for_owner.ProjectForOwnerUiState
import com.example.groupeak.ui.screens.project_for_owner.ProjectMemberItemUi
import com.example.groupeak.ui.theme.OnestFontFamily

private val HeaderBlue = Color(0xFF1876FF)
private val AccentBlue = Color(0xFF3988FE)
private val BorderGray = Color(0xFFB0B3C7)
private val DarkText = Color(0xFF333B46)
private val EmptyStateGray = Color(0xFF70717A)
private val DeleteRed = Color(0xFFFF0000)
private val GrayPlaceholder = Color(0xFF7A7A7A)

enum class InviteDialogStep {
    FORM,
    ROLE_SELECTION
}

@Composable
fun InviteMemberDialog(
    onDismiss: () -> Unit,
    onSendInvite: (email: String, role: String) -> Unit
) {
    var currentStep by remember { mutableStateOf(InviteDialogStep.FORM) }
    var emailInput by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf<String?>(null) }

    val rolesList = listOf("Frontend", "Backend", "DevOps", "Designer", "QA")
    val roleScrollState = rememberScrollState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .width(348.dp)
                .height(299.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            if (currentStep == InviteDialogStep.FORM) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Приглашение",
                        color = AccentBlue,
                        fontFamily = OnestFontFamily,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(11.dp))
                    InviteInputField(
                        label = "Почта",
                        iconId = R.drawable.ic_mail,
                        placeholder = "Введите почту участника",
                        value = emailInput,
                        onValueChange = { emailInput = it }
                    )
                    Spacer(modifier = Modifier.height(11.dp))
                    InviteInputField(
                        label = "Должность",
                        iconId = R.drawable.ic_briefcase,
                        placeholder = "Выберите должность участника",
                        value = selectedRole.orEmpty(),
                        isClickable = true,
                        onClick = { currentStep = InviteDialogStep.ROLE_SELECTION },
                        hasTrailingIcon = true
                    )
                    Spacer(modifier = Modifier.height(21.dp))
                    Button(
                        onClick = { onSendInvite(emailInput, selectedRole.orEmpty()) },
                        modifier = Modifier.width(261.dp).height(32.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Отправить приглашение",
                            color = Color.White,
                            fontFamily = OnestFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back_arrow),
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier
                            .size(28.dp)
                            .offset(x = 8.dp, y = 15.dp)
                            .clickable { currentStep = InviteDialogStep.FORM }
                    )

                    Text(
                        text = "Должность",
                        color = AccentBlue,
                        fontFamily = OnestFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 70.dp)
                            .width(231.dp)
                            .height(180.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(roleScrollState)
                        ) {
                            rolesList.forEach { role ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedRole = role
                                            currentStep = InviteDialogStep.FORM
                                        }
                                        .padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = role,
                                        color = DarkText,
                                        fontFamily = OnestFontFamily,
                                        fontSize = 20.sp,
                                        lineHeight = 26.sp,
                                        fontWeight = FontWeight.Normal,
                                        maxLines = 1
                                    )
                                }

                                HorizontalDivider(
                                    modifier = Modifier.fillMaxWidth(),
                                    thickness = 1.dp,
                                    color = BorderGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InviteInputField(
    label: String,
    iconId: Int,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit = {},
    isClickable: Boolean = false,
    onClick: () -> Unit = {},
    hasTrailingIcon: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    ) {
        Text(
            text = label,
            color = AccentBlue,
            fontFamily = OnestFontFamily,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 11.dp, bottom = 6.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp)
                .border(1.dp, BorderGray, RoundedCornerShape(18.dp))
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White)
                .clickable(enabled = isClickable) { onClick() }
                .padding(horizontal = 11.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = iconId),
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                if (isClickable) {
                    Text(
                        text = if (value.isBlank()) placeholder else value,
                        color = if (value.isBlank()) GrayPlaceholder else DarkText,
                        fontFamily = OnestFontFamily,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = TextStyle(
                            color = DarkText,
                            fontSize = 14.sp,
                            fontFamily = OnestFontFamily
                        ),
                        decorationBox = { innerTextField ->
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    color = GrayPlaceholder,
                                    fontFamily = OnestFontFamily,
                                    fontSize = 14.sp
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                if (hasTrailingIcon) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_right_arrow),
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MembersBodySimple(
    uiState: ProjectForOwnerUiState,
    onMemberClick: (Long) -> Unit,
    onInviteMemberClick: () -> Unit,
    onDeleteMember: (Long) -> Unit,
    onSendInvite: (email: String, role: String) -> Unit = { _, _ -> },
) {

    val scrollState = rememberScrollState()
    val members = uiState.members

    var showInviteDialog by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = 69.dp
            )
            .verticalScroll(scrollState),
    ) {

        MembersHeaderExact()

        Spacer(modifier = Modifier.height(34.dp))

        members.forEachIndexed { index, member ->

            MemberRowExact(
                member = member,
                onClick = {
                    onMemberClick(member.id)
                },
                onDeleteClick = {
                    onDeleteMember(member.id)
                },
                showDeleteButton = !member.isOwner,
            )

            Spacer(modifier = Modifier.height(22.dp))

            HorizontalDivider(
                modifier = Modifier
                    .padding(start = 51.dp)
                    .width(289.dp),
                thickness = 1.dp,
                color = BorderGray,
            )

            if (index != members.lastIndex) {
                Spacer(modifier = Modifier.height(22.dp))
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier
                .padding(start = 69.dp)
                .clickable {
                    showInviteDialog = true
                },
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Icon(
                painter = painterResource(
                    id = R.drawable.ic_add_member
                ),
                contentDescription = null,
                tint = AccentBlue,
                modifier = Modifier.size(24.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Пригласить участника",
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                color = AccentBlue,
            )
        }

        Spacer(modifier = Modifier.height(62.dp))
    }

    if (showInviteDialog) {

        InviteMemberDialog(
            onDismiss = {
                showInviteDialog = false
            },
            onSendInvite = { email, role ->
                android.util.Log.e("INVITE_DEBUG", "InviteMemberDialog onSendInvite: email='$email', role='$role'")
                onSendInvite(email, role)
                showInviteDialog = false
            }
        )
    }
}

@Composable
fun MembersHeaderExact() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(25.dp)
    ) {
        Text(
            text = "Участник",
            fontFamily = OnestFontFamily,
            fontSize = 20.sp,
            color = HeaderBlue,
            modifier = Modifier.offset(x = 40.dp)
        )
        Text(
            text = "Должность",
            fontFamily = OnestFontFamily,
            fontSize = 20.sp,
            color = HeaderBlue,
            modifier = Modifier.offset(x = 240.dp)
        )
    }
}

@Composable
fun MemberRowExact(
    member: ProjectMemberItemUi,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    showDeleteButton: Boolean,
) {
    val density = LocalDensity.current
    var textWidthPx by remember(member.fullName) { mutableStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 5.dp)
                .width(170.dp)
        ) {
            Text(
                text = member.fullName,
                fontFamily = OnestFontFamily,
                fontSize = 20.sp,
                color = DarkText,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { res ->
                    textWidthPx = res.size.width
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .widthIn(max = 140.dp)
            )

            if (member.isOwner) {
                val offset = with(density) { (textWidthPx / 2f).toDp() + 18.dp }
                Image(
                    painter = painterResource(id = R.drawable.crown),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(x = offset)
                        .size(24.dp)
                )
            }
        }

        Text(
            text = member.position,
            fontFamily = OnestFontFamily,
            fontSize = 20.sp,
            color = EmptyStateGray,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = 235.dp)
                .width(120.dp)
        )

        if (showDeleteButton) {
            Icon(
                painter = painterResource(id = R.drawable.ic_delete),
                contentDescription = null,
                tint = DeleteRed,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = (-10).dp)
                    .size(26.dp)
                    .clickable { onDeleteClick() }
            )
        }
    }
}