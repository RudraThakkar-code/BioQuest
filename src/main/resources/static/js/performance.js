const API_BASE = '/api';

function ensureName() {
    const userName = localStorage.getItem('userName');
    if (!userName) {
        window.location.href = 'index.html';
        return null;
    }
    const titleObj = document.getElementById('headerTitle');
    if (titleObj) titleObj.textContent = `${userName}'s BioQuest - Performance Dashboard`;
    return userName;
}

document.addEventListener('DOMContentLoaded', async () => {
    if (!ensureName()) return;

    try {
        const res = await fetch(`${API_BASE}/performance`);
        const data = await res.json();
        
        const strengthsList = document.getElementById('strengthsList');
        const weaknessesList = document.getElementById('weaknessesList');
        const tableBody = document.getElementById('perfTableBody');
        
        let strengthsHtml = '';
        let weaknessesHtml = '';
        
        for (const [topic, stats] of Object.entries(data)) {
            const accuracy = stats.attempted > 0 ? (stats.correct / stats.attempted) * 100 : 0;
            const accuracyStr = accuracy.toFixed(1) + '%';
            
            // Populate table
            const tr = document.createElement('tr');
            tr.innerHTML = `
                <td>${topic}</td>
                <td>${stats.attempted}</td>
                <td>${stats.correct}</td>
                <td style="color: ${accuracy >= 70 ? 'var(--success)' : (accuracy <= 40 ? 'var(--danger)' : 'var(--text-color)')}">
                    ${accuracyStr}
                </td>
            `;
            tableBody.appendChild(tr);
            
            // Populate lists based on accuracy
            if (stats.attempted >= 1) { // Only classify if attempted at least once
                if (accuracy >= 70) {
                    strengthsHtml += `<span class="topic-tag tag-strong">${topic} (${accuracyStr})</span>`;
                } else if (accuracy <= 50) {
                    weaknessesHtml += `<span class="topic-tag tag-weak">${topic} (${accuracyStr})</span>`;
                }
            }
        }
        
        strengthsList.innerHTML = strengthsHtml || '<p>No clear strengths yet. Keep practicing!</p>';
        weaknessesList.innerHTML = weaknessesHtml || '<p>No major weak areas identified.</p>';
        
    } catch(e) {
        console.error("Failed to load performance data", e);
    }
});
