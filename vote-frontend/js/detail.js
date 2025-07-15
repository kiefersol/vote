let voteId = null;
let isActive = false;
let chart = null;

async function loadVoteDetail() {
  const params = new URLSearchParams(window.location.search);
  voteId = params.get('vote_id');

  // 1. 상세 정보
  const detailRes = await fetch(`/vote/detail?vote_id=${voteId}`);
  const detail = await detailRes.json();

  document.getElementById('voteTitle').textContent = detail.title;
  document.getElementById('voteDescription').textContent = detail.description;

  isActive = new Date(detail.end_time) > new Date() && detail.is_active;

  // 2. 투표 가능 상태일 경우: 실시간 결과
  if (isActive) {
    const resultRes = await fetch(`/vote/result?vote_id=${voteId}`);
    const json = await resultRes.json();
    const results = json.results;

    updateChart(results.map(r => r.option_text), results.map(r => r.vote_count));
    updateVoteButtons(results);
  } else {
    // 3. 투표 종료 상태일 경우: 최종 결과
    const finalRes = await fetch(`/vote/final_result?vote_id=${voteId}`);
    const results = await finalRes.json();

    const labels = results.map(r => r.option_text);
    const data = results.map(r => r.db_count);
    const maxCount = Math.max(...data);
    const highlight = results.map(r => r.db_count === maxCount);

    updateFinalChart(labels, data, highlight);

    const container = document.getElementById('voteOptions');
    container.innerHTML = `<p class="text-red-500 font-semibold">이 투표는 종료되었습니다. 최종 결과를 확인하세요.</p>`;

    renderFinalResultTable(results, maxCount);
  }
}

function updateChart(labels, data) {
  const ctx = document.getElementById('voteChart');

  if (chart) {
    chart.data.labels = labels;
    chart.data.datasets[0].data = data;
    chart.update();
  } else {
    chart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: labels,
        datasets: [{
          label: '투표 수',
          data: data,
          backgroundColor: ['#4f46e5', '#16a34a', '#facc15', '#f87171', '#ec4899', '#0ea5e9'],
          borderColor: '#ffffff',
          borderWidth: 2
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: { position: 'bottom' },
          tooltip: {
            callbacks: {
              label: ctx => `${ctx.label}: ${ctx.parsed}표`
            }
          }
        }
      }
    });
  }
}

function updateFinalChart(labels, data, highlight) {
  const baseColors = ['#4f46e5', '#16a34a', '#facc15', '#f87171', '#ec4899', '#0ea5e9'];
  const bgColors = baseColors.map((color, i) =>
    highlight[i] ? color : color + '22' // 반투명 처리 (더 강하게)
  );

  if (chart) chart.destroy();

  chart = new Chart(document.getElementById('voteChart'), {
    type: 'doughnut',
    data: {
      labels: labels,
      datasets: [{
        label: '최종 결과',
        data: data,
        backgroundColor: bgColors,
        borderColor: '#ffffff',
        borderWidth: 2
      }]
    },
    options: {
      responsive: true,
      plugins: {
        legend: { position: 'bottom' },
        tooltip: {
          callbacks: {
            label: ctx => `${ctx.label}: ${ctx.parsed}표`
          }
        }
      }
    }
  });
}

function renderFinalResultTable(results, maxCount) {
  const totalVotes = results.reduce((sum, r) => sum + r.db_count, 0);

  const tableContainer = document.createElement('div');
  tableContainer.className = 'mt-6';

  tableContainer.innerHTML = `
    <h2 class="text-xl font-bold text-center text-gray-800 mb-2">🏆 최종 결과 표</h2>

    <div class="overflow-x-auto">
      <table class="min-w-full table-auto border border-gray-300 rounded-lg shadow-md bg-white">
        <thead class="bg-blue-100 text-gray-800">
          <tr>
            <th class="border p-3 text-sm font-semibold">항목</th>
            <th class="border p-3 text-sm font-semibold">투표 수</th>
            <th class="border p-3 text-sm font-semibold">1등 여부</th>
          </tr>
        </thead>
        <tbody>
          ${results.map(r => {
            const isWinner = r.db_count === maxCount;
            return `
              <tr class="${isWinner ? 'bg-yellow-100 font-bold text-yellow-900' : 'hover:bg-gray-50'} transition">
                <td class="border p-3">${r.option_text}</td>
                <td class="border p-3">${r.db_count}</td>
                <td class="border p-3 text-lg">${isWinner ? '👑' : ''}</td>
              </tr>`;
          }).join('')}
        </tbody>
      </table>
    <br>
    <p class="text-sm text-gray-600 mb-4 text-right">
      총 투표 수 : <span class="font-semibold">${totalVotes}</span>표
    </p>

    </div>
  `;

  document.getElementById('voteOptions').appendChild(tableContainer);
}

function updateVoteButtons(results) {
  const optionContainer = document.getElementById('voteOptions');
  optionContainer.innerHTML = '';

  results.forEach(option => {
    const btn = document.createElement('button');
    btn.className = 'px-4 py-2 m-1 bg-blue-500 text-white rounded hover:bg-blue-600';
    btn.textContent = option.option_text;

    btn.onclick = () => {
      showConfirm(`"${option.option_text}"에 투표하시겠습니까?`, async () => {
        try {
          const res = await fetch('/vote/submit', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: `vote_id=${voteId}&option_id=${option.option_id}`
          });

          if (res.ok) {
            showToast('투표가 완료되었습니다.');
            refreshVoteResults();
          } else {
            showToast('투표에 실패했습니다.');
          }
        } catch (err) {
          console.error(err);
          showToast('에러가 발생했습니다.');
        }
      });
    };

    optionContainer.appendChild(btn);
  });
}

async function refreshVoteResults() {
  const resultRes = await fetch(`/vote/result?vote_id=${voteId}`);
  const json = await resultRes.json();
  const results = json.results;

  updateChart(results.map(r => r.option_text), results.map(r => r.vote_count));
  updateVoteButtons(results);
}

loadVoteDetail();
