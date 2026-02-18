package by.dreb.tutorhelper.presentation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import android.app.Activity
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.presentation.finance.FinanceScreen
import androidx.hilt.navigation.compose.hiltViewModel
import by.dreb.tutorhelper.presentation.monetization.MonetizationViewModel
import by.dreb.tutorhelper.presentation.monetization.PaywallScreen
import by.dreb.tutorhelper.presentation.schedule.LessonCreateScreen
import by.dreb.tutorhelper.presentation.schedule.LessonDetailsScreen
import by.dreb.tutorhelper.presentation.schedule.LessonEditScreen
import by.dreb.tutorhelper.presentation.schedule.ScheduleScreen
import by.dreb.tutorhelper.presentation.students.StudentCreateScreen
import by.dreb.tutorhelper.presentation.students.StudentDetailsScreen
import by.dreb.tutorhelper.presentation.students.StudentEditScreen
import by.dreb.tutorhelper.presentation.students.StudentsScreen
import by.dreb.tutorhelper.presentation.summary.PrivacyPolicyScreen
import by.dreb.tutorhelper.presentation.summary.SummaryScreen
import by.dreb.tutorhelper.ui.theme.TutorHelperTheme
import java.time.LocalDate

data class BottomDestination(
    val route: String,
    val labelRes: Int,
    val icon: @Composable () -> Unit
)


private tailrec fun ContextWrapper?.findActivity(): Activity? = when (this) {
    is Activity -> this
    null -> null
    else -> baseContext.let { if (it is ContextWrapper) it else null }.findActivity()
}

@Composable
fun AppRoot() {
    val monetizationViewModel: MonetizationViewModel = hiltViewModel()
    val monetizationState = monetizationViewModel.uiState
        .collectAsStateWithLifecycle().value

    val activity = (androidx.compose.ui.platform.LocalContext.current as? ContextWrapper).findActivity()

    LaunchedEffect(Unit) {
        monetizationViewModel.refreshStatus()
    }

    TutorHelperTheme {
        if (monetizationState.shouldShowPaywall) {
            PaywallScreen(
                uiState = monetizationState,
                onSubscribeClick = { selectedActivity -> monetizationViewModel.launchPurchase(selectedActivity) },
                onRestoreClick = monetizationViewModel::refreshStatus,
                activity = activity
            )
            return@TutorHelperTheme
        }

        val navController = rememberNavController()
        val destinations = remember {
            listOf(
                BottomDestination(
                    route = "schedule",
                    labelRes = R.string.tab_schedule,
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) }
                ),
                BottomDestination(
                    route = "students",
                    labelRes = R.string.tab_students,
                    icon = { Icon(Icons.Default.Groups, contentDescription = null) }
                ),
                BottomDestination(
                    route = "finance",
                    labelRes = R.string.tab_finance,
                    icon = { Icon(Icons.Default.Payments, contentDescription = null) }
                ),
                BottomDestination(
                    route = "summary",
                    labelRes = R.string.tab_summary,
                    icon = { Icon(Icons.Default.Insights, contentDescription = null) }
                )
            )
        }

        val backStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry.value?.destination?.route
        val showBottomBar = destinations.any { it.route == currentRoute }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (showBottomBar) {
                    Surface(
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                        shadowElevation = 16.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            tonalElevation = 0.dp,
                            modifier = Modifier
                                .height(100.dp)
                                .padding(top = 12.dp)
                        ) {
                            destinations.forEach { destination ->
                                val isSelected = currentRoute == destination.route
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = {
                                        if (!isSelected) {
                                            navController.navigate(destination.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    },

                                    icon = { destination.icon() },
                                    label = {
                                        Text(
                                            text = stringResource(destination.labelRes),
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                    },
                                    alwaysShowLabel = true
                                )
                            }
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "schedule",
                modifier = Modifier.padding(padding),
                enterTransition = { fadeIn(animationSpec = tween(300)) + slideInHorizontally { it / 10 } },
                exitTransition = { fadeOut(animationSpec = tween(300)) },
                popEnterTransition = { fadeIn(animationSpec = tween(300)) },
                popExitTransition = { fadeOut(animationSpec = tween(300)) + slideOutHorizontally { it / 10 } }
            ) {
                composable("schedule") {
                    ScheduleScreen(
                        onLessonClick = { id -> navController.navigate("lesson_details/$id") },
                        onAddLessonClick = { selectedDate -> navController.navigate("lesson_create?date=$selectedDate") }
                    )
                }
                composable(
                    route = "lesson_details/{lessonId}",
                    arguments = listOf(navArgument("lessonId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val lessonId =
                        backStackEntry.arguments?.getLong("lessonId") ?: return@composable
                    LessonDetailsScreen(
                        lessonId = lessonId,
                        onBackClick = { navController.popBackStack() },
                        onEditClick = { id -> navController.navigate("lesson_edit/$id") }
                    )
                }
                composable(
                    route = "lesson_edit/{lessonId}",
                    arguments = listOf(navArgument("lessonId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val lessonId =
                        backStackEntry.arguments?.getLong("lessonId") ?: return@composable
                    LessonEditScreen(
                        lessonId = lessonId,
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable(
                    route = "lesson_create?date={date}",
                    arguments = listOf(
                        navArgument("date") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val selectedDateArg = backStackEntry.arguments?.getString("date")
                    val initialDate = selectedDateArg?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                    LessonCreateScreen(
                        initialDate = initialDate,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable("students") {
                    StudentsScreen(
                        onStudentClick = { id -> navController.navigate("student_details/$id") },
                        onAddStudentClick = { navController.navigate("student_create") }
                    )
                }
                composable(
                    route = "student_details/{studentId}",
                    arguments = listOf(navArgument("studentId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val studentId =
                        backStackEntry.arguments?.getLong("studentId") ?: return@composable
                    StudentDetailsScreen(
                        studentId = studentId,
                        onBackClick = { navController.popBackStack() },
                        onEditClick = { id -> navController.navigate("student_edit/$id") }
                    )
                }
                composable(
                    route = "student_edit/{studentId}",
                    arguments = listOf(navArgument("studentId") { type = NavType.LongType })
                ) {
                    StudentEditScreen(onBackClick = { navController.popBackStack() })
                }
                composable("student_create") {
                    StudentCreateScreen(onBackClick = { navController.popBackStack() })
                }

                composable("finance") { FinanceScreen() }
                composable("summary") {
                    SummaryScreen(onOpenPrivacyPolicy = { navController.navigate("privacy_policy") })
                }
                composable("privacy_policy") {
                    PrivacyPolicyScreen(onBackClick = { navController.popBackStack() })
                }
            }
        }
    }
}
