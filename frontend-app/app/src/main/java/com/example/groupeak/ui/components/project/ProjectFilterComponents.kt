package com.example.groupeak.ui.components.project

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.groupeak.R
import com.example.groupeak.ui.screens.project.ProjectMemberItemUi
import com.example.groupeak.ui.screens.project.ProjectTaskPriorityUi

private val FilterBlue = Color(0xFF3988FE)
private val FilterBlueLight = Color(0xFF71AAFF)
private val FilterBlueText = Color(0xFF1673FF)
private val FilterBorder = Color(0xFFB0B3C7)
private val FilterOverlay = Color(0x4D000000)
private val FilterDarkText = Color(0xFF333B46)
private val FilterSecondaryText = Color(0xFF70717A)
private val FilterPlaceholder = Color(0xFF7A7A7A)

private val HighPriorityRed = Color(0xFFFF1E1E)
private val MediumPriorityYellow = Color(0xFFD6C100)
private val LowPriorityGreen = Color(0xFF28C52E)

private val MainDialogWidth = 348.dp
private val MainDialogHeight = 245.dp
private val MembersDialogWidth = 348.dp
private val MembersDialogHeight = 470.dp

private enum class FilterStep {
    Main,
    Members
}

fun hasAppliedProjectFilter(
    selectedMemberIds: Set<Long>,
    selectedPriority: ProjectTaskPriorityUi?,
): Boolean {
    return selectedMemberIds.isNotEmpty() || selectedPriority != null
}

@Composable
fun ProjectFilterButton(
    isFilterApplied: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(if (isFilterApplied) FilterBlueLight else Color.White)
            .border(
                width = 1.dp,
                color = if (isFilterApplied) FilterBlueLight else FilterBorder,
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_filter),
            contentDescription = "Фильтр",
            tint = if (isFilterApplied) Color.White else FilterBlue,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
fun ProjectFilterDialogHost(
    visible: Boolean,
    members: List<ProjectMemberItemUi>,
    initialSelectedMemberIds: Set<Long> = emptySet(),
    initialPriority: ProjectTaskPriorityUi? = null,
    onDismiss: () -> Unit,
    onApply: (Set<Long>, ProjectTaskPriorityUi?) -> Unit,
) {
    if (!visible) return

    var step by remember(visible) { mutableStateOf(FilterStep.Main) }
    var selectedMemberIds by remember(visible) { mutableStateOf(initialSelectedMemberIds) }
    var selectedPriority by remember(visible) { mutableStateOf(initialPriority) }

    LaunchedEffect(visible, initialSelectedMemberIds, initialPriority) {
        if (visible) {
            step = FilterStep.Main
            selectedMemberIds = initialSelectedMemberIds
            selectedPriority = initialPriority
        }
    }

    val noRipple = remember { MutableInteractionSource() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FilterOverlay)
                .clickable(
                    indication = null,
                    interactionSource = noRipple,
                    onClick = onDismiss
                )
        ) {
            when (step) {
                FilterStep.Main -> {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(MainDialogWidth)
                            .height(MainDialogHeight)
                            .clickable(
                                indication = null,
                                interactionSource = noRipple,
                                onClick = {}
                            ),
                        color = Color.White,
                        shape = RoundedCornerShape(18.dp),
                        shadowElevation = 0.dp,
                    ) {
                        ProjectMainFilterContent(
                            membersText = formatSelectedMembersText(
                                members = members,
                                selectedIds = selectedMemberIds,
                            ),
                            selectedPriority = selectedPriority,
                            onMembersClick = { step = FilterStep.Members },
                            onPrioritySelected = { priority ->
                                selectedPriority =
                                    if (selectedPriority == priority) null else priority
                            },
                            onResetClick = {
                                selectedMemberIds = emptySet()
                                selectedPriority = null
                            },
                            onApplyClick = {
                                onApply(selectedMemberIds, selectedPriority)
                                onDismiss()
                            }
                        )
                    }
                }

                FilterStep.Members -> {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(MembersDialogWidth)
                            .height(MembersDialogHeight)
                            .clickable(
                                indication = null,
                                interactionSource = noRipple,
                                onClick = {}
                            ),
                        color = Color.White,
                        shape = RoundedCornerShape(18.dp),
                        shadowElevation = 0.dp,
                    ) {
                        ProjectMembersPickerContent(
                            members = members,
                            selectedIds = selectedMemberIds,
                            onBackClick = { step = FilterStep.Main },
                            onMemberToggle = { memberId ->
                                selectedMemberIds = if (memberId in selectedMemberIds) {
                                    selectedMemberIds - memberId
                                } else {
                                    selectedMemberIds + memberId
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectMainFilterContent(
    membersText: String,
    selectedPriority: ProjectTaskPriorityUi?,
    onMembersClick: () -> Unit,
    onPrioritySelected: (ProjectTaskPriorityUi) -> Unit,
    onResetClick: () -> Unit,
    onApplyClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 10.dp, end = 10.dp, top = 17.dp, bottom = 13.dp)
    ) {
        Text(
            text = "Участники",
            color = FilterBlue,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(start = 11.dp)
        )

        MembersSelectorField(
            text = membersText,
            isPlaceholder = membersText == "Выберите участников для фильтра",
            onClick = onMembersClick,
            modifier = Modifier.padding(top = 6.dp)
        )

        Text(
            text = "Приоритет",
            color = FilterBlue,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(start = 11.dp, top = 11.dp)
        )

        PrioritySegment(
            selectedPriority = selectedPriority,
            onPrioritySelected = onPrioritySelected,
            modifier = Modifier.padding(top = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 23.dp, start = 9.dp, end = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            FilterActionButton(
                text = "Применить",
                filled = true,
                onClick = onApplyClick
            )

            FilterActionButton(
                text = "Сбросить",
                filled = false,
                onClick = onResetClick
            )
        }
    }
}

@Composable
private fun MembersSelectorField(
    text: String,
    isPlaceholder: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val noRipple = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(55.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                color = FilterBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .background(Color.White)
            .clickable(
                indication = null,
                interactionSource = noRipple,
                onClick = onClick
            )
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_group),
            contentDescription = null,
            tint = FilterBlue,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 11.dp)
                .size(24.dp)
        )

        Text(
            text = text,
            color = if (isPlaceholder) FilterPlaceholder else FilterDarkText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 43.dp, end = 44.dp)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = FilterBlue,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 11.dp)
                .size(24.dp)
        )
    }
}

@Composable
private fun PrioritySegment(
    selectedPriority: ProjectTaskPriorityUi?,
    onPrioritySelected: (ProjectTaskPriorityUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    var lastVisualPriority by remember { mutableStateOf(selectedPriority) }
    var previousSelectedPriority by remember { mutableStateOf(selectedPriority) }

    val displayPriority = selectedPriority ?: lastVisualPriority
    val selectingFromEmpty = previousSelectedPriority == null && selectedPriority != null

    LaunchedEffect(selectedPriority) {
        if (selectedPriority != null) {
            lastVisualPriority = selectedPriority
        }
        previousSelectedPriority = selectedPriority
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(37.dp)
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                color = FilterBorder,
                shape = RoundedCornerShape(18.dp)
            )
            .background(Color.White)
    ) {
        val segmentWidth = maxWidth / 3

        val displayIndex = when (displayPriority) {
            ProjectTaskPriorityUi.LOW -> 0
            ProjectTaskPriorityUi.MEDIUM -> 1
            ProjectTaskPriorityUi.HIGH -> 2
            null -> 0
        }

        val animatedOffset by animateDpAsState(
            targetValue = segmentWidth * displayIndex,
            animationSpec = if (selectingFromEmpty) {
                tween(durationMillis = 0)
            } else {
                tween(durationMillis = 240, easing = FastOutSlowInEasing)
            },
            label = "project_priority_offset"
        )

        val animatedAlpha by animateFloatAsState(
            targetValue = if (selectedPriority == null) 0f else 1f,
            animationSpec = tween(durationMillis = 170, easing = FastOutSlowInEasing),
            label = "project_priority_alpha"
        )

        val animatedColor by animateColorAsState(
            targetValue = when (displayPriority) {
                ProjectTaskPriorityUi.LOW -> LowPriorityGreen
                ProjectTaskPriorityUi.MEDIUM -> MediumPriorityYellow
                ProjectTaskPriorityUi.HIGH -> HighPriorityRed
                null -> Color.Transparent
            },
            animationSpec = if (selectingFromEmpty) {
                tween(durationMillis = 0)
            } else {
                tween(durationMillis = 220, easing = FastOutSlowInEasing)
            },
            label = "project_priority_color"
        )

        Box(
            modifier = Modifier
                .offset(x = animatedOffset)
                .width(segmentWidth)
                .fillMaxHeight()
                .padding(1.dp)
                .graphicsLayer { alpha = animatedAlpha }
                .background(
                    color = animatedColor,
                    shape = RoundedCornerShape(18.dp)
                )
        )

        Row(modifier = Modifier.fillMaxSize()) {
            PriorityTab(
                text = "Низкий",
                selected = selectedPriority == ProjectTaskPriorityUi.LOW,
                onClick = { onPrioritySelected(ProjectTaskPriorityUi.LOW) },
                modifier = Modifier.weight(1f)
            )

            PriorityTab(
                text = "Средний",
                selected = selectedPriority == ProjectTaskPriorityUi.MEDIUM,
                onClick = { onPrioritySelected(ProjectTaskPriorityUi.MEDIUM) },
                modifier = Modifier.weight(1f)
            )

            PriorityTab(
                text = "Высокий",
                selected = selectedPriority == ProjectTaskPriorityUi.HIGH,
                onClick = { onPrioritySelected(ProjectTaskPriorityUi.HIGH) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PriorityTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val noRipple = remember { MutableInteractionSource() }

    val animatedTextColor by animateColorAsState(
        targetValue = if (selected) Color.White else FilterDarkText,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
        label = "project_priority_text_color"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                indication = null,
                interactionSource = noRipple,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = animatedTextColor,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun FilterActionButton(
    text: String,
    filled: Boolean,
    onClick: () -> Unit,
) {
    val noRipple = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .width(140.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (filled) FilterBlue else Color.White)
            .border(
                width = if (filled) 0.dp else 1.dp,
                color = if (filled) Color.Transparent else FilterBlue,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(
                indication = null,
                interactionSource = noRipple,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (filled) Color.White else FilterBlueText,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ProjectMembersPickerContent(
    members: List<ProjectMemberItemUi>,
    selectedIds: Set<Long>,
    onBackClick: () -> Unit,
    onMemberToggle: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp, bottom = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .padding(horizontal = 12.dp)
        ) {
            val noRipple = remember { MutableInteractionSource() }

            Icon(
                painter = painterResource(id = R.drawable.ic_back_arrow),
                contentDescription = "Назад",
                tint = FilterBlue,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(28.dp)
                    .clickable(
                        indication = null,
                        interactionSource = noRipple,
                        onClick = onBackClick
                    )
            )

            Text(
                text = "Выбор участников",
                color = FilterBlueText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, top = 14.dp)
        ) {
            itemsIndexed(members) { _, member ->
                MemberRow(
                    member = member,
                    checked = member.id in selectedIds,
                    onToggle = { onMemberToggle(member.id) }
                )

                HorizontalDivider(
                    color = FilterBorder,
                    thickness = 1.dp,
                    modifier = Modifier.padding(start = 14.dp, end = 14.dp)
                )
            }
        }
    }
}

@Composable
private fun MemberRow(
    member: ProjectMemberItemUi,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    val noRipple = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(71.dp)
            .clickable(
                indication = null,
                interactionSource = noRipple,
                onClick = onToggle
            )
            .padding(start = 14.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.width(118.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = member.fullName,
                color = FilterDarkText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start
            )
        }

        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = member.position,
                color = FilterSecondaryText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier.width(32.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            BlueCheckBox(checked = checked)
        }
    }
}

@Composable
private fun BlueCheckBox(
    checked: Boolean,
) {
    Box(
        modifier = Modifier.size(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (checked) FilterBlue else Color.White)
                .border(
                    width = 2.dp,
                    color = FilterBlue,
                    shape = RoundedCornerShape(2.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

private fun formatSelectedMembersText(
    members: List<ProjectMemberItemUi>,
    selectedIds: Set<Long>,
): String {
    val selectedMembers = members.filter { it.id in selectedIds }

    return when (selectedMembers.size) {
        0 -> "Выберите участников для фильтра"
        1 -> selectedMembers[0].fullName
        2 -> "${selectedMembers[0].fullName}, ${selectedMembers[1].fullName}"
        else -> "${selectedMembers[0].fullName}, ${selectedMembers[1].fullName} и еще ${selectedMembers.size - 2}"
    }
}