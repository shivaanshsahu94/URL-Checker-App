const fs = require('fs');
let code = fs.readFileSync('app/src/main/java/com/example/ui/screens/MainScreens.kt', 'utf8');

// Replace transitionSpec
code = code.replace(
    /if \(from == "history" && to == "settings"\) \{\n\s+\(slideInHorizontally.+?\n\s+slideOutHorizontally\(animationSpec = spring\(stiffness = Spring\.StiffnessMediumLow\)\) \{ -it \/ 3 \} \+ fadeOut\(\)\n\s+\)\n\s+\} else if \(from == "settings" && to == "history"\) \{\n\s+\(slideInHorizontally.+?\n\s+slideOutHorizontally\(animationSpec = spring\(stiffness = Spring\.StiffnessMediumLow\)\) \{ it \/ 3 \} \+ fadeOut\(\)\n\s+\)\n\s+\}/s,
    `if (from == "history" && to == "settings") {
                                (slideInHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) { it } + fadeIn()).togetherWith(
                                    slideOutHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) { -it / 3 } + fadeOut()
                                )
                            } else if (from == "settings" && to == "history") {
                                (slideInHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) { -it } + fadeIn()).togetherWith(
                                    slideOutHorizontally(animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) { it / 3 } + fadeOut()
                                )
                            }`
);

// Add pointerInput
// Need to find Box(modifier = Modifier.padding(innerPadding)) {
code = code.replace(
    /Box\(modifier = Modifier\.padding\(innerPadding\)\) \{/g,
    `var offsetX by remember { mutableFloatStateOf(0f) }
                Box(modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .pointerInput(activeTab) {
                        if (activeTab == "history" || activeTab == "settings") {
                            androidx.compose.foundation.gestures.detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (offsetX < -100 && activeTab == "history") {
                                        viewModel.setTab("settings")
                                    } else if (offsetX > 100 && activeTab == "settings") {
                                        viewModel.setTab("history")
                                    }
                                    offsetX = 0f
                                },
                                onDragCancel = { offsetX = 0f },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    offsetX += dragAmount
                                }
                            )
                        }
                    }
                ) {`
);

fs.writeFileSync('app/src/main/java/com/example/ui/screens/MainScreens.kt', code);
