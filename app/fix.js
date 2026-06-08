const fs = require('fs');

let content = fs.readFileSync('app/src/main/java/com/example/ui/screens/MainScreens.kt', 'utf8');

const repairs = [
    { start: 246, name: 'backBlurEffect', replaceSize: 3, radius: '20f, 20f' },
    { start: 320, name: 'cardBlurEffect', replaceSize: 3, radius: '30f, 30f' },
    { start: 405, name: 'pillBlurEffect', replaceSize: 3, radius: '20f, 20f' },
    { start: 482, name: 'circularBlurEffect', replaceSize: 3, radius: '20f, 20f' },
    { start: 540, name: 'ambientBlurEffect', replaceSize: 3, radius: '30f, 30f' },
    { start: 1303, name: 'popupBackdropBlur', replaceSize: 3, radius: '30f, 30f' },
    { start: 2126, name: 'navBlurEffect', replaceSize: 3, radius: '25f, 25f' }
];

let lines = content.split('\n');

for (let r of repairs.reverse()) {
    let lineIdx = r.start - 1;
    lines[lineIdx] = `    val ${r.name} = remember {`;
    lines[lineIdx+1] = `        android.graphics.RenderEffect.createBlurEffect(`;
    lines[lineIdx+2] = `            ${r.radius}, android.graphics.Shader.TileMode.CLAMP`;
    lines.splice(lineIdx+3, 0, `        ).asComposeRenderEffect()`, `    }`);
}

content = lines.join('\n');

const applyRepairs = [
    { start: 275+7, replaceWord: 'backBlurEffect' },
    { start: 348+7, replaceWord: 'cardBlurEffect' },
    { start: 428+7, replaceWord: 'pillBlurEffect' },
    { start: 506+7, replaceWord: 'circularBlurEffect' },
    { start: 551+7, replaceWord: 'ambientBlurEffect' },
    { start: 1338+7+2, replaceWord: 'popupBackdropBlur' },
    { start: 1705+7+2, replaceWord: 'popupBackdropBlur' }, // wait, there were two usages of popupBackdropBlur?
    { start: 2148+7+2+2, replaceWord: 'navBlurEffect' }
];

// Instead of line numbers, I'll just use a regex for `.graphicsLayer { renderEffect =  }`!

content = content.replace(/\.graphicsLayer \{ renderEffect =  \}/g, () => 'REPLACE_ME');

let idx = 0;
const namesList = [
    'backBlurEffect',
    'cardBlurEffect',
    'pillBlurEffect',
    'circularBlurEffect',
    'ambientBlurEffect',
    'popupBackdropBlur',
    'popupBackdropBlur', // Search Icon blur used it too?
    'navBlurEffect'
];

content = content.replace(/REPLACE_ME/g, () => {
    return `.graphicsLayer { renderEffect = ${namesList[idx++]} }`
});

fs.writeFileSync('app/src/main/java/com/example/ui/screens/MainScreens.kt', content);
