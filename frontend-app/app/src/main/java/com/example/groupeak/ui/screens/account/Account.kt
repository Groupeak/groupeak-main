package com.example.groupeak.ui.screens.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.groupeak.R
import com.example.groupeak.ui.components.GroupeakBackground
import com.example.groupeak.ui.theme.BluePrimary300
import com.example.groupeak.ui.theme.GroupeakDarkBlue
import com.example.groupeak.ui.theme.GroupeakGray
import com.example.groupeak.ui.theme.GroupeakLightGray
import com.example.groupeak.ui.theme.OnestFontFamily
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.material3.HorizontalDivider

@Composable
fun AccountScreen(
    uiState: AccountUiState,
    onProfileClick: () -> Unit,
    onContactClick: () -> Unit,
    onChangeEmailClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onNotificationsClick: () -> Unit
) {
    GroupeakBackground {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Шапка: надпись по центру экрана, кнопки на фиксированных отступах
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 55.dp, bottom = 58.dp)
            ) {

                // Заголовок
                Text(
                    text = "Аккаунт",
                    fontSize = 32.sp,
                    fontFamily = OnestFontFamily,
                    fontWeight = FontWeight.Normal,
                    color = BluePrimary300,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxWidth()
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 19.dp)
                        .size(28.dp) // область нажатия
                        .clickable { onNotificationsClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_notif),
                        contentDescription = "Уведомления",
                        tint = BluePrimary300,
                        modifier = Modifier.size(22.dp) // размер иконки
                    )
                }
            }

            // Информация о пользователе
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 49.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Аватар
                Image(
                    painter = painterResource(id = R.drawable.im_avatar),
                    contentDescription = "Аватар",
                    modifier = Modifier.size(82.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = uiState.userName, // ← из состояния
                        fontSize = 24.sp,
                        fontFamily = OnestFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = GroupeakDarkBlue
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_email),
                            contentDescription = null,
                            tint = GroupeakDarkBlue,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = uiState.email, // ← из состояния
                            fontSize = 16.sp,
                            fontFamily = OnestFontFamily,
                            fontWeight = FontWeight.Normal,
                            color = GroupeakDarkBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(47.dp))

            // Основной контент в белом блоке
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 32.dp, end = 32.dp)
                    .background(Color.White, shape = RoundedCornerShape(18.dp))
            ) {
                // Статистика
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 34.dp, start = 17.dp, end = 17.dp),
                ) {
                    StatCard(
                        count = uiState.tasksCompleted, // ← из состояния
                        label = "Задач выполнено",
                        countColor = Color.Green,
                        modifier = Modifier
                            .width(130.dp)
                            .height(50.dp)
                    )

                    Spacer(modifier = Modifier.width(46.dp))

                    StatCard(
                        count = uiState.tasksNotCompleted, // ← из состояния
                        label = "Задач не выполнено",
                        countColor = Color.Red,
                        modifier = Modifier
                            .width(130.dp)
                            .height(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                MenuSection(
                    title = "Мой профиль",
                    onClick = onProfileClick,
                    showArrow = true,
                    modifier = Modifier.padding(vertical = 25.dp)
                )

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 29.dp, end = 35.dp),
                    color = GroupeakLightGray
                )

                MenuSection(
                    title = "Обратная связь",
                    onClick = onContactClick,
                    showArrow = true,
                    modifier = Modifier.padding(vertical = 25.dp)
                )

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 29.dp, end = 35.dp),
                    color = GroupeakLightGray
                )

                MenuSection(
                    title = "Изменить почту",
                    onClick = onChangeEmailClick,
                    showArrow = true,
                    modifier = Modifier.padding(vertical = 25.dp)
                )

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 29.dp, end = 35.dp),
                    color = GroupeakLightGray
                )

                MenuSection(
                    title = "Изменить пароль",
                    onClick = onChangePasswordClick,
                    showArrow = true,
                    modifier = Modifier.padding(vertical = 25.dp)
                )

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 29.dp, end = 35.dp),
                    color = GroupeakLightGray
                )

                MenuSection(
                    title = "Выйти из аккаунта",
                    onClick = onLogoutClick,
                    isLogout = true,
                    showArrow = true,
                    modifier = Modifier.padding(top = 16.dp, bottom = 23.dp)
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    count: String,
    label: String,
    countColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.White, shape = RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                color = GroupeakLightGray,
                shape = RoundedCornerShape(18.dp)
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = count,
            fontSize = 14.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            color = countColor,
            modifier = Modifier.offset(y = 3.dp)
        )
        Text(
            text = label,
            fontSize = 12.sp,
            fontFamily = OnestFontFamily,
            fontWeight = FontWeight.Normal,
            color = GroupeakDarkBlue,
            modifier = Modifier.offset(y = -3.dp)
        )
    }
}
@Composable
private fun MenuSection(
    title: String,
    onClick: () -> Unit,
    isLogout: Boolean = false,
    showArrow: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable { onClick() }
            .fillMaxWidth()
            .padding(
                start = 29.dp,
                end = 37.dp,
                top = 0.dp,
                bottom = 0.dp
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontFamily = OnestFontFamily,
                fontWeight = FontWeight.Normal,
                color = if (isLogout) Color.Red else GroupeakDarkBlue
            )

            if (showArrow) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_right_arrow),
                    contentDescription = "Перейти",
                    tint = BluePrimary300,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun BottomNavItem(label: String, icon: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = OnestFontFamily,
            color = GroupeakGray
        )
    }
}