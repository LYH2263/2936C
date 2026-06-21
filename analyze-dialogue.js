const fs = require('fs');
const path = 'd:/Asolo2/2936C/对话/';
const files = [
  'Spring Boot API 规范化与文档.md',
  '编写 ExamService 统计集成测试.md',
  '重构 ExamView 逻辑为 Composable.md',
];

for (const f of files) {
  const t = fs.readFileSync(path + f, 'utf8');
  console.log('\n===', f, '===');
  console.log('Lines:', t.split('\n').length);

  const tools = [...t.matchAll(/toolName: (\w+)/g)].map((m) => m[1]);
  console.log('Tool calls:', tools.length);
  const counts = {};
  tools.forEach((x) => { counts[x] = (counts[x] || 0) + 1; });
  console.log('Tool breakdown:', JSON.stringify(counts));

  const exits = [...t.matchAll(/exit code: ([^\n]+)/gi)].map((m) => m[1].trim());
  console.log('Exit codes:', exits.length ? exits.join(' | ') : 'none');

  const errors = [...t.matchAll(/(Error[^\n]{0,120}|失败[^\n]{0,80}|BUILD FAILURE|Tests run:[^\n]+Failures: [1-9])/g)]
    .slice(0, 8)
    .map((m) => m[1]);
  if (errors.length) {
    console.log('Error snippets:');
    errors.forEach((e) => console.log(' -', e.slice(0, 120)));
  }

  // find last 1500 chars for summary
  console.log('\n--- TAIL ---');
  console.log(t.slice(-1800));
}
