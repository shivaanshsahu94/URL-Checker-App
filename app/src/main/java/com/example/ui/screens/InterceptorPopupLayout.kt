package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.ui.theme.LocalDarkTheme
import com.example.ui.theme.SharedEffects
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.util.BrowserLauncher
import com.example.viewmodel.MainViewModel

@Composable
fun InterceptorPopupLayout(viewModel: MainViewModel) {
    val context = LocalContext.current
    val isDark = LocalDarkTheme.current

    BackHandler {
        viewModel.closeInterceptor()
        (context as? android.app.Activity)?.finish()
    }

    LaunchedEffect(Unit) {
        triggerSuccessHaptic(context)
    }

    val original by viewModel.originalUrl.collectAsStateWithLifecycle()
    val resolved by viewModel.resolvedUrl.collectAsStateWithLifecycle()
    val isResolving by viewModel.isResolving.collectAsStateWithLifecycle()

    // Floating Center Screen Container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                viewModel.closeInterceptor()
                (context as? android.app.Activity)?.finish()
            },
        contentAlignment = Alignment.Center
    ) {
        // Broad screen real-time blur backdrop
        val fullBgColor = if (isDark) Color(0x350A0E17) else Color(0x25EDF2F7)
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { renderEffect = SharedEffects.glassBlur }
                .background(fullBgColor)
        )

        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
        ) {
            val (glassCardRef, searchIconRef) = createRefs()

            // The main URL container card: inherits TranslucentGlassCard style perfectly!
            TranslucentGlassCard(
                shape = RoundedCornerShape(36.dp),
                border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(glassCardRef) {
                        top.linkTo(parent.top, margin = 36.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                // Content overlay (Crisp and readable)
                Column(
                    modifier = Modifier
                        .padding(start = 20.dp, end = 20.dp, bottom = 24.dp, top = 56.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Redirect Security Scan",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    if (isResolving) {
                        // Intermediate Tracing Screen loader with GlassCard
                        TranslucentGlassCard(
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                LinearProgressIndicator(
                                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50)),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Tracing Redirect Tracks...",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = original,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.61f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        // Prominent URL display card using TranslucentGlassCard
                        TranslucentGlassCard(
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                        ) {
                            Column {
                                Text(
                                    text = "Original Link:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = original,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Unmasked Destination:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = resolved,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Secondary actions: Copy & Share as GlassyPillButtons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GlassyPillButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("Resolved URL", resolved)
                                    clipboard?.setPrimaryClip(clip)
                                    triggerSuccessHaptic(context)
                                    android.widget.Toast.makeText(context, "URL copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                testTag = "copy_resolved_btn",
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StickCopyIcon(
                                        color = if (isDark) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Copy URL", 
                                        fontSize = 12.sp, 
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            GlassyPillButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, resolved)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share URL Target"))
                                },
                                testTag = "share_resolved_btn",
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    StickShareIcon(
                                        color = if (isDark) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Share URL", 
                                        fontSize = 12.sp, 
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left Pill: Cancel Button
                        GlassyPillButton(
                            onClick = { 
                                viewModel.closeInterceptor()
                                (context as? android.app.Activity)?.finish()
                            },
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant),
                            testTag = "cancel_popup_btn",
                            modifier = Modifier.weight(1f)
                        ) {
                            StickCloseIcon(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Cancel", 
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                             )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Middle: Central circle open browser button (M3 primary colored!)
                        GlassyCircularButton(
                            onClick = {
                                val urlToOpen = resolved.ifEmpty { original }
                                if (urlToOpen.isNotEmpty()) {
                                    BrowserLauncher.openWithChooser(context, urlToOpen)
                                } else {
                                    android.widget.Toast.makeText(context, "URL is null or empty", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                viewModel.closeInterceptor()
                                (context as? android.app.Activity)?.finish()
                            },
                            containerColor = MaterialTheme.colorScheme.primary,
                            border = BorderStroke(0.dp, Color.Transparent),
                            testTag = "open_browser_scalloped_btn"
                        ) {
                            StickBrowserIcon(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Right Pill: Check URL Button (Dynamic URL logic mapped to ViewModel)
                        GlassyPillButton(
                            onClick = {
                                val urlToCheck = resolved.ifEmpty { original }
                                if (urlToCheck.isNotEmpty()) {
                                    run {
                                        // Scope clipboard operations separately
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("Resolved URL", urlToCheck)
                                        clipboard?.setPrimaryClip(clip)
                                    }
                                    val checkerLabel = viewModel.getCheckerLabel()
                                    val toastMsg = if (checkerLabel == "URLVoid") {
                                        "URL copied to clipboard. Please paste it in the search box."
                                    } else {
                                        "URL copied. Opening scan report..."
                                    }
                                    android.widget.Toast.makeText(context, toastMsg, android.widget.Toast.LENGTH_LONG).show()
                                    
                                    val checkerUrl = viewModel.getCheckerUrl(urlToCheck)
                                    BrowserLauncher.openInExternalBrowser(context, checkerUrl)
                                } else {
                                    android.widget.Toast.makeText(context, "URL is null or empty", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                viewModel.closeInterceptor()
                                (context as? android.app.Activity)?.finish()
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            testTag = "check_virustotal_btn",
                            modifier = Modifier.weight(1f)
                        ) {
                            StickSecurityIcon(
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Check on\n${viewModel.getCheckerLabel()}", 
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }

            // The Overlapping stick icon (magnifying glass) sits on the exact top edge of the surface popup (Y-center constrained)
            val circleShape = RoundedCornerShape(50)
            val glassBgColor = if (isDark) Color(0x23111827) else Color(0x40FFFFFF)
            val glassBorder = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.35f))

            // Overlapping search icon card component with custom shape and glassmorphism styling
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .constrainAs(searchIconRef) {
                        top.linkTo(glassCardRef.top)
                        bottom.linkTo(glassCardRef.top)
                        start.linkTo(glassCardRef.start)
                        end.linkTo(glassCardRef.end)
                    },
                contentAlignment = Alignment.Center
            ) {
                // Liquid glass blur underlay for the circular container
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(circleShape)
                        .graphicsLayer { renderEffect = SharedEffects.glassBlur }
                        .background(glassBgColor)
                )

                // Bright semi-transparent reflective border
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(circleShape)
                        .border(glassBorder, circleShape)
                )

                Image(
                    painter = painterResource(id = com.example.R.drawable.ic_generated_logo),
                    contentDescription = "App Icon Header",
                    modifier = Modifier
                        .size(60.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                )
            }
        }
    }
}
