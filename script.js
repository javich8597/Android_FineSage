const fs = require('fs');

const data = fs.readFileSync('app/src/main/java/com/example/ui/finance/FinanceScreens.kt', 'utf8');
const lines = data.split('\n');

function extract(startLine, endLine) {
    return lines.slice(startLine - 1, endLine - 1).join('\n');
}

const imports = extract(1, 57);

const sharedContent = `package com.example.ui.shared

${imports.replace('package com.example.ui.finance', '')}

${extract(58, 189)}
${extract(433, 539)}
${extract(2324, 2460)}
${extract(3225, 3266)}
`;

const dashboardContent = `package com.example.ui.dashboard

import com.example.ui.shared.*
import com.example.ui.history.*
import com.example.ui.finance.FinanceViewModel
${imports.replace('package com.example.ui.finance', '')}

${extract(539, 1218)}
${extract(1218, 1308)}
${extract(1308, 1419)}
`;

const coachContent = `package com.example.ui.coach

import com.example.ui.shared.*
import com.example.ui.finance.FinanceViewModel
${imports.replace('package com.example.ui.finance', '')}

${extract(1419, 1776)}
${extract(1776, 1841)}
`;

const goalsContent = `package com.example.ui.goals

import com.example.ui.shared.*
import com.example.ui.finance.FinanceViewModel
${imports.replace('package com.example.ui.finance', '')}

${extract(1841, 1909)}
${extract(1909, 1997)}
${extract(2460, 2553)}
`;

const managementContent = `package com.example.ui.management

import com.example.ui.shared.*
import com.example.ui.finance.FinanceViewModel
${imports.replace('package com.example.ui.finance', '')}

${extract(1997, 2324)}
`;

const historyContent = `package com.example.ui.history

import com.example.ui.shared.*
import com.example.ui.finance.FinanceViewModel
${imports.replace('package com.example.ui.finance', '')}

${extract(2553, 2788)}
${extract(2788, 2931)}
${extract(2931, 3086)}
${extract(3086, 3225)}
`;

const chartsContent = `package com.example.ui.charts

import com.example.ui.shared.*
import com.example.ui.finance.FinanceViewModel
${imports.replace('package com.example.ui.finance', '')}

${extract(3266, lines.length + 1)}
`;

const mainContent = `package com.example.ui.finance

import com.example.ui.shared.*
import com.example.ui.dashboard.*
import com.example.ui.coach.*
import com.example.ui.goals.*
import com.example.ui.history.*
import com.example.ui.management.*
import com.example.ui.charts.*
${imports.replace('package com.example.ui.finance', '')}

${extract(189, 433)}
`;

fs.mkdirSync('app/src/main/java/com/example/ui/shared', { recursive: true });
fs.mkdirSync('app/src/main/java/com/example/ui/dashboard', { recursive: true });
fs.mkdirSync('app/src/main/java/com/example/ui/coach', { recursive: true });
fs.mkdirSync('app/src/main/java/com/example/ui/goals', { recursive: true });
fs.mkdirSync('app/src/main/java/com/example/ui/management', { recursive: true });
fs.mkdirSync('app/src/main/java/com/example/ui/history', { recursive: true });
fs.mkdirSync('app/src/main/java/com/example/ui/charts', { recursive: true });

fs.writeFileSync('app/src/main/java/com/example/ui/shared/SharedComponents.kt', sharedContent);
fs.writeFileSync('app/src/main/java/com/example/ui/dashboard/DashboardScreen.kt', dashboardContent);
fs.writeFileSync('app/src/main/java/com/example/ui/coach/CoachScreen.kt', coachContent);
fs.writeFileSync('app/src/main/java/com/example/ui/goals/GoalsScreen.kt', goalsContent);
fs.writeFileSync('app/src/main/java/com/example/ui/management/ManagementScreen.kt', managementContent);
fs.writeFileSync('app/src/main/java/com/example/ui/history/HistoryScreen.kt', historyContent);
fs.writeFileSync('app/src/main/java/com/example/ui/charts/ChartsScreen.kt', chartsContent);
fs.writeFileSync('app/src/main/java/com/example/ui/finance/FinanceScreens.kt', mainContent);
