package by.dreb.tutorhelper.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import by.dreb.tutorhelper.R
import by.dreb.tutorhelper.domain.model.ThemeMode
import androidx.navigation.NavType
import androidx.navigation.navArgument
import by.dreb.tutorhelper.presentation.finance.FinanceScreen
import by.dreb.tutorhelper.presentation.schedule.LessonCreateScreen
import by.dreb.tutorhelper.presentation.schedule.LessonDetailsScreen
import by.dreb.tutorhelper.presentation.schedule.LessonEditScreen
import by.dreb.tutorhelper.presentation.schedule.ScheduleScreen
import by.dreb.tutorhelper.presentation.settings.SettingsUiState
import by.dreb.tutorhelper.presentation.students.StudentsScreen
import by.dreb.tutorhelper.presentation.summary.SummaryScreen
import by.dreb.tutorhelper.ui.theme.TutorHelperTheme

data class BottomDestination(
    val route: String,
    val labelRes: Int,
    val icon: @Composable () -> Unit
)

@Composable
fun AppRoot(
    settingsState: SettingsUiState,
    onThemeSelected: (ThemeMode) -> Unit
) {
    val darkTheme = settingsState.themeMode == ThemeMode.DARK

    TutorHelperTheme(darkTheme = darkTheme) {
        val navController = rememberNavController()
        val destinations = listOf(
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

        val backStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry.value?.destination?.route

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar {
                    destinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = destination.icon,
                            label = { Text(stringResource(destination.labelRes)) }
                        )
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "schedule",
                modifier = Modifier.padding(padding)
            ) {
                composable("schedule") {
                    ScheduleScreen(
                        onLessonClick = { lessonId ->
                            navController.navigate("lesson_details/$lessonId")
                        },
                        onEditClick = { lessonId ->
                            navController.navigate("lesson_edit/$lessonId")
                        },
                        onAddLessonClick = {
                            navController.navigate("lesson_create")
                        }
                    )
                }
                composable(
                    route = "lesson_details/{lessonId}",
                    arguments = listOf(navArgument("lessonId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val lessonId = backStackEntry.arguments?.getLong("lessonId") ?: return@composable
                    LessonDetailsScreen(
                        lessonId = lessonId,
                        onBackClick = { navController.popBackStack() },
                        onEditClick = { id ->
                            navController.navigate("lesson_edit/$id")
                        }
                    )
                }
                composable(
                    route = "lesson_edit/{lessonId}",
                    arguments = listOf(navArgument("lessonId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val lessonId = backStackEntry.arguments?.getLong("lessonId") ?: return@composable
                    LessonEditScreen(
                        lessonId = lessonId,
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable("lesson_create") {
                    LessonCreateScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable("students") { StudentsScreen() }
                composable("finance") { FinanceScreen() }
                composable("summary") {
                    SummaryScreen(
                        settingsState = settingsState,
                        onThemeSelected = onThemeSelected
                    )
                }
            }
        }
    }
}
