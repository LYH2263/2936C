const fs = require('fs');
const path = 'd:/Asolo2/2936C/对话/';

function analyze(filename, title) {
  const t = fs.readFileSync(path + filename, 'utf8');
  console.log('\n\n########################################');
  console.log('#', title);
  console.log('########################################');

  // Extract run_command blocks
  const cmdBlocks = [...t.matchAll(/toolName: run_command[\s\S]*?command:\s*([^\n]+)[\s\S]*?(?:exit code:|status:)/gi)];
  console.log('\n--- COMMANDS ---');
  cmdBlocks.forEach((m, i) => {
    console.log(`${i + 1}. ${m[1].trim().slice(0, 150)}`);
  });

  // GetDiagnostics
  const diag = [...t.matchAll(/GetDiagnostics[\s\S]{0,300}/g)];
  console.log('\n--- DIAGNOSTICS ---', diag.length);
  diag.forEach((m) => console.log(m[0].replace(/\s+/g, ' ').slice(0, 200)));

  // todo_write sequences
  const todos = [...t.matchAll(/Todos updated: (\d+) items/g)];
  console.log('\n--- TODO UPDATES ---', todos.length);

  // search patterns for process issues
  const checks = [
    ['mvn test', /mvn test/gi],
    ['npm run build', /npm run build/gi],
    ['docker', /docker/gi],
    ['未实际执行', /未实际执行/g],
    ['无 Maven', /无 Maven/g],
    ['GetDiagnostics', /GetDiagnostics/g],
    ['view_files', /view_files/g],
    ['Write', /toolName: Write/g],
    ['head_limit|First \d+ lines|Select-Object -First', /head_limit|First \d+ lines|Select-Object -First/gi],
    ['Permission denied', /Permission denied/gi],
    ['BUILD FAILURE', /BUILD FAILURE/gi],
    ['40%', /40%/g],
    ['行数', /行数/g],
  ];
  console.log('\n--- KEYWORD COUNTS ---');
  checks.forEach(([name, re]) => {
    const n = (t.match(re) || []).length;
    if (n) console.log(name + ':', n);
  });

  // first 3000 chars after user message if any
  const userIdx = t.indexOf('user:');
  if (userIdx >= 0) {
    console.log('\n--- USER SECTION ---');
    console.log(t.slice(userIdx, userIdx + 1200));
  }

  // look for assistant planning at start
  console.log('\n--- START (first 2500) ---');
  console.log(t.slice(0, 2500));
}

analyze('Spring Boot API 规范化与文档.md', '对话1: Spring Boot API 规范化与文档');
analyze('编写 ExamService 统计集成测试.md', '对话2: 编写 ExamService 统计集成测试');
analyze('重构 ExamView 逻辑为 Composable.md', '对话3: 重构 ExamView 逻辑为 Composable');
