async function fetchVoteList() {
  try {
    const response = await fetch('/vote/list');
    const voteItems = await response.json();
    const listElement = document.getElementById('voteList');
    listElement.innerHTML = '';

    voteItems.forEach(item => {
      const statusBadge = item.is_active
        ? '<span class="px-2 py-1 text-xs font-semibold text-green-700 bg-green-100 rounded-full">진행 중</span>'
        : '<span class="px-2 py-1 text-xs font-semibold text-gray-700 bg-gray-200 rounded-full">종료됨</span>';

      const card = `
        <a href="/html/detail.html?vote_id=${item.vote_id}" class="block bg-white p-5 rounded-xl shadow hover:shadow-lg transition">
          <div class="flex items-center justify-between mb-2">
            <h2 class="text-xl font-semibold text-blue-700">${item.title}</h2>
            ${statusBadge}
          </div>
          <p class="text-gray-600">${item.description}</p>
          <p class="text-sm text-gray-400 mt-2">
            ${formatDate(item.start_time)} ~ ${formatDate(item.end_time)}
          </p>
        </a>
      `;
      listElement.insertAdjacentHTML('beforeend', card);
    });
  } catch (err) {
    console.error('투표 리스트 불러오기 실패:', err);
  }
}

function formatDate(str) {
  const date = new Date(str);
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
}

fetchVoteList();
