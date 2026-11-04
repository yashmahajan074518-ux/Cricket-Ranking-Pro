package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ui.screens.*
import com.example.ui.theme.CricketRankingProTheme
import com.example.viewmodel.ActiveModal
import com.example.viewmodel.CricketViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: CricketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CricketRankingProTheme {
                val db by viewModel.dbFlow.collectAsState()
                val showIntro by viewModel.showIntro.collectAsState()
                val activeModal by viewModel.activeModal.collectAsState()
                val scope = rememberCoroutineScope()

                // Toast event listener
                LaunchedEffect(Unit) {
                    viewModel.toastEvent.collectLatest { msg ->
                        Toast.makeText(this@MainActivity, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                val activeGroup = db.groups.find { it.id == db.settings.activeGroupId } ?: db.groups.firstOrNull()

                Box(modifier = Modifier.fillMaxSize()) {
                    AnimatedContent(
                        targetState = showIntro,
                        transitionSpec = {
                            fadeIn(tween(700)) togetherWith fadeOut(tween(500))
                        },
                        label = "intro_transition"
                    ) { isIntro ->
                        if (isIntro) {
                            IntroScreen(
                                onDismiss = { viewModel.dismissIntro() },
                                onPlaySound = { viewModel.playStartupSound() }
                            )
                        } else {
                            if (activeGroup == null) {
                                FirstRunScreen(
                                    onCreateGroup = { name, season ->
                                        scope.launch {
                                            viewModel.repository.createGroup(name, season)
                                            viewModel.emitToast("Group '$name' created!")
                                        }
                                    },
                                    onRestoreBackup = {
                                        viewModel.openModal(ActiveModal.BackupRestore)
                                    }
                                )
                            } else {
                                DashboardScreen(viewModel = viewModel)
                            }
                        }
                    }

                    // Global Modal Host
                    if (!showIntro && activeGroup != null) {
                        when (val modal = activeModal) {
                            is ActiveModal.PlayerForm -> {
                                PlayerFormModal(
                                    player = modal.player,
                                    group = activeGroup,
                                    viewModel = viewModel
                                )
                            }
                            is ActiveModal.PlayerProfile -> {
                                PlayerProfileModal(
                                    playerId = modal.playerId,
                                    group = activeGroup,
                                    viewModel = viewModel
                                )
                            }
                            is ActiveModal.MatchDetail -> {
                                MatchDetailModal(
                                    matchId = modal.matchId,
                                    group = activeGroup,
                                    viewModel = viewModel
                                )
                            }
                            is ActiveModal.MatchWizard -> {
                                MatchWizardModal(
                                    viewModel = viewModel,
                                    group = activeGroup
                                )
                            }
                            is ActiveModal.GroupManage -> {
                                GroupManageModal(viewModel = viewModel)
                            }
                            is ActiveModal.AdminPin -> {
                                AdminModal(viewModel = viewModel)
                            }
                            is ActiveModal.BackupRestore -> {
                                BackupRestoreModal(viewModel = viewModel)
                            }
                            is ActiveModal.ShareDialog -> {
                                ShareModal(group = activeGroup, viewModel = viewModel)
                            }
                            is ActiveModal.RatingInfo -> {
                                RatingInfoModal(onDismiss = { viewModel.closeModal() })
                            }
                            ActiveModal.None -> {
                                // No modal
                            }
                        }
                    } else if (!showIntro && activeGroup == null && activeModal is ActiveModal.BackupRestore) {
                        BackupRestoreModal(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
