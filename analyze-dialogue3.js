const fs = require('fs');
const path = 'd:/Asolo2/2936C/对话/';

function extractSections(file) {
  const t = fs.readFileSync(path + file, 'utf8');
  const sections = [];

  // All run_command with surrounding context
  let idx = 0;
  while ((idx = t.indexOf('toolName: run_command', idx)) !== -1) {
    sections.push('=== RUN_COMMAND ===\n' + t.slice(idx, idx + 2500));
    idx += 10;
  }

  // mvn/docker related paragraphs
  const keywords = ['mvn compile', 'mvn test', 'where.exe', 'docker run', '未实际执行', 'GetDiagnostics', 'npm run build', 'npm install', '40%', '行数', '1052', '834', 'Permission', 'BUILD', 'Tests run'];
  keywords.forEach((kw) => {
    let i = 0;
    while ((i = t.indexOf(kw, i)) !== -1) {
      sections.push(`=== KW:${kw} ===\n` + t.slice(Math.max(0, i - 200), i + 600));
      i += kw.length;
    }
  });

  return sections.join('\n\n');
}

const files = [
  ['Spring Boot API 规范化与文档.md', 'api'],
  ['编写 ExamService 统计集成测试.md', 'test'],
  ['重构 ExamView 逻辑为 Composable.md', 'examview'],
];

files.forEach(([f, tag]) => {
  fs.writeFileSync(`d:/Asolo2/2936C/extract-${tag}.txt`, extractSections(f), 'utf8');
});

console.log('done');
